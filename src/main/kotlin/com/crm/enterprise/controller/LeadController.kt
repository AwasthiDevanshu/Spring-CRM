package com.crm.enterprise.controller

import com.crm.enterprise.dto.LeadRequest
import com.crm.enterprise.dto.LeadResponse
import com.crm.enterprise.dto.LeadUpdateRequest
import com.crm.enterprise.entity.LeadStatus
import com.crm.enterprise.service.LeadService
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
@RequestMapping("/api/leads")
@Tag(name = "Leads Management", description = "Lead management endpoints for creating, reading, updating, and deleting leads")
class LeadController(
    private val leadService: LeadService
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
        @RequestBody leadRequest: LeadRequest,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<LeadResponse> {
        return try {
            val lead = leadService.createLead(leadRequest, companyId)
            ResponseEntity.ok(lead)
        } catch (e: Exception) {
            println("Error creating lead: ${e.message}")
            e.printStackTrace()
            ResponseEntity.badRequest().build()
        }
    }
    
    @GetMapping
    fun getLeads(
        @RequestParam companyId: Long,
        @RequestParam(required = false) status: LeadStatus?,
        @RequestParam(required = false) assignedUserId: Long?,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<List<LeadResponse>> {
        return try {
            val leads = when {
                status != null -> leadService.getLeadsByStatus(companyId, status)
                assignedUserId != null -> leadService.getLeadsByAssignedUser(companyId, assignedUserId)
                !search.isNullOrBlank() -> leadService.searchLeads(companyId, search)
                else -> leadService.getLeadsByCompany(companyId)
            }
            ResponseEntity.ok(leads)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @GetMapping("/{id}")
    fun getLead(
        @PathVariable id: Long,
        @RequestParam companyId: Long
    ): ResponseEntity<LeadResponse> {
        return try {
            val lead = leadService.getLeadById(id, companyId)
            if (lead != null) {
                ResponseEntity.ok(lead)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @PutMapping("/{id}")
    fun updateLead(
        @PathVariable id: Long,
        @RequestBody updateRequest: LeadUpdateRequest,
        @RequestParam companyId: Long
    ): ResponseEntity<LeadResponse> {
        return try {
            val lead = leadService.updateLead(id, updateRequest, companyId)
            if (lead != null) {
                ResponseEntity.ok(lead)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteLead(
        @PathVariable id: Long,
        @RequestParam companyId: Long
    ): ResponseEntity<Void> {
        return try {
            val deleted = leadService.deleteLead(id, companyId)
            if (deleted) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
}

