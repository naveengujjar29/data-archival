package com.archival.archivalservice.apprepository;

import com.archival.archivalservice.appmodels.UserTableAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTableAssignmentRepository extends JpaRepository<UserTableAssignment, Long> {

    Optional<UserTableAssignment> findByUserName(String userName);
}