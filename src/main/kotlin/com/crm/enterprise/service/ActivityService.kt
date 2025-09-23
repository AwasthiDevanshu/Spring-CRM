package com.crm.enterprise.service

import com.crm.enterprise.controller.ActivitySummaryReport
import com.crm.enterprise.dto.ActivityRequest
import com.crm.enterprise.dto.ActivityResponse
import com.crm.enterprise.dto.ActivityUpdateRequest
import com.crm.enterprise.entity.Activity
import com.crm.enterprise.entity.ActivityType
import com.crm.enterprise.repository.ActivityRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ActivityService(

    private val userService: UserService,
    private val activityRepository: ActivityRepository
) {
    
    fun getActivitySummaryReport(companyId: Long, days: Int): ActivitySummaryReport {
        val cutoffDate = LocalDateTime
            .now()
            .minusDays(days.toLong())
        val allActivities = activityRepository.findByCompanyId(companyId)
        val periodActivities = allActivities.filter { it.createdAt.isAfter(cutoffDate) }
        val previousPeriodActivities = allActivities.filter { 
            it.createdAt.isAfter(cutoffDate.minusDays(days.toLong())) &&
            it.createdAt.isBefore(cutoffDate) 
        }
        
        val totalActivities = periodActivities.size
        val callsMade = periodActivities.count { it.type == ActivityType.CALL }
        val emailsSent = periodActivities.count { it.type == ActivityType.EMAIL }
        val meetingsHeld = periodActivities.count { it.type == ActivityType.MEETING }
        
        val previousTotalActivities = previousPeriodActivities.size
        val changePercent = if (previousTotalActivities > 0) {
            ((totalActivities - previousTotalActivities).toDouble() / previousTotalActivities) * 100
        } else 0.0
        
        return ActivitySummaryReport(
            totalActivities = totalActivities,
            callsMade = callsMade,
            emailsSent = emailsSent,
            meetingsHeld = meetingsHeld,
            periodComparison = com.crm.enterprise.controller.PeriodComparison(
                changePercent = changePercent,
                changeType = if (changePercent >= 0) "increase" else "decrease"
            )
        )
    }
    
    fun getTotalActivitiesThisMonth(companyId: Long): Int {
        val startOfMonth = LocalDateTime
            .now()
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
        return activityRepository.findByCompanyId(companyId)
            .count { it.createdAt.isAfter(startOfMonth) }
    }
    
    // Additional methods needed by ActivityController
    fun findByCompanyId(companyId: Long): List<ActivityResponse> {
        return activityRepository.findByCompanyId(companyId)
            .map { toActivityResponse(it) }
    }
    
    fun findByCompanyIdAndAssignedUserId(companyId: Long, assignedUserId: Long): List<ActivityResponse> {
        return activityRepository.findByCompanyId(companyId)
            .filter { it.assignedTo == assignedUserId }
            .map { toActivityResponse(it) }
    }
    
    fun findById(id: Long, companyId: Long): ActivityResponse? {
        val activity = activityRepository.findById(id).orElse(null)
        return if (activity != null && activity.companyId == companyId) {
            toActivityResponse(activity)
        } else null
    }
    
    fun createActivity(activityRequest: ActivityRequest, companyId: Long, createdByUserId: Long): ActivityResponse {
        var activityAssignedTo =  activityRequest.assignedTo;
        if (activityAssignedTo == 0L){
            activityAssignedTo = createdByUserId;
        }
        val activity = Activity(
            type = activityRequest.type,
            subject = activityRequest.subject,
            description = activityRequest.description,
            outcome = activityRequest.outcome,
            duration = activityRequest.duration,
            assignedTo = activityAssignedTo, // Default to creator if not provided
            assignedBy = createdByUserId, // Set to the user who created the activity
            entityType = activityRequest.entityType,
            entityId = activityRequest.entityId,
            companyId = companyId,
            activityDate = if (activityRequest.dueDate != null)
                LocalDateTime.parse(activityRequest.dueDate) else LocalDateTime.now(),
            dueDate = if (activityRequest.dueDate != null)
                LocalDateTime.parse(activityRequest.dueDate) else null
        )
        
        val savedActivity = activityRepository.save(activity)
        return toActivityResponse(savedActivity)
    }
    
    fun updateActivity(id: Long, updateRequest: ActivityUpdateRequest, companyId: Long): ActivityResponse? {
        val existingActivity = activityRepository.findById(id).orElse(null)
        if (existingActivity == null || existingActivity.companyId != companyId) {
            return null
        }
        
        val updatedActivity = existingActivity.copy(
            type = updateRequest.type ?: existingActivity.type,
            subject = updateRequest.subject ?: existingActivity.subject,
            description = updateRequest.description ?: existingActivity.description,
            outcome = updateRequest.outcome ?: existingActivity.outcome,
            duration = updateRequest.duration ?: existingActivity.duration,
            assignedTo = updateRequest.assignedTo ?: existingActivity.assignedTo,
            activityDate = if (updateRequest.dueDate != null)
                LocalDateTime.parse(updateRequest.dueDate) else existingActivity.activityDate,
            dueDate = if (updateRequest.dueDate != null)
                LocalDateTime.parse(updateRequest.dueDate) else existingActivity.dueDate,
            updatedAt = LocalDateTime.now()
        )
        
        val savedActivity = activityRepository.save(updatedActivity)
        return toActivityResponse(savedActivity)
    }
    
    fun deleteActivity(id: Long, companyId: Long): Boolean {
        val activity = activityRepository.findById(id).orElse(null)
        return if (activity != null && activity.companyId == companyId) {
            activityRepository.deleteById(id)
            true
        } else false
    }
    
    fun findOverdueActivities(companyId: Long): List<ActivityResponse> {
        val now = LocalDateTime.now()
        return activityRepository.findByCompanyId(companyId)
            .filter { it.activityDate.isBefore(now) }
            .map { toActivityResponse(it) }
    }
    
    private fun toActivityResponse(activity: Activity): ActivityResponse {
        val assignedByUser = userService.findByIdAndCompanyId(activity.assignedBy, activity.companyId)
        val assignedToUser = userService.findByIdAndCompanyId(activity.assignedTo, activity.companyId)
        return ActivityResponse(
            id = activity.id ?: 0L,
            type = activity.type,
            subject = activity.subject,
            description = activity.description,
            status = activity.status,
            priority = activity.priority,
            dueDate = activity.dueDate,
            completedAt = activity.completedAt,
            assignedTo = activity.assignedTo,
            assignedToName = assignedToUser?.fullName,
            assignedBy = activity.assignedBy,
            assignedByName = assignedByUser?.fullName,
            entityType = activity.entityType,
            entityId = activity.entityId,
            entityName = null, // TODO: Fetch entity name from respective service
            outcome = activity.outcome,
            duration = activity.duration,
            companyId = activity.companyId,
            activityDate = activity.activityDate,
            createdAt = activity.createdAt,
            updatedAt = activity.updatedAt
        )
    }
}