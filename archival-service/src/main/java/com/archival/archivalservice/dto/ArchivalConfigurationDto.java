package com.archival.archivalservice.dto;

import com.archival.archivalservice.enums.ArchivalTimeUnit;

public class ArchivalConfigurationDto {
    private String tableName;
    private int archiveAfter;
    private ArchivalTimeUnit archivalTimeUnit;
    private int deleteAfter;
    private ArchivalTimeUnit deleteAfterTimeUnit;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getArchiveAfter() {
        return archiveAfter;
    }

    public void setArchiveAfter(int archiveAfter) {
        this.archiveAfter = archiveAfter;
    }

    public ArchivalTimeUnit getArchivalTimeUnit() {
        return archivalTimeUnit;
    }

    public void setArchivalTimeUnit(ArchivalTimeUnit archivalTimeUnit) {
        this.archivalTimeUnit = archivalTimeUnit;
    }

    public int getDeleteAfter() {
        return deleteAfter;
    }

    public void setDeleteAfter(int deleteAfter) {
        this.deleteAfter = deleteAfter;
    }

    public ArchivalTimeUnit getDeleteAfterTimeUnit() {
        return deleteAfterTimeUnit;
    }

    public void setDeleteAfterTimeUnit(ArchivalTimeUnit deleteAfterTimeUnit) {
        this.deleteAfterTimeUnit = deleteAfterTimeUnit;
    }
}
