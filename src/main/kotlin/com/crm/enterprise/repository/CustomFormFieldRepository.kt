package com.crm.enterprise.repository

import com.crm.enterprise.entity.CustomFormField
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CustomFormFieldRepository : CrudRepository<CustomFormField, Long> {
    
    fun findByFormIdAndIsActiveTrueOrderByOrderAsc(formId: Long): List<CustomFormField>
    
    fun findByFormIdOrderByOrderAsc(formId: Long): List<CustomFormField>
    
    @Query("""
        SELECT * FROM custom_form_fields 
        WHERE form_id = :formId 
        AND is_active = true 
        ORDER BY `order` ASC
    """)
    fun findActiveFieldsByFormId(@Param("formId") formId: Long): List<CustomFormField>
}
