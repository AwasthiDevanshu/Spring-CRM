package com.crm.enterprise.repository

import com.crm.enterprise.entity.CustomForm
import com.crm.enterprise.entity.FormStatus
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CustomFormRepository : CrudRepository<CustomForm, Long> {
    
    fun findByCompanyIdAndDeletedAtIsNull(companyId: Long): List<CustomForm>
    
    fun findByCompanyIdAndStatusAndDeletedAtIsNull(companyId: Long, status: FormStatus): List<CustomForm>
    
    fun findByCompanyIdAndIsPublicAndDeletedAtIsNull(companyId: Long, isPublic: Boolean): List<CustomForm>
    
    @Query("""
        SELECT * FROM custom_forms 
        WHERE company_id = :companyId 
        AND (expires_at IS NULL OR expires_at > :currentTime)
        AND deleted_at IS NULL
        ORDER BY created_at DESC
    """)
    fun findActiveFormsByCompany(
        @Param("companyId") companyId: Long,
        @Param("currentTime") currentTime: LocalDateTime
    ): List<CustomForm>
    
    @Query("""
        SELECT * FROM custom_forms 
        WHERE id = :formId 
        AND is_public = true 
        AND status = 'ACTIVE'
        AND (expires_at IS NULL OR expires_at > :currentTime)
        AND deleted_at IS NULL
    """)
    fun findPublicFormById(
        @Param("formId") formId: Long,
        @Param("currentTime") currentTime: LocalDateTime
    ): CustomForm?
    
    @Query("""
        SELECT COUNT(*) FROM custom_form_submissions 
        WHERE form_id = :formId
    """)
    fun countSubmissionsByFormId(@Param("formId") formId: Long): Long
}
