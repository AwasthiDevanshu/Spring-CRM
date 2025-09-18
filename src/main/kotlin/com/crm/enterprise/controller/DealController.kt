package com.crm.enterprise.controller

import com.crm.enterprise.dto.DealRequest
import com.crm.enterprise.dto.DealResponse
import com.crm.enterprise.dto.DealUpdateRequest
import com.crm.enterprise.entity.DealStatus
import com.crm.enterprise.service.DealService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/deals")
@Tag(name = "Deals Management", description = "Deal management endpoints for creating, reading, updating, and deleting deals")
class DealController(
    private val dealService: DealService
) {
    
    @PostMapping
    @Operation(
        summary = "Create New Deal",
        description = "Create a new deal in the CRM system"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Deal created successfully",
                content = [Content(schema = Schema(implementation = DealResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data"
            )
        ]
    )
    fun createDeal(
        @Parameter(description = "Deal information", required = true)
        @RequestBody dealRequest: DealRequest,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<DealResponse> {
        return try {
            val deal = dealService.createDeal(dealRequest, companyId)
            ResponseEntity.ok(deal)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @GetMapping
    @Operation(
        summary = "Get Deals",
        description = "Get deals with optional filtering by status, assigned user, pipeline, or stage"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Deals retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<DealResponse>::class))]
            )
        ]
    )
    fun getDeals(
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long,
        @Parameter(description = "Deal status", required = false)
        @RequestParam(required = false) status: DealStatus?,
        @Parameter(description = "Assigned user ID", required = false)
        @RequestParam(required = false) assignedUserId: Long?,
        @Parameter(description = "Pipeline ID", required = false)
        @RequestParam(required = false) pipelineId: Long?,
        @Parameter(description = "Stage ID", required = false)
        @RequestParam(required = false) stageId: Long?
    ): ResponseEntity<List<DealResponse>> {
        return try {
            val deals = when {
                status != null -> dealService.getDealsByStatus(companyId, status)
                assignedUserId != null -> dealService.getDealsByAssignedUser(companyId, assignedUserId)
                pipelineId != null -> dealService.getDealsByPipeline(companyId, pipelineId)
                stageId != null -> dealService.getDealsByStage(companyId, stageId)
                else -> dealService.getDealsByCompany(companyId)
            }
            ResponseEntity.ok(deals)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get Deal by ID",
        description = "Get a specific deal by its ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Deal retrieved successfully",
                content = [Content(schema = Schema(implementation = DealResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Deal not found"
            )
        ]
    )
    fun getDeal(
        @Parameter(description = "Deal ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<DealResponse> {
        return try {
            val deal = dealService.getDealById(id, companyId)
            if (deal != null) {
                ResponseEntity.ok(deal)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Update Deal",
        description = "Update an existing deal"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Deal updated successfully",
                content = [Content(schema = Schema(implementation = DealResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Deal not found"
            )
        ]
    )
    fun updateDeal(
        @Parameter(description = "Deal ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Updated deal information", required = true)
        @RequestBody updateRequest: DealUpdateRequest,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<DealResponse> {
        return try {
            val deal = dealService.updateDeal(id, updateRequest, companyId)
            if (deal != null) {
                ResponseEntity.ok(deal)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete Deal",
        description = "Delete a deal from the system"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Deal deleted successfully"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Deal not found"
            )
        ]
    )
    fun deleteDeal(
        @Parameter(description = "Deal ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<Void> {
        return try {
            val deleted = dealService.deleteDeal(id, companyId)
            if (deleted) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @GetMapping("/analytics/summary")
    @Operation(
        summary = "Get Deal Analytics Summary",
        description = "Get summary statistics for deals"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Analytics retrieved successfully",
                content = [Content(schema = Schema(implementation = DealAnalytics::class))]
            )
        ]
    )
    fun getDealAnalytics(
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<DealAnalytics> {
        return try {
            val analytics = dealService.getDealAnalytics(companyId)
            ResponseEntity.ok(analytics)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
}

data class DealAnalytics(
    val totalDeals: Int,
    val openDeals: Int,
    val wonDeals: Int,
    val lostDeals: Int,
    val totalValue: java.math.BigDecimal,
    val wonValue: java.math.BigDecimal,
    val averageDealSize: java.math.BigDecimal,
    val conversionRate: Double
)
