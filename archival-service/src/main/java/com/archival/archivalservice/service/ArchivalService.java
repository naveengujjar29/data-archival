package com.archival.archivalservice.service;


import com.archival.archivalservice.appmodels.ArchivalConfiguration;
import com.archival.archivalservice.appmodels.UserTableAssignment;
import com.archival.archivalservice.apprepository.ArchivalCriteriaRepository;
import com.archival.archivalservice.apprepository.UserTableAssignmentRepository;
import com.archival.archivalservice.dto.ArchivalConfigurationDto;
import com.archival.archivalservice.dto.ArchivalQueryDTO;
import com.archival.archivalservice.dto.UserTableAssignmentDto;
import com.archival.archivalservice.exception.PermissionDeniedException;
import com.archival.archivalservice.utils.ObjectConverter;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class ArchivalService {

    private static final Logger logger = LoggerFactory.getLogger(ArchivalService.class);
    private static final String ARCHIVAL_TABLE_SUFFIX = "_archive";

    @Autowired
    private ArchivalCriteriaRepository archivalCriteriaRepository;

    @Autowired
    private UserTableAssignmentRepository userTableAssignmentRepository;

    @Autowired
    private ObjectConverter objectConverter;

    @Autowired
    @Qualifier("appDataSource")
    private DataSource appDataSource;

    @Autowired
    @Qualifier("archivalDataSource")
    private DataSource archivalDataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ArchivalConfigurationDto configureTableArchivalSetting(ArchivalConfigurationDto archivalConfigurationDto) throws PermissionDeniedException {
        if (!hasPermissionOnTable(archivalConfigurationDto.getTableName())) {
            throw new PermissionDeniedException("User does not have permission to configure configuration on this table " + archivalConfigurationDto.getTableName());
        }
        ArchivalConfiguration archivalConfiguration;
        Optional<ArchivalConfiguration> savedArchivalConfiguration = this.archivalCriteriaRepository.findByTableName((archivalConfigurationDto.getTableName()));
        if (savedArchivalConfiguration.isPresent()) {
            archivalConfiguration = savedArchivalConfiguration.get();
            archivalConfiguration.setTableName(archivalConfigurationDto.getTableName());
            archivalConfiguration.setArchiveAfter(archivalConfigurationDto.getArchiveAfter());
            archivalConfiguration.setDeleteAfter(archivalConfigurationDto.getDeleteAfter());
            archivalConfiguration.setArchivalTimeUnit(archivalConfigurationDto.getArchivalTimeUnit());
            archivalConfiguration.setDeleteAfterTimeUnit(archivalConfigurationDto.getDeleteAfterTimeUnit());
        } else {
            archivalConfiguration = (ArchivalConfiguration) this.objectConverter.convert(archivalConfigurationDto, ArchivalConfiguration.class);
        }

        archivalConfiguration = this.archivalCriteriaRepository.saveAndFlush(archivalConfiguration);
        ArchivalConfigurationDto savedDto = (ArchivalConfigurationDto) this.objectConverter.convert(archivalConfiguration, ArchivalConfigurationDto.class);
        return savedDto;
    }

    public List<ArchivalConfigurationDto> getArchivalConfiguration() {
        List<ArchivalConfiguration> configurations = this.archivalCriteriaRepository.findAll();
        List<ArchivalConfigurationDto> data = configurations.stream().map(s -> (ArchivalConfigurationDto) this.objectConverter.convert(s,
                ArchivalConfigurationDto.class)).collect(Collectors.toList());
        return data;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight
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
                // Archive data from app DB to archival DB
                long archivedRecords = archiveTableData(tableName, criteria.getArchiveAfter(), TimeUnit.valueOf(criteria.getArchivalTimeUnit().toString()));
                logger.info("Archived {} records for table: {}", archivedRecords, tableName);

                // Delete old data from archival DB
                String deleteTableFromArchivalName = tableName + ARCHIVAL_TABLE_SUFFIX;
                long deletedRecords = deleteOldDataFromArchivalDB(deleteTableFromArchivalName, criteria.getDeleteAfter(), TimeUnit.valueOf(criteria.getDeleteAfterTimeUnit().toString()));
                logger.info("Deleted {} old records from archival DB for table: {}", deletedRecords, tableName);
            } catch (Exception e) {
                logger.error("Failed to process archival for table {}: {}", tableName, e.getMessage());
            }
        }
        logger.info("Archival process completed.");
    }

    private long archiveTableData(String tableName, long archiveAfter, TimeUnit timeUnit) throws SQLException {
        // Calculate the threshold based on the TimeUnit
        LocalDateTime archiveThreshold = calculateThreshold(LocalDateTime.now(), archiveAfter, timeUnit);
        JdbcTemplate appJdbcTemplate = new JdbcTemplate(appDataSource);
        JdbcTemplate archivalJdbcTemplate = new JdbcTemplate(archivalDataSource);

        // Get metadata to dynamically handle columns
        List<String> columns = getTableColumns(tableName, appDataSource);
        String columnList = String.join(", ", columns);
        String placeholders = String.join(", ", columns.stream().map(c -> "?").collect(Collectors.toList()));

        // Select records to archive
        String selectQuery = "SELECT " + columnList + " FROM " + tableName + " WHERE created_at < ?";
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

        // Insert into archival DB
        String insertQuery = "INSERT INTO " + tableName + "_archive" + " (" + columnList + ") VALUES (" + placeholders + ")";
        try (PreparedStatement stmt = archivalJdbcTemplate.getDataSource().getConnection().prepareStatement(insertQuery)) {
            for (Object[] record : recordsToArchive) {
                for (int i = 0; i < record.length; i++) {
                    stmt.setObject(i + 1, record[i]);
                }
                stmt.addBatch();
            }
            int[] rowsInserted = stmt.executeBatch();
            int totalInserted = rowsInserted.length;
            logger.info("Inserted {} records into archival DB for table: {}", totalInserted, tableName);

            // Delete from app DB
            String deleteQuery = "DELETE FROM " + tableName + " WHERE created_at < ?";
            int rowsDeleted = appJdbcTemplate.update(deleteQuery, archiveThreshold);
            logger.info("Deleted {} records from app DB for table: {}", rowsDeleted, tableName);

            if (totalInserted != rowsDeleted) {
                logger.warn("Mismatch between inserted ({}) and deleted ({}) records for table: {}", totalInserted, rowsDeleted, tableName);
            }

            return totalInserted;
        }
    }

    private long deleteOldDataFromArchivalDB(String tableName, long deleteAfter, TimeUnit timeUnit) throws SQLException {
        // Calculate the threshold based on the TimeUnit
        LocalDateTime deleteThreshold = calculateThreshold(LocalDateTime.now(), deleteAfter, timeUnit);
        JdbcTemplate archivalJdbcTemplate = new JdbcTemplate(archivalDataSource);

        String deleteQuery = "DELETE FROM " + tableName + " WHERE created_at < ?";
        int rowsDeleted = archivalJdbcTemplate.update(deleteQuery, deleteThreshold);
        logger.info("Deleted {} old records from archival DB for table: {}", rowsDeleted, tableName);

        return rowsDeleted;
    }

    private LocalDateTime calculateThreshold(LocalDateTime baseTime, long duration, TimeUnit timeUnit) {
        return switch (timeUnit) {
            case DAYS -> baseTime.minusDays(duration);
            case HOURS -> baseTime.minus(duration, ChronoUnit.HOURS);
            case MINUTES -> baseTime.minus(duration, ChronoUnit.MINUTES);
            case SECONDS -> baseTime.minus(duration, ChronoUnit.SECONDS);
            case MILLISECONDS -> baseTime.minus(duration, ChronoUnit.MILLIS);
            case MICROSECONDS -> baseTime.minus(duration, ChronoUnit.MICROS);
            case NANOSECONDS -> baseTime.minus(duration, ChronoUnit.NANOS);
            default -> {
                if ("MONTHS".equals(timeUnit.name())) {
                    yield baseTime.minusDays(duration * 30);
                } else if ("YEARS".equals(timeUnit.name())) {
                    yield baseTime.minusDays(duration * 365);
                }
                throw new IllegalArgumentException("Unsupported time unit: " + timeUnit);
            }
        };
    }

    private List<String> getTableColumns(String tableName, DataSource dataSource) throws SQLException {
        try (var conn = dataSource.getConnection();
             var rs = conn.getMetaData().getColumns(null, null, tableName, null)) {
            List<String> columns = new java.util.ArrayList<>();
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
            }
            if (columns.isEmpty()) {
                throw new IllegalArgumentException("No columns found for table: " + tableName);
            }
            return columns;
        }
    }

    public UserTableAssignmentDto assignTablesToUser(UserTableAssignmentDto dto) {
        Optional<UserTableAssignment> existingAssignment = this.userTableAssignmentRepository.findByUserName(dto.getUserName());
        UserTableAssignment userAssignment;
        if (existingAssignment.isPresent()) {
            // Update existing assignment
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
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
        if (roles.contains("ADMIN")) {
            return true;
        }
        Optional<UserTableAssignment> userAssignment = this.userTableAssignmentRepository.findByUserName(authentication.getName());
        if (userAssignment.isPresent()) {
            String tables = userAssignment.get().getTableNames();
            return Arrays.stream(tables.split(","))
                    .map(String::trim) // Trim whitespace
                    .anyMatch(name -> name.equals(tableName));
        }
        return false;
    }

    public List<Map<String, Object>> getArchivedData(String tableName, ArchivalQueryDTO queryParams) throws PermissionDeniedException {
        if (!hasPermissionOnTable(tableName)) {
            throw new PermissionDeniedException("User does not have permission to configure configuration on this table " + tableName);
        }
        String targetTableName = tableName + ARCHIVAL_TABLE_SUFFIX;
        StringBuilder query = new StringBuilder("SELECT * FROM " + targetTableName);
        List<Object> params = new ArrayList<>();
        JdbcTemplate archivalJdbcTemplate = new JdbcTemplate(archivalDataSource);

        // Add date range filtering if provided
        if (queryParams.getStartDate() != null || queryParams.getEndDate() != null) {
            query.append(" WHERE ");
            if (queryParams.getStartDate() != null) {
                query.append("created_at >= ?");
                params.add(queryParams.getStartDate());
            }
            if (queryParams.getStartDate() != null && queryParams.getEndDate() != null) {
                query.append(" AND ");
            }
            if (queryParams.getEndDate() != null) {
                query.append("created_at <= ?");
                params.add(queryParams.getEndDate());
            }
        }

        // Add sorting
        String sortOrder = "desc".equalsIgnoreCase(queryParams.getSort()) ? "DESC" : "ASC";
        query.append(" ORDER BY created_at ").append(sortOrder);

        // Add pagination
        int offset = queryParams.getPage() * queryParams.getSize();
        query.append(" LIMIT ? OFFSET ?");
        params.add(queryParams.getSize());
        params.add(offset);

        // Execute the query
        return archivalJdbcTemplate.queryForList(query.toString(), params.toArray());
    }
}
