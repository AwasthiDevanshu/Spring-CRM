package com.crm.enterprise.service

import com.crm.enterprise.dto.ActivityRequest
import com.crm.enterprise.dto.ActivityResponse
import com.crm.enterprise.dto.ActivityUpdateRequest
import com.crm.enterprise.entity.Activity
import com.crm.enterprise.entity.ActivityStatus
import com.crm.enterprise.entity.ActivityPriority
import com.crm.enterprise.repository.ActivityRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class ActivityService(
    private val activityRepository: ActivityRepository
) {
    
    fun findByCompanyId(companyId: Long): List<ActivityResponse> {
        return activityRepository.findByCompanyId(companyId)
            .map { toActivityResponse(it) }
    }
    
    fun findByCompanyIdAndAssignedUserId(companyId: Long, assignedUserId: Long): List<ActivityResponse> {
        return activityRepository.findByCompanyIdAndAssignedUserId(companyId, assignedUserId)
            .map { toActivityResponse(it) }
    }
    
    fun findById(id: Long): ActivityResponse? {
        return activityRepository.findById(id)
            .map { toActivityResponse(it) }
            .orElse(null)
    }
    
    fun createActivity(activityRequest: ActivityRequest, companyId: Long): ActivityResponse {
        val activity = Activity(
            type = activityRequest.type,
            subject = activityRequest.subject,
            description = activityRequest.description,
            userId = 1L, // Default to admin user for now
            companyId = companyId,
            activityDate = activityRequest.dueDate?.let { parseDateTimeString(it) } ?: LocalDateTime.now()
        )
        
        val savedActivity = activityRepository.save(activity)
        return toActivityResponse(savedActivity)
    }
    
    private fun parseDateTimeString(dateTimeStr: String): LocalDateTime {
        return try {
            // Try to parse as ISO datetime first (2025-09-18T14:30:00)
            LocalDateTime.parse(dateTimeStr)
        } catch (e: Exception) {
            try {
                // Try to parse as date only and add default time (2025-09-18)
                val date = LocalDate.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE)
                date.atTime(9, 0) // Default to 9:00 AM
            } catch (e2: Exception) {
                // Fallback to current time if parsing fails
                LocalDateTime.now()
            }
        }
    }
    
    fun updateActivity(id: Long, updateRequest: ActivityUpdateRequest, companyId: Long): ActivityResponse? {
        return activityRepository.findById(id)
            .filter { it.companyId == companyId }
            .map { existingActivity ->
                val updatedActivity = existingActivity.copy(
                    type = updateRequest.type ?: existingActivity.type,
                    subject = updateRequest.subject ?: existingActivity.subject,
                    description = updateRequest.description ?: existingActivity.description,
                    activityDate = updateRequest.dueDate?.let { parseDateTimeString(it) } ?: existingActivity.activityDate,
                    updatedAt = LocalDateTime.now()
                )
                
                val savedActivity = activityRepository.save(updatedActivity)
                toActivityResponse(savedActivity)
            }
            .orElse(null)
    }
    
    fun deleteActivity(id: Long, companyId: Long): Boolean {
        return activityRepository.findById(id)
            .filter { it.companyId == companyId }
            .map {
                activityRepository.deleteById(id)
                true
            }
            .orElse(false)
    }
    
    fun findOverdueActivities(companyId: Long): List<ActivityResponse> {
        return activityRepository.findOverdueActivities(companyId, LocalDateTime.now())
            .map { toActivityResponse(it) }
    }
    
    fun findByCompanyIdAndDueDateBetween(companyId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<ActivityResponse> {
        return activityRepository.findByCompanyIdAndDueDateBetween(companyId, startDate, endDate)
            .map { toActivityResponse(it) }
    }
    
    private fun toActivityResponse(activity: Activity): ActivityResponse {
        return ActivityResponse(
            id = activity.id ?: 0L,
            type = activity.type,
            subject = activity.subject,
            description = activity.description,
            status = ActivityStatus.PENDING, // Default status
            priority = ActivityPriority.MEDIUM, // Default priority
            dueDate = activity.activityDate,
            completedDate = null,
            assignedUserId = activity.userId,
            relatedEntityType = null,
            relatedEntityId = null,
            companyId = activity.companyId,
            createdAt = activity.createdAt,
            updatedAt = activity.updatedAt
        )
    }
}
