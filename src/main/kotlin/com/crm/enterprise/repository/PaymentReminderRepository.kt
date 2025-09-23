package com.crm.enterprise.repository

import com.crm.enterprise.entity.PaymentReminder
import com.crm.enterprise.entity.ReminderStatus
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PaymentReminderRepository : CrudRepository<PaymentReminder, Long> {
    
    fun findByCompanyIdAndStatus(companyId: Long, status: ReminderStatus): List<PaymentReminder>
    
    fun findByDealId(dealId: Long): List<PaymentReminder>
    
    fun findByContactId(contactId: Long): List<PaymentReminder>
    
    @Query("""
        SELECT * FROM payment_reminders 
        WHERE due_date < :currentDate 
        AND status = 'PENDING' 
        AND deleted_at IS NULL
    """)
    fun findOverdueReminders(@Param("currentDate") currentDate: LocalDateTime): List<PaymentReminder>
    
    @Query("""
        SELECT * FROM payment_reminders 
        WHERE company_id = :companyId 
        AND due_date < :currentDate 
        AND status = 'PENDING' 
        AND deleted_at IS NULL
    """)
    fun findOverdueRemindersByCompany(
        @Param("companyId") companyId: Long, 
        @Param("currentDate") currentDate: LocalDateTime
    ): List<PaymentReminder>
    
    @Query("""
        SELECT * FROM payment_reminders 
        WHERE company_id = :companyId 
        AND due_date BETWEEN :startDate AND :endDate 
        AND status = 'PENDING' 
        AND deleted_at IS NULL
        ORDER BY due_date ASC
    """)
    fun findUpcomingReminders(
        @Param("companyId") companyId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<PaymentReminder>
    
    @Query("""
        SELECT * FROM payment_reminders 
        WHERE company_id = :companyId 
        AND type = :type 
        AND status = 'PENDING' 
        AND deleted_at IS NULL
        ORDER BY due_date ASC
    """)
    fun findByCompanyIdAndTypeAndStatus(
        @Param("companyId") companyId: Long,
        @Param("type") type: String,
        @Param("status") status: ReminderStatus
    ): List<PaymentReminder>
}
