package com.crm.enterprise.controller

import com.crm.enterprise.dto.CustomFieldConfiguration
import com.crm.enterprise.dto.CustomFieldRequest
import com.crm.enterprise.dto.CustomFieldResponse
import com.crm.enterprise.dto.CustomFieldUpdateRequest
import com.crm.enterprise.service.CustomFieldService
import com.crm.enterprise.util.RequestUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/custom-fields")
@Tag(name = "Custom Fields Management", description = "Custom field management endpoints for creating, reading, updating, and deleting custom fields")
class CustomFieldController(
    private val customFieldService: CustomFieldService,
    private val requestUtils: RequestUtils
) {
    
    private val logger = LoggerFactory.getLogger(CustomFieldController::class.java)

    @GetMapping("/configuration/{entityType}")
    @Operation(
        summary = "Get Custom Field Configuration",
        description = "Get custom field configuration for a specific entity type (LEAD, CONTACT, DEAL, etc.)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Custom field configuration retrieved successfully",
                content = [Content(schema = Schema(implementation = CustomFieldConfiguration::class))]
            )
        ]
    )
    fun getFieldConfiguration(
        @Parameter(description = "Entity type (LEAD, CONTACT, DEAL, etc.)", required = true)
        @PathVariable entityType: String,
        request: HttpServletRequest
    ): ResponseEntity<CustomFieldConfiguration> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val configuration = customFieldService.getFieldConfiguration(companyId, entityType)
        return ResponseEntity.ok(configuration)
    }

    @GetMapping
    @Operation(
        summary = "Get All Custom Fields",
        description = "Get all custom fields for a company"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Custom fields retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<CustomFieldResponse>::class))]
            )
        ]
    )
    fun getAllCustomFields(
        request: HttpServletRequest
    ): ResponseEntity<List<CustomFieldResponse>> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val fields = customFieldService.getAllCustomFields(companyId)
        return ResponseEntity.ok(fields)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get Custom Field by ID",
        description = "Get a specific custom field by its ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Custom field retrieved successfully",
                content = [Content(schema = Schema(implementation = CustomFieldResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Custom field not found"
            )
        ]
    )
    fun getCustomField(
        @Parameter(description = "Custom field ID", required = true)
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<CustomFieldResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val field = customFieldService.getCustomFieldById(id, companyId)
        return if (field != null) {
            ResponseEntity.ok(field)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    @Operation(
        summary = "Create Custom Field",
        description = "Create a new custom field"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Custom field created successfully",
                content = [Content(schema = Schema(implementation = CustomFieldResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data or field name already exists"
            )
        ]
    )
    fun createCustomField(
        @Parameter(description = "Custom field information", required = true)
        @RequestBody request: CustomFieldRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<CustomFieldResponse> {
        return try {
            val companyId = requestUtils.extractCompanyIdFromToken(httpRequest)
            if (companyId == null) {
                return ResponseEntity.badRequest().build()
            }
            val field = customFieldService.createCustomField(request, companyId)
            ResponseEntity.ok(field)
        } catch (e: Exception) {
            logger.error("Error creating custom field: {}", e.message, e)
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update Custom Field",
        description = "Update an existing custom field"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Custom field updated successfully",
                content = [Content(schema = Schema(implementation = CustomFieldResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Custom field not found"
            )
        ]
    )
    fun updateCustomField(
        @Parameter(description = "Custom field ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Updated custom field information", required = true)
        @RequestBody request: CustomFieldUpdateRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<CustomFieldResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(httpRequest)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val updatedField = customFieldService.updateCustomField(id, request, companyId)
        return if (updatedField != null) {
            ResponseEntity.ok(updatedField)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete Custom Field",
        description = "Delete a custom field by its ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Custom field deleted successfully"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Custom field not found"
            )
        ]
    )
    fun deleteCustomField(
        @Parameter(description = "Custom field ID", required = true)
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val deleted = customFieldService.deleteCustomField(id, companyId)
        return if (deleted) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
