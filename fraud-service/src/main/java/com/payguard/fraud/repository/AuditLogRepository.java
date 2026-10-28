package com.payguard.fraud.repository;

import com.payguard.fraud.entity.FraudAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<FraudAuditLog, UUID> {
}
