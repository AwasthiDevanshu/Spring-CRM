package com.crm.enterprise.controller

import com.crm.enterprise.dto.ActivityRequest
import com.crm.enterprise.dto.ActivityResponse
import com.crm.enterprise.dto.ActivityUpdateRequest
import com.crm.enterprise.service.ActivityService
import com.crm.enterprise.util.RequestUtils
import jakarta.servlet.http.HttpServletRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/activities")
@Tag(name = "Activities Management", description = "Activity management endpoints for creating, reading, updating, and deleting activities")
class ActivityController(
    private val activityService: ActivityService,
    private val requestUtils: RequestUtils
) {
    
    private val logger = LoggerFactory.getLogger(ActivityController::class.java)
    
    @GetMapping
    @Operation(
        summary = "Get Activities",
        description = "Get all activities for a company with optional filtering"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Activities retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<ActivityResponse>::class))]
            )
        ]
    )
    fun getActivities(
        @Parameter(description = "Assigned user ID")
        @RequestParam(required = false) assignedUserId: Long? = null,
        @Parameter(description = "Limit number of results")
        @RequestParam(required = false) limit: Int? = null,
        @Parameter(description = "Page number for pagination")
        @RequestParam(required = false) page: Int? = null,
        request: HttpServletRequest
    ): ResponseEntity<List<ActivityResponse>> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        val userId = requestUtils.extractUserIdFromToken(request)
        val isSuperuser = requestUtils.extractIsSuperuserFromToken(request)
        val isCompanyAdmin = requestUtils.extractIsCompanyAdminFromToken(request)
        
        if (companyId == null || userId == null) {
            return ResponseEntity.badRequest().build()
        }
            
        val activities = when {
            // If specific user requested, get their activities
            assignedUserId != null -> {
                activityService.findByCompanyIdAndAssignedUserId(companyId, assignedUserId)
            }
            // If admin or superuser, get all company activities
            isSuperuser == true || isCompanyAdmin == true -> {
                activityService.findByCompanyId(companyId)
            }
            // Regular user gets only their assigned activities
            else -> {
                activityService.findByCompanyIdAndAssignedUserId(companyId, userId)
            }
        }
        
        return ResponseEntity.ok(activities)
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get Activity by ID",
        description = "Get a specific activity by its ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Activity retrieved successfully",
                content = [Content(schema = Schema(implementation = ActivityResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Activity not found"
            )
        ]
    )
    fun getActivity(
        @Parameter(description = "Activity ID", required = true)
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<ActivityResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
            ?: return ResponseEntity.badRequest().build()
            
        val activity = activityService.findById(id, companyId)
        return if (activity != null) {
            ResponseEntity.ok(activity)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping
    @Operation(
        summary = "Create New Activity",
        description = "Create a new activity in the CRM system"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Activity created successfully",
                content = [Content(schema = Schema(implementation = ActivityResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data"
            )
        ]
    )
    fun createActivity(
        @Parameter(description = "Activity information", required = true)
        @RequestBody activityRequest: ActivityRequest,
        request: HttpServletRequest
    ): ResponseEntity<ActivityResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
            ?: return ResponseEntity.badRequest().build()
        val userId = requestUtils.extractUserIdFromToken(request)
            ?: return ResponseEntity.badRequest().build()
            
        return try {
            val activity = activityService.createActivity(activityRequest, companyId, userId)
            ResponseEntity.ok(activity)
        } catch (e: Exception) {
            logger.error("Error creating activity: {}", e.message, e)
            ResponseEntity.badRequest().build()
        }
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Update Activity",
        description = "Update an existing activity"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Activity updated successfully",
                content = [Content(schema = Schema(implementation = ActivityResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Activity not found"
            )
        ]
    )
    fun updateActivity(
        @Parameter(description = "Activity ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Updated activity information", required = true)
        @RequestBody updateRequest: ActivityUpdateRequest,
        request: HttpServletRequest
    ): ResponseEntity<ActivityResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
            ?: return ResponseEntity.badRequest().build()
            
        val updatedActivity = activityService.updateActivity(id, updateRequest, companyId)
        return if (updatedActivity != null) {
            ResponseEntity.ok(updatedActivity)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete Activity",
        description = "Delete an activity by its ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Activity deleted successfully"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Activity not found"
            )
        ]
    )
    fun deleteActivity(
        @Parameter(description = "Activity ID", required = true)
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
            ?: return ResponseEntity.badRequest().build()
            
        val deleted = activityService.deleteActivity(id, companyId)
        return if (deleted) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/overdue")
    @Operation(
        summary = "Get Overdue Activities",
        description = "Get all overdue activities for a company"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Overdue activities retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<ActivityResponse>::class))]
            )
        ]
    )
    fun getOverdueActivities(
        request: HttpServletRequest
    ): ResponseEntity<List<ActivityResponse>> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
            ?: return ResponseEntity.badRequest().build()
            
        val activities = activityService.findOverdueActivities(companyId)
        return ResponseEntity.ok(activities)
    }
}
