package com.crm.enterprise.controller

import com.crm.enterprise.dto.CompanyRequest
import com.crm.enterprise.dto.CompanyResponse
import com.crm.enterprise.dto.CompanyUpdateRequest
import com.crm.enterprise.dto.CompanyStatsResponse
import com.crm.enterprise.service.CompanyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Company Management", description = "Company management endpoints for creating, reading, updating, and deleting companies")
class CompanyController(
    private val companyService: CompanyService
) {
    
    @PostMapping
    @Operation(
        summary = "Create New Company",
        description = "Create a new company in the CRM system"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Company created successfully",
                content = [Content(schema = Schema(implementation = CompanyResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data"
            )
        ]
    )
    fun createCompany(
        @Parameter(description = "Company information", required = true)
        @Valid @RequestBody companyRequest: CompanyRequest
    ): ResponseEntity<CompanyResponse> {
        val company = companyService.createCompany(companyRequest)
        return ResponseEntity.ok(company)
    }
    
    @GetMapping
    @Operation(
        summary = "Get All Companies",
        description = "Get all companies in the system"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Companies retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<CompanyResponse>::class))]
            )
        ]
    )
    fun getAllCompanies(): ResponseEntity<List<CompanyResponse>> {
        val companies = companyService.getAllCompanies()
        return ResponseEntity.ok(companies)
    }
    
    @GetMapping("/active")
    @Operation(
        summary = "Get Active Companies",
        description = "Get all active companies in the system"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Active companies retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<CompanyResponse>::class))]
            )
        ]
    )
    fun getActiveCompanies(): ResponseEntity<List<CompanyResponse>> {
        val companies = companyService.getActiveCompanies()
        return ResponseEntity.ok(companies)
    }
    
    @GetMapping("/search")
    @Operation(
        summary = "Search Companies",
        description = "Search companies by name"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Companies retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<CompanyResponse>::class))]
            )
        ]
    )
    fun searchCompanies(
        @Parameter(description = "Search term", required = true)
        @RequestParam search: String
    ): ResponseEntity<List<CompanyResponse>> {
        val companies = companyService.searchCompanies(search)
        return ResponseEntity.ok(companies)
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get Company by ID",
        description = "Get a specific company by its ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Company retrieved successfully",
                content = [Content(schema = Schema(implementation = CompanyResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Company not found"
            )
        ]
    )
    fun getCompany(
        @Parameter(description = "Company ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<CompanyResponse> {
        val company = companyService.getCompanyById(id)
        return if (company != null) {
            ResponseEntity.ok(company)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/{id}/stats")
    @Operation(
        summary = "Get Company Statistics",
        description = "Get comprehensive statistics for a company including user count, leads, contacts, deals, and revenue"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Company statistics retrieved successfully",
                content = [Content(schema = Schema(implementation = CompanyStatsResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Company not found"
            )
        ]
    )
    fun getCompanyStats(
        @Parameter(description = "Company ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<CompanyStatsResponse> {
        val stats = companyService.getCompanyStats(id)
        return if (stats != null) {
            ResponseEntity.ok(stats)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Update Company",
        description = "Update an existing company"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Company updated successfully",
                content = [Content(schema = Schema(implementation = CompanyResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Company not found"
            )
        ]
    )
    fun updateCompany(
        @Parameter(description = "Company ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Updated company information", required = true)
        @Valid @RequestBody updateRequest: CompanyUpdateRequest
    ): ResponseEntity<CompanyResponse> {
        val company = companyService.updateCompany(id, updateRequest)
        return if (company != null) {
            ResponseEntity.ok(company)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete Company",
        description = "Delete a company from the system"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Company deleted successfully"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Company not found"
            )
        ]
    )
    fun deleteCompany(
        @Parameter(description = "Company ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        val deleted = companyService.deleteCompany(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
