package com.crm.enterprise.repository

import com.crm.enterprise.entity.CustomFormSubmission
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CustomFormSubmissionRepository : CrudRepository<CustomFormSubmission, Long> {
    
    fun findByFormIdOrderByCreatedAtDesc(formId: Long): List<CustomFormSubmission>
    
    fun findByFormIdAndStatusOrderByCreatedAtDesc(formId: Long, status: String): List<CustomFormSubmission>
    
    @Query("""
        SELECT * FROM custom_form_submissions 
        WHERE form_id = :formId 
        AND created_at BETWEEN :startDate AND :endDate
        ORDER BY created_at DESC
    """)
    fun findByFormIdAndDateRange(
        @Param("formId") formId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<CustomFormSubmission>
    
    @Query("""
        SELECT COUNT(*) FROM custom_form_submissions 
        WHERE form_id = :formId 
        AND created_at >= :startDate
    """)
    fun countSubmissionsSince(
        @Param("formId") formId: Long,
        @Param("startDate") startDate: LocalDateTime
    ): Long
    
    @Query("""
        SELECT * FROM custom_form_submissions 
        WHERE form_id = :formId 
        AND submitted_by_email = :email
        ORDER BY created_at DESC
    """)
    fun findByFormIdAndEmail(
        @Param("formId") formId: Long,
        @Param("email") email: String
    ): List<CustomFormSubmission>
}
