package com.crm.enterprise.repository

import com.crm.enterprise.entity.CustomField
import org.springframework.data.repository.CrudRepository
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository

@Repository
interface CustomFieldRepository : CrudRepository<CustomField, Long> {

    @Query("SELECT * FROM custom_fields WHERE company_id = :companyId AND entity_type = :entityType AND is_active = true ORDER BY display_order ASC")
    fun findByCompanyIdAndEntityType(companyId: Long, entityType: String): List<CustomField>

    @Query("SELECT * FROM custom_fields WHERE company_id = :companyId AND is_active = true ORDER BY entity_type, display_order ASC")
    fun findByCompanyId(companyId: Long): List<CustomField>

    @Query("SELECT * FROM custom_fields WHERE company_id = :companyId AND field_name = :fieldName AND entity_type = :entityType")
    fun findByCompanyIdAndFieldNameAndEntityType(companyId: Long, fieldName: String, entityType: String): CustomField?

    @Query("SELECT * FROM custom_fields WHERE company_id = :companyId AND entity_type = :entityType AND field_name = :fieldName")
    fun findByCompanyIdAndEntityTypeAndFieldName(companyId: Long, entityType: String, fieldName: String): CustomField?
}
