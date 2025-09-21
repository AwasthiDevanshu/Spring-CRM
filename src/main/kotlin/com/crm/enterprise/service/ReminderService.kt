package com.crm.enterprise.service

import com.crm.enterprise.dto.ReminderFilter
import com.crm.enterprise.dto.ReminderRequest
import com.crm.enterprise.dto.ReminderResponse
import com.crm.enterprise.dto.ReminderUpdateRequest
import com.crm.enterprise.entity.Reminder
import com.crm.enterprise.entity.ReminderStatus
import com.crm.enterprise.entity.ReminderType
import com.crm.enterprise.repository.ReminderRepository
import com.crm.enterprise.repository.UserRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ReminderService(
    private val reminderRepository: ReminderRepository,
    private val userRepository: UserRepository,
    private val auditTrailService: AuditTrailService
) {
    
    fun createReminder(
        request: ReminderRequest,
        assignedBy: Long,
        companyId: Long
    ): ReminderResponse {
        val reminder = Reminder(
            type = request.type,
            title = request.title,
            description = request.description,
            reminderDate = LocalDateTime.parse(request.reminderDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            priority = request.priority,
            assignedTo = request.assignedTo,
            assignedBy = assignedBy,
            entityType = request.entityType,
            entityId = request.entityId,
            activityId = request.activityId,
            companyId = companyId
        )
        
        val saved = reminderRepository.save(reminder)
        
        // Log audit trail
        auditTrailService.logEntityCreation("REMINDER", saved.id!!, saved.title, assignedBy, companyId)
        
        return toResponse(saved)
    }
    
    fun getReminders(companyId: Long, filter: ReminderFilter): List<ReminderResponse> {
        val reminders = when {
            filter.assignedTo != null -> {
                reminderRepository.findByAssignedToAndCompanyIdOrderByReminderDateAsc(filter.assignedTo, companyId)
            }
            filter.status != null -> {
                reminderRepository.findByStatusAndCompanyIdOrderByReminderDateAsc(filter.status, companyId)
            }
            filter.type != null -> {
                reminderRepository.findByTypeAndCompanyIdOrderByReminderDateAsc(filter.type, companyId)
            }
            filter.entityType != null && filter.entityId != null -> {
                reminderRepository.findByEntityTypeAndEntityIdAndCompanyIdOrderByReminderDateAsc(
                    filter.entityType, filter.entityId, companyId
                )
            }
            filter.startDate != null && filter.endDate != null -> {
                reminderRepository.findByCompanyIdAndDateRange(companyId, filter.startDate, filter.endDate)
            }
            else -> {
                reminderRepository.findByCompanyIdOrderByReminderDateAsc(companyId)
            }
        }
        
        return reminders.map { toResponse(it) }
    }
    
    fun getReminderById(id: Long, companyId: Long): ReminderResponse? {
        val reminder = reminderRepository.findByIdAndCompanyIdAndNotDeleted(id, companyId)
        return reminder?.let { toResponse(it) }
    }
    
    fun updateReminder(
        id: Long,
        request: ReminderUpdateRequest,
        companyId: Long,
        performedBy: Long
    ): ReminderResponse? {
        val existingReminder = reminderRepository.findByIdAndCompanyIdAndNotDeleted(id, companyId)
        if (existingReminder == null) {
            return null
        }
        
        val updatedReminder = existingReminder.copy(
            type = request.type ?: existingReminder.type,
            title = request.title ?: existingReminder.title,
            description = request.description ?: existingReminder.description,
            reminderDate = request.reminderDate?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) } ?: existingReminder.reminderDate,
            priority = request.priority ?: existingReminder.priority,
            assignedTo = request.assignedTo ?: existingReminder.assignedTo,
            status = request.status ?: existingReminder.status,
            updatedAt = LocalDateTime.now()
        )
        
        val saved = reminderRepository.save(updatedReminder)
        
        // Log audit trail
        auditTrailService.logEntityUpdate("REMINDER", id, saved.title, "status", existingReminder.status.name, saved.status.name, performedBy, companyId)
        
        return toResponse(saved)
    }
    
    fun deleteReminder(id: Long, companyId: Long, performedBy: Long): Boolean {
        val reminder = reminderRepository.findByIdAndCompanyIdAndNotDeleted(id, companyId)
        return if (reminder != null) {
            auditTrailService.logEntityDeletion("REMINDER", id, reminder.title, performedBy, companyId)
            reminderRepository.softDeleteByIdAndCompanyId(id, companyId)
            true
        } else false
    }
    
    fun acknowledgeReminder(id: Long, companyId: Long, performedBy: Long): Boolean {
        val reminder = reminderRepository.findByIdAndCompanyIdAndNotDeleted(id, companyId)
        return if (reminder != null) {
            val updatedReminder = reminder.copy(
                status = ReminderStatus.ACKNOWLEDGED,
                acknowledgedAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            reminderRepository.save(updatedReminder)
            
            auditTrailService.logActivity(
                com.crm.enterprise.dto.AuditTrailRequest(
                    entityType = "REMINDER",
                    entityId = id,
                    action = "ACKNOWLEDGE",
                    description = "Acknowledged reminder: ${reminder.title}"
                ),
                performedBy,
                companyId
            )
            true
        } else false
    }
    
    fun getPendingReminders(companyId: Long): List<ReminderResponse> {
        val reminders = reminderRepository.findPendingRemindersDueNow(companyId, LocalDateTime.now())
        return reminders.map { toResponse(it) }
    }
    
    fun getPendingRemindersForUser(userId: Long, companyId: Long): List<ReminderResponse> {
        val reminders = reminderRepository.findPendingRemindersForUser(userId, companyId, LocalDateTime.now())
        return reminders.map { toResponse(it) }
    }
    
    @Async
    fun processDueReminders() {
        val dueReminders = reminderRepository.findPendingRemindersDueNow(1L, LocalDateTime.now()) // Assuming companyId = 1 for now
        dueReminders.forEach { reminder ->
            // Here you would typically send notifications (email, push, etc.)
            // For now, we'll just mark them as sent
            val updatedReminder = reminder.copy(
                status = ReminderStatus.SENT,
                sentAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            reminderRepository.save(updatedReminder)
        }
    }
    
    private fun toResponse(reminder: Reminder): ReminderResponse {
        val assignedToUser = userRepository.findById(reminder.assignedTo).orElse(null)
        val assignedByUser = userRepository.findById(reminder.assignedBy).orElse(null)
        
        return ReminderResponse(
            id = reminder.id!!,
            type = reminder.type,
            title = reminder.title,
            description = reminder.description,
            reminderDate = reminder.reminderDate,
            status = reminder.status,
            priority = reminder.priority,
            assignedTo = reminder.assignedTo,
            assignedToName = assignedToUser?.let { "${it.firstName} ${it.lastName}".trim() },
            assignedBy = reminder.assignedBy,
            assignedByName = assignedByUser?.let { "${it.firstName} ${it.lastName}".trim() },
            entityType = reminder.entityType,
            entityId = reminder.entityId,
            entityName = null, // Would need to fetch from respective entity service
            activityId = reminder.activityId,
            companyId = reminder.companyId,
            sentAt = reminder.sentAt,
            acknowledgedAt = reminder.acknowledgedAt,
            createdAt = reminder.createdAt,
            updatedAt = reminder.updatedAt
        )
    }
}
