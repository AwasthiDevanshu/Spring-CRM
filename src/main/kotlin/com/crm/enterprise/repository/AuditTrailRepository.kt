package com.crm.enterprise.repository

import com.crm.enterprise.entity.AuditTrail
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AuditTrailRepository : CrudRepository<AuditTrail, Long> {
    
    fun findByCompanyIdOrderByCreatedAtDesc(companyId: Long): List<AuditTrail>
    
    fun findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType: String, entityId: Long): List<AuditTrail>
    
    fun findByEntityTypeAndEntityIdAndCompanyIdOrderByCreatedAtDesc(
        entityType: String, 
        entityId: Long, 
        companyId: Long
    ): List<AuditTrail>
    
    fun findByPerformedByAndCompanyIdOrderByCreatedAtDesc(performedBy: Long, companyId: Long): List<AuditTrail>
    
    fun findByActionAndCompanyIdOrderByCreatedAtDesc(action: String, companyId: Long): List<AuditTrail>
    
    fun findByEntityTypeAndCompanyIdOrderByCreatedAtDesc(entityType: String, companyId: Long): List<AuditTrail>
    
    @Query("""
        SELECT * FROM audit_trails 
        WHERE company_id = :companyId 
        AND created_at >= :startDate 
        AND created_at <= :endDate 
        ORDER BY created_at DESC
    """)
    fun findByCompanyIdAndDateRange(
        @Param("companyId") companyId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<AuditTrail>
    
    @Query("""
        SELECT * FROM audit_trails 
        WHERE company_id = :companyId 
        AND entity_type = :entityType 
        AND created_at >= :startDate 
        AND created_at <= :endDate 
        ORDER BY created_at DESC
    """)
    fun findByCompanyIdAndEntityTypeAndDateRange(
        @Param("companyId") companyId: Long,
        @Param("entityType") entityType: String,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<AuditTrail>
    
    fun countByCompanyId(companyId: Long): Long
    
    fun countByEntityTypeAndEntityId(entityType: String, entityId: Long): Long
}
