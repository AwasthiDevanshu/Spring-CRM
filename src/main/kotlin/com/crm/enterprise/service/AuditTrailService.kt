package com.crm.enterprise.service

import com.crm.enterprise.dto.AuditTrailFilter
import com.crm.enterprise.dto.AuditTrailRequest
import com.crm.enterprise.dto.AuditTrailResponse
import com.crm.enterprise.dto.AuditTrailSummary
import com.crm.enterprise.entity.AuditTrail
import com.crm.enterprise.repository.AuditTrailRepository
import com.crm.enterprise.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuditTrailService(
    private val auditTrailRepository: AuditTrailRepository,
    private val userRepository: UserRepository
) {
    
    fun logActivity(
        request: AuditTrailRequest,
        performedBy: Long,
        companyId: Long
    ): AuditTrailResponse {
        val auditTrail = AuditTrail(
            entityType = request.entityType,
            entityId = request.entityId,
            action = request.action,
            fieldName = request.fieldName,
            oldValue = request.oldValue,
            newValue = request.newValue,
            description = request.description,
            performedBy = performedBy,
            companyId = companyId,
            ipAddress = request.ipAddress,
            userAgent = request.userAgent
        )
        
        val saved = auditTrailRepository.save(auditTrail)
        return toResponse(saved)
    }
    
    fun getAuditTrails(companyId: Long, filter: AuditTrailFilter): List<AuditTrailResponse> {
        
        val trails = when {
            filter.entityType != null && filter.entityId != null -> {
                auditTrailRepository.findByEntityTypeAndEntityIdAndCompanyIdOrderByCreatedAtDesc(
                    filter.entityType, filter.entityId, companyId
                )
            }
            filter.entityType != null -> {
                auditTrailRepository.findByEntityTypeAndCompanyIdOrderByCreatedAtDesc(
                    filter.entityType, companyId
                )
            }
            filter.performedBy != null -> {
                auditTrailRepository.findByPerformedByAndCompanyIdOrderByCreatedAtDesc(
                    filter.performedBy, companyId
                )
            }
            filter.action != null -> {
                auditTrailRepository.findByActionAndCompanyIdOrderByCreatedAtDesc(
                    filter.action, companyId
                )
            }
            filter.startDate != null && filter.endDate != null -> {
                auditTrailRepository.findByCompanyIdAndDateRange(
                    companyId, filter.startDate, filter.endDate
                )
            }
            else -> {
                auditTrailRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
            }
        }
        
        return trails.map { toResponse(it) }
    }
    
    fun getEntityAuditTrail(entityType: String, entityId: Long, companyId: Long): List<AuditTrailResponse> {
        val trails = auditTrailRepository.findByEntityTypeAndEntityIdAndCompanyIdOrderByCreatedAtDesc(
            entityType, entityId, companyId
        )
        return trails.map { toResponse(it) }
    }
    
    fun getAuditSummary(companyId: Long): AuditTrailSummary {
        val allTrails = auditTrailRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
        
        val activitiesByType = allTrails.groupBy { it.entityType }
            .mapValues { it.value.size.toLong() }
        
        val activitiesByAction = allTrails.groupBy { it.action }
            .mapValues { it.value.size.toLong() }
        
        val recentActivities = allTrails.take(10).map { toResponse(it) }
        
        return AuditTrailSummary(
            totalActivities = allTrails.size.toLong(),
            activitiesByType = activitiesByType,
            activitiesByAction = activitiesByAction,
            recentActivities = recentActivities
        )
    }
    
    fun getRecentActivities(companyId: Long, limit: Int = 20): List<AuditTrailResponse> {
        val trails = auditTrailRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
            .take(limit)
        return trails.map { toResponse(it) }
    }
    
    private fun toResponse(auditTrail: AuditTrail): AuditTrailResponse {
        val user = userRepository.findById(auditTrail.performedBy).orElse(null)
        val performedByName = user?.let { "${it.firstName} ${it.lastName}".trim() }
        
        return AuditTrailResponse(
            id = auditTrail.id!!,
            entityType = auditTrail.entityType,
            entityId = auditTrail.entityId,
            action = auditTrail.action,
            fieldName = auditTrail.fieldName,
            oldValue = auditTrail.oldValue,
            newValue = auditTrail.newValue,
            description = auditTrail.description,
            performedBy = auditTrail.performedBy,
            performedByName = performedByName,
            companyId = auditTrail.companyId,
            ipAddress = auditTrail.ipAddress,
            userAgent = auditTrail.userAgent,
            createdAt = auditTrail.createdAt
        )
    }
    
    // Helper methods for common audit actions
    fun logEntityCreation(entityType: String, entityId: Long, entityName: String, performedBy: Long, companyId: Long) {
        val request = AuditTrailRequest(
            entityType = entityType,
            entityId = entityId,
            action = "CREATE",
            description = "Created $entityType: $entityName"
        )
        logActivity(request, performedBy, companyId)
    }
    
    fun logEntityUpdate(entityType: String, entityId: Long, entityName: String, fieldName: String, oldValue: String?, newValue: String?, performedBy: Long, companyId: Long) {
        val request = AuditTrailRequest(
            entityType = entityType,
            entityId = entityId,
            action = "UPDATE",
            fieldName = fieldName,
            oldValue = oldValue,
            newValue = newValue,
            description = "Updated $entityType: $entityName - $fieldName changed from '$oldValue' to '$newValue'"
        )
        logActivity(request, performedBy, companyId)
    }
    
    fun logEntityDeletion(entityType: String, entityId: Long, entityName: String, performedBy: Long, companyId: Long) {
        val request = AuditTrailRequest(
            entityType = entityType,
            entityId = entityId,
            action = "DELETE",
            description = "Deleted $entityType: $entityName"
        )
        logActivity(request, performedBy, companyId)
    }
    
    fun logStatusChange(entityType: String, entityId: Long, entityName: String, oldStatus: String, newStatus: String, performedBy: Long, companyId: Long) {
        val request = AuditTrailRequest(
            entityType = entityType,
            entityId = entityId,
            action = "STATUS_CHANGE",
            fieldName = "status",
            oldValue = oldStatus,
            newValue = newStatus,
            description = "Changed $entityType status: $entityName from '$oldStatus' to '$newStatus'"
        )
        logActivity(request, performedBy, companyId)
    }
    
    fun logAssignment(entityType: String, entityId: Long, entityName: String, assignedTo: String, performedBy: Long, companyId: Long) {
        val request = AuditTrailRequest(
            entityType = entityType,
            entityId = entityId,
            action = "ASSIGN",
            fieldName = "assignedTo",
            newValue = assignedTo,
            description = "Assigned $entityType: $entityName to $assignedTo"
        )
        logActivity(request, performedBy, companyId)
    }
}
