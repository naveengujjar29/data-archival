package com.archival.archivalservice.appmodels;

import com.archival.archivalservice.enums.ArchivalTimeUnit;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Entity
@Table(name = "archival_criteria")
public class ArchivalConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tableName;
    private int archiveAfter;

    @Enumerated(EnumType.STRING)
    private ArchivalTimeUnit archivalTimeUnit;
    private int deleteAfter;

    @Enumerated(EnumType.STRING)
    private ArchivalTimeUnit deleteAfterTimeUnit;

    @Basic
    @CreatedDate
    @Column(insertable = false, updatable = false, columnDefinition = "TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP")
    private Date createdDateTime;

    @UpdateTimestamp
    @Column(insertable = false, updatable = false, columnDefinition = "TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP ON " +
            "UPDATE CURRENT_TIMESTAMP")
    private Date lastModifiedDateTime;

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

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Date getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    public void setLastModifiedDateTime(Date lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }
}
