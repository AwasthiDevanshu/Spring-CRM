package com.crm.enterprise.repository

import com.crm.enterprise.entity.Note
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NoteRepository : CrudRepository<Note, Long> {
    
    @Query("SELECT * FROM notes WHERE entity_type = :entityType AND entity_id = :entityId ORDER BY created_at DESC")
    fun findByEntityTypeAndEntityId(entityType: String, entityId: Long): List<Note>
    
    @Query("SELECT * FROM notes WHERE company_id = :companyId ORDER BY created_at DESC")
    fun findByCompanyId(companyId: Long): List<Note>
    
    @Query("SELECT * FROM notes WHERE created_by = :userId ORDER BY created_at DESC")
    fun findByCreatedBy(userId: Long): List<Note>
}
