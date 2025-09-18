package com.crm.enterprise.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard Analytics", description = "Dashboard analytics and KPI endpoints")
class DashboardController {

    @GetMapping("/stats")
    @Operation(
        summary = "Get Dashboard Statistics",
        description = "Get key performance indicators and statistics for the dashboard"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Statistics retrieved successfully",
                content = [Content(schema = Schema(implementation = DashboardStats::class))]
            )
        ]
    )
    fun getDashboardStats(
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<DashboardStats> {
        // Mock data - in production, this would come from services
        val stats = DashboardStats(
            totalLeads = 156,
            newLeads = 23,
            qualifiedLeads = 45,
            totalContacts = 89,
            totalDeals = 34,
            openDeals = 12,
            wonDeals = 18,
            lostDeals = 4,
            totalRevenue = BigDecimal("125000.00"),
            monthlyRevenue = BigDecimal("25000.00"),
            conversionRate = 15.5,
            averageDealSize = BigDecimal("3676.47"),
            topLeadSources = listOf(
                LeadSourceStats("Website", 45),
                LeadSourceStats("Referral", 32),
                LeadSourceStats("Phone", 28),
                LeadSourceStats("Email", 21)
            ),
            recentActivities = listOf(
                ActivitySummary("New lead: John Doe", "2 hours ago"),
                ActivitySummary("Deal closed: Enterprise License", "4 hours ago"),
                ActivitySummary("Contact updated: Jane Smith", "6 hours ago")
            )
        )
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/leads/chart")
    @Operation(
        summary = "Get Leads Chart Data",
        description = "Get leads data for chart visualization"
    )
    fun getLeadsChartData(
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long,
        @Parameter(description = "Number of days", required = false)
        @RequestParam(defaultValue = "30") days: Int
    ): ResponseEntity<LeadsChartData> {
        // Mock data for chart
        val chartData = LeadsChartData(
            labels = listOf("Week 1", "Week 2", "Week 3", "Week 4"),
            newLeads = listOf(12, 19, 8, 15),
            qualifiedLeads = listOf(8, 12, 6, 10),
            convertedLeads = listOf(3, 5, 2, 4)
        )
        return ResponseEntity.ok(chartData)
    }

    @GetMapping("/revenue/chart")
    @Operation(
        summary = "Get Revenue Chart Data",
        description = "Get revenue data for chart visualization"
    )
    fun getRevenueChartData(
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long,
        @Parameter(description = "Number of months", required = false)
        @RequestParam(defaultValue = "12") months: Int
    ): ResponseEntity<RevenueChartData> {
        // Mock data for revenue chart
        val chartData = RevenueChartData(
            labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
            revenue = listOf(15000, 22000, 18000, 25000, 30000, 28000),
            target = listOf(20000, 20000, 20000, 20000, 20000, 20000)
        )
        return ResponseEntity.ok(chartData)
    }
}

data class DashboardStats(
    val totalLeads: Int,
    val newLeads: Int,
    val qualifiedLeads: Int,
    val totalContacts: Int,
    val totalDeals: Int,
    val openDeals: Int,
    val wonDeals: Int,
    val lostDeals: Int,
    val totalRevenue: BigDecimal,
    val monthlyRevenue: BigDecimal,
    val conversionRate: Double,
    val averageDealSize: BigDecimal,
    val topLeadSources: List<LeadSourceStats>,
    val recentActivities: List<ActivitySummary>
)

data class LeadSourceStats(
    val source: String,
    val count: Int
)

data class ActivitySummary(
    val description: String,
    val timeAgo: String
)

data class LeadsChartData(
    val labels: List<String>,
    val newLeads: List<Int>,
    val qualifiedLeads: List<Int>,
    val convertedLeads: List<Int>
)

data class RevenueChartData(
    val labels: List<String>,
    val revenue: List<Int>,
    val target: List<Int>
)
