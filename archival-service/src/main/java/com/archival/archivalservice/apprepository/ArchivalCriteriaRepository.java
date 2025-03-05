package com.archival.archivalservice.apprepository;

import com.archival.archivalservice.appmodels.ArchivalConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArchivalCriteriaRepository extends JpaRepository<ArchivalConfiguration, String> {
    Optional<ArchivalConfiguration> findByTableName(String tableName);
}
