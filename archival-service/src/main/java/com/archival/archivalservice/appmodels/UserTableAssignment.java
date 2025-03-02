package com.archival.archivalservice.appmodels;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;
import java.util.List;


@Entity
@Table(name = "user_table_assignments")
public class UserTableAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "table_names")
    private String tableNames; // Stored as a comma-separated string (e.g., "student,example_table")

    @CreatedDate
    @Column(name = "created_date_time", insertable = false, updatable = false, columnDefinition = "TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP")
    private Date createdDateTime;

    @UpdateTimestamp
    @Column(name = "updated_date_time", insertable = false, updatable = false, columnDefinition = "TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP ON " +
            "UPDATE CURRENT_TIMESTAMP")
    private Date updatedDateTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTableNames() {
        return tableNames;
    }

    public void setTableNames(String tableNames) {
        this.tableNames = tableNames;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Date getUpdatedDateTime() {
        return updatedDateTime;
    }

    public void setUpdatedDateTime(Date updatedDateTime) {
        this.updatedDateTime = updatedDateTime;
    }
}
