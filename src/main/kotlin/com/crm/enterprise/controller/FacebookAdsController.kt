// Temporarily disabled due to Facebook SDK compilation issues
/*
package com.crm.enterprise.controller

import com.crm.enterprise.dto.FacebookAdsIntegrationRequest
import com.crm.enterprise.dto.FacebookAdsIntegrationResponse
import com.crm.enterprise.dto.FacebookAdsSyncResponse
import com.crm.enterprise.entity.FacebookAdsLead
import com.crm.enterprise.service.FacebookAdsService
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
@RequestMapping("/api/facebook-ads")
@Tag(name = "Facebook Ads Integration", description = "Facebook Ads integration endpoints for lead synchronization")
class FacebookAdsController(
    private val facebookAdsService: FacebookAdsService
) {

    @PostMapping("/integrations")
    @Operation(
        summary = "Create Facebook Ads Integration",
        description = "Set up Facebook Ads integration for automatic lead synchronization"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Integration created successfully",
                content = [Content(schema = Schema(implementation = FacebookAdsIntegrationResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid Facebook credentials or request data"
            )
        ]
    )
    fun createIntegration(
        @Parameter(description = "Integration configuration", required = true)
        @RequestBody request: FacebookAdsIntegrationRequest,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<FacebookAdsIntegrationResponse> {
        return try {
            val integration = facebookAdsService.createIntegration(request, companyId)
            ResponseEntity.ok(integration)
        } catch (e: Exception) {
            println("Error creating Facebook Ads integration: ${e.message}")
            e.printStackTrace()
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/integrations")
    @Operation(
        summary = "Get Facebook Ads Integrations",
        description = "Get all Facebook Ads integrations for a company"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Integrations retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<FacebookAdsIntegrationResponse>::class))]
            )
        ]
    )
    fun getIntegrations(
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<List<FacebookAdsIntegrationResponse>> {
        val integrations = facebookAdsService.getIntegrations(companyId)
        return ResponseEntity.ok(integrations)
    }

    @GetMapping("/integrations/{id}")
    @Operation(
        summary = "Get Facebook Ads Integration by ID",
        description = "Get a specific Facebook Ads integration by its ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Integration retrieved successfully",
                content = [Content(schema = Schema(implementation = FacebookAdsIntegrationResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Integration not found"
            )
        ]
    )
    fun getIntegration(
        @Parameter(description = "Integration ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<FacebookAdsIntegrationResponse> {
        val integration = facebookAdsService.getIntegration(id, companyId)
        return if (integration != null) {
            ResponseEntity.ok(integration)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/integrations/{id}")
    @Operation(
        summary = "Update Facebook Ads Integration",
        description = "Update an existing Facebook Ads integration"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Integration updated successfully",
                content = [Content(schema = Schema(implementation = FacebookAdsIntegrationResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Integration not found"
            )
        ]
    )
    fun updateIntegration(
        @Parameter(description = "Integration ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Updated integration configuration", required = true)
        @RequestBody request: FacebookAdsIntegrationRequest,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<FacebookAdsIntegrationResponse> {
        val updatedIntegration = facebookAdsService.updateIntegration(id, request, companyId)
        return if (updatedIntegration != null) {
            ResponseEntity.ok(updatedIntegration)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/integrations/{id}")
    @Operation(
        summary = "Delete Facebook Ads Integration",
        description = "Delete a Facebook Ads integration"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Integration deleted successfully"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Integration not found"
            )
        ]
    )
    fun deleteIntegration(
        @Parameter(description = "Integration ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<Void> {
        val deleted = facebookAdsService.deleteIntegration(id, companyId)
        return if (deleted) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/integrations/{id}/sync")
    @Operation(
        summary = "Sync Facebook Ads Leads",
        description = "Manually trigger synchronization of leads from Facebook Ads"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Sync completed successfully",
                content = [Content(schema = Schema(implementation = FacebookAdsSyncResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Integration not found"
            )
        ]
    )
    fun syncLeads(
        @Parameter(description = "Integration ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<FacebookAdsSyncResponse> {
        return try {
            val syncResponse = facebookAdsService.syncLeads(id, companyId)
            ResponseEntity.ok(syncResponse)
        } catch (e: Exception) {
            println("Error syncing Facebook Ads leads: ${e.message}")
            e.printStackTrace()
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/leads/process")
    @Operation(
        summary = "Process New Facebook Ads Leads",
        description = "Process new Facebook Ads leads and create CRM leads"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Leads processed successfully",
                content = [Content(schema = Schema(implementation = FacebookAdsSyncResponse::class))]
            )
        ]
    )
    fun processNewLeads(
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<FacebookAdsSyncResponse> {
        val syncResponse = facebookAdsService.processNewLeads(companyId)
        return ResponseEntity.ok(syncResponse)
    }

    @GetMapping("/leads")
    @Operation(
        summary = "Get Facebook Ads Leads",
        description = "Get all Facebook Ads leads for a company"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Leads retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<FacebookAdsLead>::class))]
            )
        ]
    )
    fun getLeads(
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<List<FacebookAdsLead>> {
        val leads = facebookAdsService.getLeads(companyId)
        return ResponseEntity.ok(leads)
    }

    @GetMapping("/test-connection")
    @Operation(
        summary = "Test Facebook Ads Connection",
        description = "Test Facebook Ads API connection with provided credentials"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Connection test completed"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Connection test failed"
            )
        ]
    )
    fun testConnection(
        @Parameter(description = "Facebook Access Token", required = true)
        @RequestParam accessToken: String,
        @Parameter(description = "Facebook Ad Account ID", required = true)
        @RequestParam adAccountId: String
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val context = com.facebook.ads.sdk.APIContext(accessToken)
            val adAccount = com.facebook.ads.sdk.AdAccount(adAccountId, context)
            val accountInfo = adAccount.get().execute()
            
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Connection successful",
                "accountName" to accountInfo.getName(),
                "accountId" to accountInfo.id
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to "Connection failed: ${e.message}"
            ))
        }
    }
}
*/
