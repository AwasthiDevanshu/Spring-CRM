package com.crm.enterprise.controller

import com.crm.enterprise.service.DatabaseShutdownService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/crm/api/database")
@Tag(name = "Database Management", description = "Database connection management endpoints")
class DatabaseManagementController {

    @Autowired
    private lateinit var databaseShutdownService: DatabaseShutdownService

    @GetMapping("/status")
    @Operation(summary = "Get connection pool status", description = "Returns current database connection pool statistics")
    fun getConnectionPoolStatus(): ResponseEntity<Map<String, Any>> {
        return try {
            val status = databaseShutdownService.getConnectionPoolStatus()
            ResponseEntity.ok(status)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("error" to (e.message ?: "Unknown error")))
        }
    }

    @PostMapping("/cleanup")
    @Operation(summary = "Force cleanup connections", description = "Manually trigger database connection cleanup")
    fun forceCleanup(): ResponseEntity<Map<String, String>> {
        return try {
            databaseShutdownService.forceCleanup()
            ResponseEntity.ok(mapOf("message" to "Database cleanup initiated"))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("error" to (e.message ?: "Unknown error")))
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Database health check", description = "Check if database is accessible")
    fun healthCheck(): ResponseEntity<Map<String, Any>> {
        return try {
            val status = databaseShutdownService.getConnectionPoolStatus()
            val isHealthy = !status.containsKey("error") && 
                           status.get("isClosed") == false &&
                           (status.get("totalConnections") as? Int ?: 0) >= 0
            
            ResponseEntity.ok(mapOf(
                "healthy" to isHealthy,
                "status" to status,
                "timestamp" to System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "healthy" to false,
                "error" to (e.message ?: "Unknown error"),
                "timestamp" to System.currentTimeMillis()
            ))
        }
    }
}
