package com.archival.archivalservice.service;

import com.archival.archivalservice.appmodels.ArchivalConfiguration;
import com.archival.archivalservice.appmodels.UserTableAssignment;
import com.archival.archivalservice.apprepository.ArchivalCriteriaRepository;
import com.archival.archivalservice.apprepository.UserTableAssignmentRepository;
import com.archival.archivalservice.dto.ArchivalConfigurationDto;
import com.archival.archivalservice.dto.ArchivalQueryDTO;
import com.archival.archivalservice.dto.Constants;
import com.archival.archivalservice.dto.UserTableAssignmentDto;
import com.archival.archivalservice.exception.PermissionDeniedException;
import com.archival.archivalservice.utils.ObjectConverter;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@RefreshScope
public class ArchivalService {

    private static final Logger logger = LoggerFactory.getLogger(ArchivalService.class);

    @Autowired
    private ArchivalCriteriaRepository archivalCriteriaRepository;

    @Autowired
    private UserTableAssignmentRepository userTableAssignmentRepository;

    @Autowired
    private ObjectConverter objectConverter;

    @Autowired
    @Qualifier(Constants.APP_DATA_SOURCE_QUALIFIER)
    private DataSource appDataSource;

    @Autowired
    @Qualifier(Constants.ARCHIVAL_DATA_SOURCE_QUALIFIER)
    private DataSource archivalDataSource;

    public ArchivalConfigurationDto configureTableArchivalSetting(ArchivalConfigurationDto archivalConfigurationDto) throws PermissionDeniedException {
        if (!hasPermissionOnTable(archivalConfigurationDto.getTableName())) {
            throw new PermissionDeniedException(Constants.PERMISSION_DENIED_MESSAGE_PREFIX + archivalConfigurationDto.getTableName());
        }
        ArchivalConfiguration archivalConfiguration;
        Optional<ArchivalConfiguration> savedArchivalConfiguration = this.archivalCriteriaRepository.findByTableName(archivalConfigurationDto.getTableName());
        if (savedArchivalConfiguration.isPresent()) {
            archivalConfiguration = savedArchivalConfiguration.get();
            archivalConfiguration.setTableName(archivalConfigurationDto.getTableName());
            archivalConfiguration.setArchiveAfter(archivalConfigurationDto.getArchiveAfter());
            archivalConfiguration.setDeleteAfter(archivalConfigurationDto.getDeleteAfter());
            archivalConfiguration.setArchivalTimeUnit(archivalConfigurationDto.getArchivalTimeUnit());
            archivalConfiguration.setDeleteAfterTimeUnit(archivalConfigurationDto.getDeleteAfterTimeUnit());
            archivalConfiguration.setArchivalColumnName(archivalConfigurationDto.getArchivalColumnName());
        } else {
            archivalConfiguration = (ArchivalConfiguration) this.objectConverter.convert(archivalConfigurationDto, ArchivalConfiguration.class);
        }

        archivalConfiguration = this.archivalCriteriaRepository.saveAndFlush(archivalConfiguration);
        return (ArchivalConfigurationDto) this.objectConverter.convert(archivalConfiguration, ArchivalConfigurationDto.class);
    }

    public List<ArchivalConfigurationDto> getArchivalConfiguration() {
        List<ArchivalConfiguration> configurations = this.archivalCriteriaRepository.findAll();
        return configurations.stream()
                .map(s -> (ArchivalConfigurationDto) this.objectConverter.convert(s, ArchivalConfigurationDto.class))
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "${scheduler.archive.cron:0 0 1 * * *}")
    @Transactional
    public void archiveData() {
        logger.info("Starting archival process...");
        List<ArchivalConfiguration> criteriaList = archivalCriteriaRepository.findAll();

        if (criteriaList.isEmpty()) {
            logger.info("No archival criteria found. Skipping archival process.");
            return;
        }

        for (ArchivalConfiguration criteria : criteriaList) {
            String tableName = criteria.getTableName();
            try {
                long archivedRecords = archiveTableData(tableName, criteria.getArchiveAfter(),
                        TimeUnit.valueOf(criteria.getArchivalTimeUnit().toString()), criteria.getArchivalColumnName());
                logger.info("Archived {} records for table: {}", archivedRecords, tableName);
                String deleteTableFromArchivalName = tableName + Constants.ARCHIVAL_TABLE_SUFFIX;
                long deletedRecords = deleteOldDataFromArchivalDB(deleteTableFromArchivalName, criteria.getDeleteAfter(),
                        TimeUnit.valueOf(criteria.getDeleteAfterTimeUnit().toString()), criteria.getArchivalColumnName());
                logger.info("Deleted {} old records from archival DB for table: {}", deletedRecords, tableName);
            } catch (Exception e) {
                logger.error("Failed to process archival for table {}: {}", tableName, e.getMessage());
            }
        }
        logger.info("Archival process completed.");
    }

    private long archiveTableData(String tableName, long archiveAfter, TimeUnit timeUnit, String columnName) throws SQLException {
        LocalDateTime archiveThreshold = calculateThreshold(LocalDateTime.now(), archiveAfter, timeUnit);
        JdbcTemplate appJdbcTemplate = new JdbcTemplate(appDataSource);
        JdbcTemplate archivalJdbcTemplate = new JdbcTemplate(archivalDataSource);

        List<String> columns = getTableColumns(tableName, appDataSource);
        String columnList = String.join(Constants.COMMA_SEPARATOR + " ", columns);
        String placeholders = String.join(Constants.COMMA_SEPARATOR + " ", columns.stream().map(c -> "?").collect(Collectors.toList()));

        String selectQuery = "SELECT " + columnList + " FROM " + tableName + Constants.WHERE_CLAUSE + columnName + " < ?";
        List<Object[]> recordsToArchive = appJdbcTemplate.query(selectQuery,
                (rs, rowNum) -> {
                    Object[] values = new Object[columns.size()];
                    for (int i = 0; i < columns.size(); i++) {
                        values[i] = rs.getObject(i + 1);
                    }
                    return values;
                },
                archiveThreshold);

        if (recordsToArchive.isEmpty()) {
            logger.info("No records to archive for table: {}", tableName);
            return 0;
        }

        String insertQuery = "INSERT INTO " + tableName + Constants.ARCHIVAL_TABLE_SUFFIX + " (" + columnList + ") VALUES (" + placeholders + ")";
        try (PreparedStatement stmt = archivalJdbcTemplate.getDataSource().getConnection().prepareStatement(insertQuery)) {
            for (Object[] record : recordsToArchive) {
                for (int i = 0; i < record.length; i++) {
                    stmt.setObject(i + 1, record[i]);
                }
                stmt.addBatch();
            }
            int[] rowsInserted = stmt.executeBatch();
            int totalInserted = Arrays.stream(rowsInserted).sum();
            logger.info("Inserted {} records into archival DB for table: {}", totalInserted, tableName);

            String deleteQuery = "DELETE FROM " + tableName + Constants.WHERE_CLAUSE + Constants.CREATED_AT_COLUMN + " < ?";
            int rowsDeleted = appJdbcTemplate.update(deleteQuery, archiveThreshold);
            logger.info("Deleted {} records from app DB for table: {}", rowsDeleted, tableName);

            if (totalInserted != rowsDeleted) {
                logger.warn("Mismatch between inserted ({}) and deleted ({}) records for table: {}", totalInserted, rowsDeleted, tableName);
            }
            return totalInserted;
        }
    }

    private long deleteOldDataFromArchivalDB(String tableName, long deleteAfter, TimeUnit timeUnit, String columnName) throws SQLException {
        LocalDateTime deleteThreshold = calculateThreshold(LocalDateTime.now(), deleteAfter, timeUnit);
        JdbcTemplate archivalJdbcTemplate = new JdbcTemplate(archivalDataSource);

        String deleteQuery = "DELETE FROM " + tableName + Constants.WHERE_CLAUSE + columnName + " < ?";
        int rowsDeleted = archivalJdbcTemplate.update(deleteQuery, deleteThreshold);
        logger.info("Deleted {} old records from archival DB for table: {}", rowsDeleted, tableName);

        return rowsDeleted;
    }

    private LocalDateTime calculateThreshold(LocalDateTime baseTime, long duration, TimeUnit timeUnit) {
        return switch (timeUnit) {
            case DAYS -> baseTime.minusDays(duration);
            case HOURS -> baseTime.minus(duration, ChronoUnit.HOURS);
            case MINUTES -> baseTime.minus(duration, ChronoUnit.MINUTES);
            default -> {
                if (Constants.MONTHS_TIME_UNIT.equals(timeUnit.name())) {
                    yield baseTime.minusDays(duration * 30);
                } else if (Constants.YEARS_TIME_UNIT.equals(timeUnit.name())) {
                    yield baseTime.minusDays(duration * 365);
                }
                throw new IllegalArgumentException(Constants.UNSUPPORTED_TIME_UNIT_MESSAGE + timeUnit);
            }
        };
    }

    private List<String> getTableColumns(String tableName, DataSource dataSource) throws SQLException {
        try (var conn = dataSource.getConnection();
             var rs = conn.getMetaData().getColumns(null, null, tableName, null)) {
            List<String> columns = new ArrayList<>();
            while (rs.next()) {
                columns.add(rs.getString(Constants.COLUMN_NAME_FIELD));
            }
            if (columns.isEmpty()) {
                throw new IllegalArgumentException(Constants.NO_COLUMNS_FOUND_MESSAGE + tableName);
            }
            return columns;
        }
    }

    public UserTableAssignmentDto assignTablesToUser(UserTableAssignmentDto dto) {
        Optional<UserTableAssignment> existingAssignment = this.userTableAssignmentRepository.findByUserName(dto.getUserName());
        UserTableAssignment userAssignment;
        if (existingAssignment.isPresent()) {
            userAssignment = existingAssignment.get();
            userAssignment.setUserName(dto.getUserName());
            userAssignment.setTableNames(dto.getTableNames());
        } else {
            userAssignment = (UserTableAssignment) this.objectConverter.convert(dto, UserTableAssignment.class);
        }

        userAssignment = this.userTableAssignmentRepository.save(userAssignment);
        return (UserTableAssignmentDto) this.objectConverter.convert(userAssignment, UserTableAssignmentDto.class);
    }

    private boolean hasPermissionOnTable(String tableName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace(Constants.ROLE_PREFIX, ""))
                .collect(Collectors.toList());
        if (roles.contains(Constants.ADMIN_ROLE)) {
            return true;
        }
        Optional<UserTableAssignment> userAssignment = this.userTableAssignmentRepository.findByUserName(authentication.getName());
        if (userAssignment.isPresent()) {
            String tables = userAssignment.get().getTableNames();
            return Arrays.stream(tables.split(Constants.COMMA_SEPARATOR))
                    .map(String::trim)
                    .anyMatch(name -> name.equals(tableName));
        }
        return false;
    }

    public List<Map<String, Object>> getArchivedData(String tableName, ArchivalQueryDTO queryParams) throws PermissionDeniedException {
        if (!hasPermissionOnTable(tableName)) {
            throw new PermissionDeniedException(Constants.PERMISSION_DENIED_MESSAGE_PREFIX + tableName);
        }
        String targetTableName = tableName + Constants.ARCHIVAL_TABLE_SUFFIX;
        StringBuilder query = new StringBuilder("SELECT * FROM " + targetTableName);
        List<Object> params = new ArrayList<>();
        JdbcTemplate archivalJdbcTemplate = new JdbcTemplate(archivalDataSource);

        if (queryParams.getStartDate() != null || queryParams.getEndDate() != null) {
            query.append(Constants.WHERE_CLAUSE);
            if (queryParams.getStartDate() != null) {
                query.append(Constants.GREATER_THAN_EQUAL_CLAUSE);
                params.add(queryParams.getStartDate());
            }
            if (queryParams.getStartDate() != null && queryParams.getEndDate() != null) {
                query.append(Constants.AND_CLAUSE);
            }
            if (queryParams.getEndDate() != null) {
                query.append(Constants.LESS_THAN_EQUAL_CLAUSE);
                params.add(queryParams.getEndDate());
            }
        }

        String sortOrder = Constants.DESC_SORT.equalsIgnoreCase(queryParams.getSort()) ? Constants.DESC_SORT_UPPER : Constants.ASC_SORT;
        query.append(Constants.ORDER_BY_CLAUSE).append(sortOrder);

        int offset = queryParams.getPage() * queryParams.getSize();
        query.append(Constants.LIMIT_OFFSET_CLAUSE);
        params.add(queryParams.getSize());
        params.add(offset);

        return archivalJdbcTemplate.queryForList(query.toString(), params.toArray());
    }
}