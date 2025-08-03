package com.batuhan.feedback360.repository;

import com.batuhan.feedback360.model.entitiy.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
}
