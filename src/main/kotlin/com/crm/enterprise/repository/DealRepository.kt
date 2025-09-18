package com.crm.enterprise.repository

import com.crm.enterprise.entity.Deal
import com.crm.enterprise.entity.DealStatus
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface DealRepository : CrudRepository<Deal, Long> {
    
    @Query("SELECT * FROM deals WHERE company_id = :companyId")
    fun findByCompanyId(companyId: Long): List<Deal>
    
    @Query("SELECT * FROM deals WHERE company_id = :companyId AND status = :status")
    fun findByCompanyIdAndStatus(companyId: Long, status: DealStatus): List<Deal>
    
    @Query("SELECT * FROM deals WHERE company_id = :companyId AND assigned_user_id = :assignedUserId")
    fun findByCompanyIdAndAssignedUserId(companyId: Long, assignedUserId: Long): List<Deal>
    
    @Query("SELECT * FROM deals WHERE company_id = :companyId AND pipeline_id = :pipelineId")
    fun findByCompanyIdAndPipelineId(companyId: Long, pipelineId: Long): List<Deal>
    
    @Query("SELECT * FROM deals WHERE company_id = :companyId AND stage_id = :stageId")
    fun findByCompanyIdAndStageId(companyId: Long, stageId: Long): List<Deal>
    
    @Query("SELECT * FROM deals WHERE company_id = :companyId AND contact_id = :contactId")
    fun findByCompanyIdAndContactId(companyId: Long, contactId: Long): List<Deal>
    
    @Query("SELECT * FROM deals WHERE company_id = :companyId ORDER BY value DESC")
    fun findByCompanyIdOrderByValueDesc(companyId: Long): List<Deal>
    
    @Query("SELECT SUM(value) FROM deals WHERE company_id = :companyId AND status = :status")
    fun sumValueByCompanyIdAndStatus(companyId: Long, status: DealStatus): BigDecimal?
}

