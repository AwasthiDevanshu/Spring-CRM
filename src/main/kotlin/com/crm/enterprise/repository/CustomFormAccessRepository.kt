package com.crm.enterprise.repository

import com.crm.enterprise.entity.CustomFormAccess
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CustomFormAccessRepository : CrudRepository<CustomFormAccess, Long> {
    
    fun findByAccessTokenAndIsActiveTrue(accessToken: String): CustomFormAccess?
    
    fun findByFormIdAndLeadIdAndIsActiveTrue(formId: Long, leadId: Long): CustomFormAccess?
    
    fun findByFormIdAndContactIdAndIsActiveTrue(formId: Long, contactId: Long): CustomFormAccess?
    
    fun findByFormIdAndIsActiveTrue(formId: Long): List<CustomFormAccess>
    
    fun findByCompanyIdAndIsActiveTrue(companyId: Long): List<CustomFormAccess>
    
    @Query("""
        SELECT * FROM custom_form_access 
        WHERE form_id = :formId 
        AND (lead_id = :leadId OR contact_id = :contactId)
        AND is_active = true
        AND (expires_at IS NULL OR expires_at > :currentTime)
    """)
    fun findActiveAccessByFormAndEntity(
        @Param("formId") formId: Long,
        @Param("leadId") leadId: Long?,
        @Param("contactId") contactId: Long?,
        @Param("currentTime") currentTime: LocalDateTime
    ): CustomFormAccess?
    
    @Query("""
        SELECT * FROM custom_form_access 
        WHERE access_token = :accessToken 
        AND is_active = true
        AND (expires_at IS NULL OR expires_at > :currentTime)
    """)
    fun findActiveAccessByToken(
        @Param("accessToken") accessToken: String,
        @Param("currentTime") currentTime: LocalDateTime
    ): CustomFormAccess?
    
    @Query("""
        SELECT * FROM custom_form_access 
        WHERE form_id = :formId 
        AND lead_id = :leadId
        AND is_active = true
        ORDER BY created_at DESC
    """)
    fun findLeadAccessHistory(
        @Param("formId") formId: Long,
        @Param("leadId") leadId: Long
    ): List<CustomFormAccess>
    
    @Query("""
        SELECT * FROM custom_form_access 
        WHERE form_id = :formId 
        AND contact_id = :contactId
        AND is_active = true
        ORDER BY created_at DESC
    """)
    fun findContactAccessHistory(
        @Param("formId") formId: Long,
        @Param("contactId") contactId: Long
    ): List<CustomFormAccess>
    
    fun findByCreatedAtBeforeAndSubmissionCount(createdAt: LocalDateTime, submissionCount: Int): List<CustomFormAccess>
}
