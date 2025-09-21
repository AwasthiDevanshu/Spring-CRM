package com.crm.enterprise.controller

import com.crm.enterprise.dto.LeadRequest
import com.crm.enterprise.dto.LeadResponse
import com.crm.enterprise.dto.LeadUpdateRequest
import com.crm.enterprise.entity.LeadStatus
import com.crm.enterprise.service.LeadService
import com.crm.enterprise.util.RequestUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/leads")
@Tag(name = "Leads Management", description = "Lead management endpoints for creating, reading, updating, and deleting leads")
class LeadController(
    private val leadService: LeadService,
    private val requestUtils: RequestUtils
) {
    
    @PostMapping
    @Operation(
        summary = "Create New Lead",
        description = "Create a new lead in the CRM system"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Lead created successfully",
                content = [Content(schema = Schema(implementation = LeadResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data"
            )
        ]
    )
    fun createLead(
        @Parameter(description = "Lead information", required = true)
        @Valid @RequestBody leadRequest: LeadRequest,
        request: HttpServletRequest
    ): ResponseEntity<LeadResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        val userId = requestUtils.extractUserIdFromToken(request)
        if (companyId == null || userId == null) {
            return ResponseEntity.badRequest().build()
        }
        val lead = leadService.createLead(leadRequest, companyId, userId)
        return ResponseEntity.ok(lead)
    }
    
    @GetMapping
    fun getLeads(
        request: HttpServletRequest,
        @RequestParam(required = false) status: LeadStatus?,
        @RequestParam(required = false) assignedUserId: Long?,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<List<LeadResponse>> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val leads = when {
            status != null -> leadService.getLeadsByStatus(companyId, status)
            assignedUserId != null -> leadService.getLeadsByAssignedUser(companyId, assignedUserId)
            !search.isNullOrBlank() -> leadService.searchLeads(companyId, search)
            else -> leadService.getLeadsByCompany(companyId)
        }
        return ResponseEntity.ok(leads)
    }
    
    @GetMapping("/{id}")
    fun getLead(
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<LeadResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val lead = leadService.getLeadById(id, companyId)
        return if (lead != null) {
            ResponseEntity.ok(lead)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PutMapping("/{id}")
    fun updateLead(
        @PathVariable id: Long,
        @RequestBody updateRequest: LeadUpdateRequest,
        request: HttpServletRequest
    ): ResponseEntity<LeadResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        val userId = requestUtils.extractUserIdFromToken(request)
        if (companyId == null || userId == null) {
            return ResponseEntity.badRequest().build()
        }
        val lead = leadService.updateLead(id, updateRequest, companyId, userId)
        return if (lead != null) {
            ResponseEntity.ok(lead)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteLead(
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        val userId = requestUtils.extractUserIdFromToken(request)
        if (companyId == null || userId == null) {
            return ResponseEntity.badRequest().build()
        }
        val deleted = leadService.deleteLead(id, companyId, userId)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

