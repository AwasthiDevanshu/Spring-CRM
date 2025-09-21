package com.crm.enterprise.controller

import com.crm.enterprise.dto.ReminderFilter
import com.crm.enterprise.dto.ReminderRequest
import com.crm.enterprise.dto.ReminderResponse
import com.crm.enterprise.dto.ReminderUpdateRequest
import com.crm.enterprise.entity.ReminderPriority
import com.crm.enterprise.entity.ReminderStatus
import com.crm.enterprise.entity.ReminderType
import com.crm.enterprise.service.ReminderService
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
@RequestMapping("/api/reminders")
@Tag(name = "Reminders Management", description = "Reminder management endpoints for creating, reading, updating, and deleting reminders")
class ReminderController(
    private val reminderService: ReminderService,
    private val requestUtils: RequestUtils
) {
    
    @PostMapping
    @Operation(
        summary = "Create New Reminder",
        description = "Create a new reminder in the CRM system"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Reminder created successfully",
                content = [Content(schema = Schema(implementation = ReminderResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun createReminder(
        @Parameter(description = "Reminder information", required = true)
        @RequestBody reminderRequest: ReminderRequest,
        request: HttpServletRequest
    ): ResponseEntity<ReminderResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        val userId = requestUtils.extractUserIdFromToken(request)
        if (companyId == null || userId == null) {
            return ResponseEntity.badRequest().build()
        }
        val reminder = reminderService.createReminder(reminderRequest, userId, companyId)
        return ResponseEntity.ok(reminder)
    }
    
    @GetMapping
    @Operation(
        summary = "Get Reminders",
        description = "Get reminders with optional filtering"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Reminders retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<ReminderResponse>::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getReminders(
        @Parameter(description = "Reminder type filter", required = false)
        @RequestParam(required = false) type: ReminderType?,
        @Parameter(description = "Reminder status filter", required = false)
        @RequestParam(required = false) status: ReminderStatus?,
        @Parameter(description = "Reminder priority filter", required = false)
        @RequestParam(required = false) priority: ReminderPriority?,
        @Parameter(description = "Assigned to user ID", required = false)
        @RequestParam(required = false) assignedTo: Long?,
        @Parameter(description = "Entity type filter", required = false)
        @RequestParam(required = false) entityType: String?,
        @Parameter(description = "Entity ID filter", required = false)
        @RequestParam(required = false) entityId: Long?,
        @Parameter(description = "Start date (ISO format)", required = false)
        @RequestParam(required = false) startDate: String?,
        @Parameter(description = "End date (ISO format)", required = false)
        @RequestParam(required = false) endDate: String?,
        @Parameter(description = "Page number", required = false)
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size", required = false)
        @RequestParam(defaultValue = "50") size: Int,
        request: HttpServletRequest
    ): ResponseEntity<List<ReminderResponse>> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        
        val filter = ReminderFilter(
            type = type,
            status = status,
            priority = priority,
            assignedTo = assignedTo,
            entityType = entityType,
            entityId = entityId,
            startDate = startDate?.let { java.time.LocalDateTime.parse(it) },
            endDate = endDate?.let { java.time.LocalDateTime.parse(it) },
            page = page,
            size = size
        )
        
        val reminders = reminderService.getReminders(companyId, filter)
        return ResponseEntity.ok(reminders)
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get Reminder by ID",
        description = "Get a specific reminder by its ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Reminder retrieved successfully",
                content = [Content(schema = Schema(implementation = ReminderResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "Reminder not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getReminder(
        @Parameter(description = "Reminder ID", required = true)
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<ReminderResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val reminder = reminderService.getReminderById(id, companyId)
        return if (reminder != null) {
            ResponseEntity.ok(reminder)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Update Reminder",
        description = "Update an existing reminder"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Reminder updated successfully",
                content = [Content(schema = Schema(implementation = ReminderResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "Reminder not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun updateReminder(
        @Parameter(description = "Reminder ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Updated reminder information", required = true)
        @RequestBody updateRequest: ReminderUpdateRequest,
        request: HttpServletRequest
    ): ResponseEntity<ReminderResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        val userId = requestUtils.extractUserIdFromToken(request)
        if (companyId == null || userId == null) {
            return ResponseEntity.badRequest().build()
        }
        val reminder = reminderService.updateReminder(id, updateRequest, companyId, userId)
        return if (reminder != null) {
            ResponseEntity.ok(reminder)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete Reminder",
        description = "Delete a reminder from the system"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Reminder deleted successfully"),
            ApiResponse(responseCode = "404", description = "Reminder not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun deleteReminder(
        @Parameter(description = "Reminder ID", required = true)
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        val userId = requestUtils.extractUserIdFromToken(request)
        if (companyId == null || userId == null) {
            return ResponseEntity.badRequest().build()
        }
        val deleted = reminderService.deleteReminder(id, companyId, userId)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping("/{id}/acknowledge")
    @Operation(
        summary = "Acknowledge Reminder",
        description = "Mark a reminder as acknowledged"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Reminder acknowledged successfully"),
            ApiResponse(responseCode = "404", description = "Reminder not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun acknowledgeReminder(
        @Parameter(description = "Reminder ID", required = true)
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        val userId = requestUtils.extractUserIdFromToken(request)
        if (companyId == null || userId == null) {
            return ResponseEntity.badRequest().build()
        }
        val acknowledged = reminderService.acknowledgeReminder(id, companyId, userId)
        return if (acknowledged) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/pending")
    @Operation(
        summary = "Get Pending Reminders",
        description = "Get all pending reminders that are due now"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Pending reminders retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<ReminderResponse>::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getPendingReminders(
        request: HttpServletRequest
    ): ResponseEntity<List<ReminderResponse>> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val reminders = reminderService.getPendingReminders(companyId)
        return ResponseEntity.ok(reminders)
    }
    
    @GetMapping("/my-pending")
    @Operation(
        summary = "Get My Pending Reminders",
        description = "Get pending reminders assigned to the current user"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User's pending reminders retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<ReminderResponse>::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getMyPendingReminders(
        request: HttpServletRequest
    ): ResponseEntity<List<ReminderResponse>> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        val userId = requestUtils.extractUserIdFromToken(request)
        if (companyId == null || userId == null) {
            return ResponseEntity.badRequest().build()
        }
        val reminders = reminderService.getPendingRemindersForUser(userId, companyId)
        return ResponseEntity.ok(reminders)
    }
}
