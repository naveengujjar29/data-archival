package com.archival.archivalservice.controller;

import com.archival.archivalservice.dto.ArchivalConfigurationDto;
import com.archival.archivalservice.dto.ArchivalQueryDTO;
import com.archival.archivalservice.dto.UserTableAssignmentDto;
import com.archival.archivalservice.service.ArchivalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/archival")
public class ArchivalController {

    @Autowired
    private ArchivalService archivalService;

    @PostMapping("/configuration")
    public ResponseEntity<ArchivalConfigurationDto> setArchivalCriteria(@RequestBody ArchivalConfigurationDto dto) throws Exception {
        ArchivalConfigurationDto archivalConfigurationDto = this.archivalService.configureTableArchivalSetting(dto);
        return new ResponseEntity<>(archivalConfigurationDto, HttpStatus.CREATED);
    }

    @GetMapping("/configuration")
    public ResponseEntity<List<ArchivalConfigurationDto>> getArchivalCriteria() {
        List<ArchivalConfigurationDto> data = this.archivalService.getArchivalConfiguration();
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/run-now")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> runArchivalNow() {
        archivalService.archiveData();
        return ResponseEntity.ok("Archival process triggered successfully.");
    }

    @PutMapping("/assign-tables")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignTableToUser(@RequestBody UserTableAssignmentDto dto) throws SQLException {
        UserTableAssignmentDto savedData = archivalService.assignTablesToUser(dto);
        return new ResponseEntity(savedData, HttpStatus.OK);
    }

    @GetMapping("/data/{tableName}")
    public ResponseEntity<List<Map<String, Object>>> getArchivedRecords(
            @PathVariable String tableName,
            @ModelAttribute ArchivalQueryDTO queryParams) throws Exception {
        List<Map<String, Object>> data = archivalService.getArchivedData(tableName, queryParams);
        return new ResponseEntity(data, HttpStatus.OK);
    }
}