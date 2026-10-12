package lk.gamana.service;

import lk.gamana.model.AuditLog;
import lk.gamana.repository.AuditRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {
    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void log(String adminId, String action, String target) {
        auditRepository.log(adminId, action, target);
    }

    public List<AuditLog> findAll() {
        return auditRepository.findAll();
    }

    public List<AuditLog> findRecent(int limit) {
        return auditRepository.findAll().stream().limit(limit).toList();
    }
}
