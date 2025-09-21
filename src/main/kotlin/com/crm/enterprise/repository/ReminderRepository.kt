package com.crm.enterprise.repository

import com.crm.enterprise.entity.Reminder
import com.crm.enterprise.entity.ReminderStatus
import com.crm.enterprise.entity.ReminderType
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ReminderRepository : CrudRepository<Reminder, Long> {
    
    fun findByCompanyIdOrderByReminderDateAsc(companyId: Long): List<Reminder>
    
    fun findByAssignedToAndCompanyIdOrderByReminderDateAsc(assignedTo: Long, companyId: Long): List<Reminder>
    
    fun findByStatusAndCompanyIdOrderByReminderDateAsc(status: ReminderStatus, companyId: Long): List<Reminder>
    
    fun findByTypeAndCompanyIdOrderByReminderDateAsc(type: ReminderType, companyId: Long): List<Reminder>
    
    fun findByEntityTypeAndEntityIdAndCompanyIdOrderByReminderDateAsc(
        entityType: String, 
        entityId: Long, 
        companyId: Long
    ): List<Reminder>
    
    fun findByActivityIdAndCompanyIdOrderByReminderDateAsc(activityId: Long, companyId: Long): List<Reminder>
    
    @Query("""
        SELECT * FROM reminders 
        WHERE company_id = :companyId 
        AND reminder_date >= :startDate 
        AND reminder_date <= :endDate 
        AND deleted_at IS NULL
        ORDER BY reminder_date ASC
    """)
    fun findByCompanyIdAndDateRange(
        @Param("companyId") companyId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Reminder>
    
    @Query("""
        SELECT * FROM reminders 
        WHERE company_id = :companyId 
        AND status = 'PENDING' 
        AND reminder_date <= :currentTime 
        AND deleted_at IS NULL
        ORDER BY reminder_date ASC
    """)
    fun findPendingRemindersDueNow(
        @Param("companyId") companyId: Long,
        @Param("currentTime") currentTime: LocalDateTime
    ): List<Reminder>
    
    @Query("""
        SELECT * FROM reminders 
        WHERE assigned_to = :assignedTo 
        AND company_id = :companyId 
        AND status = 'PENDING' 
        AND reminder_date <= :currentTime 
        AND deleted_at IS NULL
        ORDER BY reminder_date ASC
    """)
    fun findPendingRemindersForUser(
        @Param("assignedTo") assignedTo: Long,
        @Param("companyId") companyId: Long,
        @Param("currentTime") currentTime: LocalDateTime
    ): List<Reminder>
    
    // Soft delete methods
    @Query("SELECT * FROM reminders WHERE id = :id AND company_id = :companyId AND deleted_at IS NULL")
    fun findByIdAndCompanyIdAndNotDeleted(id: Long, companyId: Long): Reminder?
    
    @Query("SELECT * FROM reminders WHERE company_id = :companyId AND deleted_at IS NOT NULL")
    fun findDeletedByCompanyId(companyId: Long): List<Reminder>
    
    @Query("UPDATE reminders SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id AND company_id = :companyId")
    fun softDeleteByIdAndCompanyId(id: Long, companyId: Long): Int
    
    @Query("UPDATE reminders SET deleted_at = NULL WHERE id = :id AND company_id = :companyId")
    fun restoreByIdAndCompanyId(id: Long, companyId: Long): Int
    
    fun countByCompanyId(companyId: Long): Long
    
    fun countByAssignedToAndCompanyId(assignedTo: Long, companyId: Long): Long
}
