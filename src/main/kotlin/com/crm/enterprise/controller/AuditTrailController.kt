package com.crm.enterprise.controller

import com.crm.enterprise.dto.AuditTrailFilter
import com.crm.enterprise.dto.AuditTrailResponse
import com.crm.enterprise.dto.AuditTrailSummary
import com.crm.enterprise.service.AuditTrailService
import com.crm.enterprise.util.RequestUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit Trail", description = "Audit trail and activity logging endpoints")
class AuditTrailController(
    private val auditTrailService: AuditTrailService,
    private val requestUtils: RequestUtils
) {
    
    @GetMapping
    @Operation(
        summary = "Get Audit Trails",
        description = "Get audit trail entries with optional filtering"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Audit trails retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<AuditTrailResponse>::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getAuditTrails(
        @Parameter(description = "Entity type filter", required = false)
        @RequestParam(required = false) entityType: String?,
        @Parameter(description = "Entity ID filter", required = false)
        @RequestParam(required = false) entityId: Long?,
        @Parameter(description = "Action filter", required = false)
        @RequestParam(required = false) action: String?,
        @Parameter(description = "Performed by user ID", required = false)
        @RequestParam(required = false) performedBy: Long?,
        @Parameter(description = "Start date (ISO format)", required = false)
        @RequestParam(required = false) startDate: String?,
        @Parameter(description = "End date (ISO format)", required = false)
        @RequestParam(required = false) endDate: String?,
        @Parameter(description = "Page number", required = false)
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size", required = false)
        @RequestParam(defaultValue = "50") size: Int,
        request: HttpServletRequest
    ): ResponseEntity<List<AuditTrailResponse>> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        
        val filter = AuditTrailFilter(
            entityType = entityType,
            entityId = entityId,
            action = action,
            performedBy = performedBy,
            startDate = startDate?.let { java.time.LocalDateTime.parse(it) },
            endDate = endDate?.let { java.time.LocalDateTime.parse(it) },
            page = page,
            size = size
        )
        
        val auditTrails = auditTrailService.getAuditTrails(companyId, filter)
        return ResponseEntity.ok(auditTrails)
    }
    
    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(
        summary = "Get Entity Audit Trail",
        description = "Get audit trail for a specific entity"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Entity audit trail retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<AuditTrailResponse>::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getEntityAuditTrail(
        @Parameter(description = "Entity type", required = true)
        @PathVariable entityType: String,
        @Parameter(description = "Entity ID", required = true)
        @PathVariable entityId: Long,
        request: HttpServletRequest
    ): ResponseEntity<List<AuditTrailResponse>> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        
        val auditTrails = auditTrailService.getEntityAuditTrail(entityType, entityId, companyId)
        return ResponseEntity.ok(auditTrails)
    }
    
    @GetMapping("/summary")
    @Operation(
        summary = "Get Audit Summary",
        description = "Get audit trail summary statistics"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Audit summary retrieved successfully",
                content = [Content(schema = Schema(implementation = AuditTrailSummary::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getAuditSummary(
        request: HttpServletRequest
    ): ResponseEntity<AuditTrailSummary> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        
        val summary = auditTrailService.getAuditSummary(companyId)
        return ResponseEntity.ok(summary)
    }
    
    @GetMapping("/recent")
    @Operation(
        summary = "Get Recent Activities",
        description = "Get recent audit trail activities"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Recent activities retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<AuditTrailResponse>::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getRecentActivities(
        @Parameter(description = "Number of recent activities to retrieve", required = false)
        @RequestParam(defaultValue = "20") limit: Int,
        request: HttpServletRequest
    ): ResponseEntity<List<AuditTrailResponse>> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        
        val activities = auditTrailService.getRecentActivities(companyId, limit)
        return ResponseEntity.ok(activities)
    }
}
