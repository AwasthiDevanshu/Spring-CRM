package com.crm.enterprise.repository

import com.crm.enterprise.entity.Activity
import com.crm.enterprise.entity.ActivityStatus
import com.crm.enterprise.entity.ActivityType
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ActivityRepository : CrudRepository<Activity, Long> {
    
    @Query("SELECT * FROM activities WHERE company_id = :companyId")
    fun findByCompanyId(companyId: Long): List<Activity>
    
    @Query("SELECT * FROM activities WHERE company_id = :companyId AND user_id = :userId")
    fun findByCompanyIdAndAssignedUserId(companyId: Long, userId: Long): List<Activity>
    
    @Query("SELECT * FROM activities WHERE company_id = :companyId AND type = :type")
    fun findByCompanyIdAndType(companyId: Long, type: ActivityType): List<Activity>
    
    @Query("SELECT * FROM activities WHERE company_id = :companyId AND activity_date <= :dueDate")
    fun findOverdueActivities(companyId: Long, dueDate: LocalDateTime): List<Activity>
    
    @Query("SELECT * FROM activities WHERE company_id = :companyId AND activity_date BETWEEN :startDate AND :endDate")
    fun findByCompanyIdAndDueDateBetween(companyId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<Activity>
}

