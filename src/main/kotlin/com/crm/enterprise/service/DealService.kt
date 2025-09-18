package com.crm.enterprise.service

import com.crm.enterprise.dto.DealRequest
import com.crm.enterprise.dto.DealResponse
import com.crm.enterprise.dto.DealUpdateRequest
import com.crm.enterprise.entity.Deal
import com.crm.enterprise.entity.DealStatus
import com.crm.enterprise.repository.DealRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class DealService(
    private val dealRepository: DealRepository
) {
    
    fun createDeal(dealRequest: DealRequest, companyId: Long): DealResponse {
        val deal = Deal(
            name = dealRequest.name,
            description = dealRequest.description,
            value = dealRequest.value,
            currency = dealRequest.currency,
            status = dealRequest.status,
            probability = dealRequest.probability,
            expectedCloseDate = dealRequest.expectedCloseDate,
            contactId = dealRequest.contactId,
            pipelineId = dealRequest.pipelineId,
            stageId = dealRequest.stageId,
            assignedUserId = dealRequest.assignedUserId,
            companyId = companyId
        )
        
        val savedDeal = dealRepository.save(deal)
        return toDealResponse(savedDeal)
    }
    
    fun getDealById(id: Long, companyId: Long): DealResponse? {
        val deal = dealRepository.findById(id).orElse(null)
        return if (deal != null && deal.companyId == companyId) {
            toDealResponse(deal)
        } else null
    }
    
    fun getDealsByCompany(companyId: Long): List<DealResponse> {
        return dealRepository.findByCompanyId(companyId)
            .map { toDealResponse(it) }
    }
    
    fun getDealsByStatus(companyId: Long, status: DealStatus): List<DealResponse> {
        return dealRepository.findByCompanyIdAndStatus(companyId, status)
            .map { toDealResponse(it) }
    }
    
    fun getDealsByAssignedUser(companyId: Long, assignedUserId: Long): List<DealResponse> {
        return dealRepository.findByCompanyIdAndAssignedUserId(companyId, assignedUserId)
            .map { toDealResponse(it) }
    }
    
    fun getDealsByPipeline(companyId: Long, pipelineId: Long): List<DealResponse> {
        return dealRepository.findByCompanyIdAndPipelineId(companyId, pipelineId)
            .map { toDealResponse(it) }
    }
    
    fun getDealsByStage(companyId: Long, stageId: Long): List<DealResponse> {
        return dealRepository.findByCompanyIdAndStageId(companyId, stageId)
            .map { toDealResponse(it) }
    }
    
    fun updateDeal(id: Long, updateRequest: DealUpdateRequest, companyId: Long): DealResponse? {
        val existingDeal = dealRepository.findById(id).orElse(null)
        if (existingDeal == null || existingDeal.companyId != companyId) {
            return null
        }
        
        val updatedDeal = existingDeal.copy(
            name = updateRequest.name ?: existingDeal.name,
            description = updateRequest.description ?: existingDeal.description,
            value = updateRequest.value ?: existingDeal.value,
            currency = updateRequest.currency ?: existingDeal.currency,
            status = updateRequest.status ?: existingDeal.status,
            probability = updateRequest.probability ?: existingDeal.probability,
            expectedCloseDate = updateRequest.expectedCloseDate ?: existingDeal.expectedCloseDate,
            actualCloseDate = updateRequest.actualCloseDate ?: existingDeal.actualCloseDate,
            contactId = updateRequest.contactId ?: existingDeal.contactId,
            pipelineId = updateRequest.pipelineId ?: existingDeal.pipelineId,
            stageId = updateRequest.stageId ?: existingDeal.stageId,
            assignedUserId = updateRequest.assignedUserId ?: existingDeal.assignedUserId,
            updatedAt = LocalDateTime.now()
        )
        
        val savedDeal = dealRepository.save(updatedDeal)
        return toDealResponse(savedDeal)
    }
    
    fun deleteDeal(id: Long, companyId: Long): Boolean {
        val deal = dealRepository.findById(id).orElse(null)
        return if (deal != null && deal.companyId == companyId) {
            dealRepository.deleteById(id)
            true
        } else false
    }
    
    fun getDealAnalytics(companyId: Long): com.crm.enterprise.controller.DealAnalytics {
        val deals = dealRepository.findByCompanyId(companyId)
        val totalDeals = deals.size
        val openDeals = deals.count { it.status == DealStatus.OPEN }
        val wonDeals = deals.count { it.status == DealStatus.WON }
        val lostDeals = deals.count { it.status == DealStatus.LOST }
        
        val totalValue = deals.sumOf { it.value }
        val wonValue = deals.filter { it.status == DealStatus.WON }.sumOf { it.value }
        val averageDealSize = if (totalDeals > 0) totalValue.divide(BigDecimal(totalDeals)) else BigDecimal.ZERO
        val conversionRate = if (totalDeals > 0) (wonDeals.toDouble() / totalDeals.toDouble()) * 100 else 0.0
        
        return com.crm.enterprise.controller.DealAnalytics(
            totalDeals = totalDeals,
            openDeals = openDeals,
            wonDeals = wonDeals,
            lostDeals = lostDeals,
            totalValue = totalValue,
            wonValue = wonValue,
            averageDealSize = averageDealSize,
            conversionRate = conversionRate
        )
    }
    
    private fun toDealResponse(deal: Deal): DealResponse {
        return DealResponse(
            id = deal.id ?: 0L,
            name = deal.name,
            description = deal.description,
            value = deal.value,
            currency = deal.currency,
            status = deal.status,
            probability = deal.probability,
            expectedCloseDate = deal.expectedCloseDate,
            actualCloseDate = deal.actualCloseDate,
            contactId = deal.contactId,
            pipelineId = deal.pipelineId,
            stageId = deal.stageId,
            assignedUserId = deal.assignedUserId,
            companyId = deal.companyId,
            createdAt = deal.createdAt,
            updatedAt = deal.updatedAt
        )
    }
}