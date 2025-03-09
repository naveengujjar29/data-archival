package com.archival.archivalservice.dto;

public final class Constants {
    // Constants for string literals
    public static final String ARCHIVAL_TABLE_SUFFIX = "_archive";
    public static final String SCHEDULER_ARCHIVE_CRON_PROPERTY = "scheduler.archive.cron";
    public static final String APP_DATA_SOURCE_QUALIFIER = "appDataSource";
    public static final String ARCHIVAL_DATA_SOURCE_QUALIFIER = "archivalDataSource";
    public static final String PERMISSION_DENIED_MESSAGE_PREFIX = "User does not have permission to configure configuration on this table ";
    public static final String TABLE_NAME_FIELD = "tableName";
    public static final String ARCHIVE_AFTER_FIELD = "archiveAfter";
    public static final String DELETE_AFTER_FIELD = "deleteAfter";
    public static final String ARCHIVAL_TIME_UNIT_FIELD = "archivalTimeUnit";
    public static final String DELETE_AFTER_TIME_UNIT_FIELD = "deleteAfterTimeUnit";
    public static final String STARTING_ARCHIVAL_PROCESS_MESSAGE = "Starting archival process...";
    public static final String NO_CRITERIA_FOUND_MESSAGE = "No archival criteria found. Skipping archival process.";
    public static final String ARCHIVED_RECORDS_MESSAGE = "Archived {} records for table: {}";
    public static final String DELETED_RECORDS_MESSAGE = "Deleted {} old records from archival DB for table: {}";
    public static final String FAILED_PROCESS_MESSAGE = "Failed to process archival for table {}: {}";
    public static final String ARCHIVAL_PROCESS_COMPLETED_MESSAGE = "Archival process completed.";
    public static final String CREATED_AT_COLUMN = "created_at";
    public static final String MONTHS_TIME_UNIT = "MONTHS";
    public static final String YEARS_TIME_UNIT = "YEARS";
    public static final String UNSUPPORTED_TIME_UNIT_MESSAGE = "Unsupported time unit: ";
    public static final String COLUMN_NAME_FIELD = "COLUMN_NAME";
    public static final String NO_COLUMNS_FOUND_MESSAGE = "No columns found for table: ";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String COMMA_SEPARATOR = ",";
    public static final String WHERE_CLAUSE = " WHERE ";
    public static final String DESC_SORT = "desc";
    public static final String ASC_SORT = "ASC";
    public static final String DESC_SORT_UPPER = "DESC";
    public static final String ORDER_BY_CLAUSE = " ORDER BY created_at ";
    public static final String LIMIT_OFFSET_CLAUSE = " LIMIT ? OFFSET ?";
    public static final String AND_CLAUSE = " AND ";
    public static final String GREATER_THAN_EQUAL_CLAUSE = "created_at >= ?";
    public static final String LESS_THAN_EQUAL_CLAUSE = "created_at <= ?";
}
