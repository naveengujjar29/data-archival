package com.archival.archivalservice.apprepository;

import com.archival.archivalservice.appmodels.ArchivalConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchivalCriteriaRepository extends JpaRepository<ArchivalConfiguration, String> {
}
