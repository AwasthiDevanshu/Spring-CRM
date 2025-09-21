package com.crm.enterprise.controller

import com.crm.enterprise.service.LeadService
import com.crm.enterprise.service.DealService
import com.crm.enterprise.service.ActivityService
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
import java.math.BigDecimal
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Analytics and reporting endpoints")
class ReportsController(
    private val leadService: LeadService,
    private val dealService: DealService,
    private val activityService: ActivityService,
    private val requestUtils: RequestUtils
) {

    @GetMapping("/leads-performance")
    @Operation(
        summary = "Get Leads Performance Report",
        description = "Get leads performance analytics including conversion rates and sources"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Leads performance data retrieved successfully",
                content = [Content(schema = Schema(implementation = LeadsPerformanceReport::class))]
            )
        ]
    )
    fun getLeadsPerformanceReport(
        @Parameter(description = "Time range in days", required = false)
        @RequestParam(defaultValue = "30") days: Int,
        request: HttpServletRequest
    ): ResponseEntity<LeadsPerformanceReport> {
        return try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
            if (companyId == null) {
                return ResponseEntity.badRequest().build()
            }
            val report = leadService.getLeadsPerformanceReport(companyId, days)
            ResponseEntity.ok(report)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/sales-pipeline")
    @Operation(
        summary = "Get Sales Pipeline Report",
        description = "Get sales pipeline analytics including deal progression and revenue"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Sales pipeline data retrieved successfully",
                content = [Content(schema = Schema(implementation = SalesPipelineReport::class))]
            )
        ]
    )
    fun getSalesPipelineReport(
        @Parameter(description = "Time range in days", required = false)
        @RequestParam(defaultValue = "30") days: Int,
        request: HttpServletRequest
    ): ResponseEntity<SalesPipelineReport> {
        return try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
            if (companyId == null) {
                return ResponseEntity.badRequest().build()
            }
            val report = dealService.getSalesPipelineReport(companyId, days)
            ResponseEntity.ok(report)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/activity-summary")
    @Operation(
        summary = "Get Activity Summary Report",
        description = "Get team activity and productivity metrics"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Activity summary data retrieved successfully",
                content = [Content(schema = Schema(implementation = ActivitySummaryReport::class))]
            )
        ]
    )
    fun getActivitySummaryReport(
        @Parameter(description = "Time range in days", required = false)
        @RequestParam(defaultValue = "30") days: Int,
        request: HttpServletRequest
    ): ResponseEntity<ActivitySummaryReport> {
        return try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
            if (companyId == null) {
                return ResponseEntity.badRequest().build()
            }
            val report = activityService.getActivitySummaryReport(companyId, days)
            ResponseEntity.ok(report)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/revenue-forecast")
    @Operation(
        summary = "Get Revenue Forecast Report",
        description = "Get revenue forecasting and growth predictions"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Revenue forecast data retrieved successfully",
                content = [Content(schema = Schema(implementation = RevenueForecastReport::class))]
            )
        ]
    )
    fun getRevenueForecastReport(
        request: HttpServletRequest
    ): ResponseEntity<RevenueForecastReport> {
        return try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
            if (companyId == null) {
                return ResponseEntity.badRequest().build()
            }
            val report = dealService.getRevenueForecastReport(companyId)
            ResponseEntity.ok(report)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/quick-stats")
    @Operation(
        summary = "Get Quick Stats",
        description = "Get quick statistics for the reports overview"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Quick stats retrieved successfully",
                content = [Content(schema = Schema(implementation = QuickStatsReport::class))]
            )
        ]
    )
    fun getQuickStats(
        request: HttpServletRequest
    ): ResponseEntity<QuickStatsReport> {
        return try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
            if (companyId == null) {
                return ResponseEntity.badRequest().build()
            }
            val stats = QuickStatsReport(
                thisMonth = MonthStats(
                    newLeads = leadService.getNewLeadsThisMonth(companyId),
                    dealsClosed = dealService.getDealsClosedThisMonth(companyId),
                    revenue = dealService.getRevenueThisMonth(companyId)
                ),
                teamPerformance = TeamPerformance(
                    topPerformer = "John Smith", // This would come from user service
                    activitiesLogged = activityService.getTotalActivitiesThisMonth(companyId),
                    responseRate = 68.0 // This would be calculated
                ),
                leadSources = leadService.getLeadSourcesDistribution(companyId)
            )
            ResponseEntity.ok(stats)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
}

// Data classes for report responses
data class LeadsPerformanceReport(
    val totalLeads: Int,
    val conversionRate: Double,
    val qualifiedLeads: Int,
    val averageLeadScore: Double,
    val periodComparison: PeriodComparison
)

data class SalesPipelineReport(
    val totalDeals: Int,
    val wonDeals: Int,
    val totalRevenue: BigDecimal,
    val averageDealSize: BigDecimal,
    val periodComparison: PeriodComparison
)

data class ActivitySummaryReport(
    val totalActivities: Int,
    val callsMade: Int,
    val emailsSent: Int,
    val meetingsHeld: Int,
    val periodComparison: PeriodComparison
)

data class RevenueForecastReport(
    val forecastedRevenue: BigDecimal,
    val pipelineValue: BigDecimal,
    val winProbability: Double,
    val growthRate: Double
)

data class QuickStatsReport(
    val thisMonth: MonthStats,
    val teamPerformance: TeamPerformance,
    val leadSources: LeadSourcesDistribution
)

data class PeriodComparison(
    val changePercent: Double,
    val changeType: String // "increase" or "decrease"
)

data class MonthStats(
    val newLeads: Int,
    val dealsClosed: Int,
    val revenue: BigDecimal
)

data class TeamPerformance(
    val topPerformer: String,
    val activitiesLogged: Int,
    val responseRate: Double
)

data class LeadSourcesDistribution(
    val website: Double,
    val referrals: Double,
    val socialMedia: Double,
    val other: Double
)
