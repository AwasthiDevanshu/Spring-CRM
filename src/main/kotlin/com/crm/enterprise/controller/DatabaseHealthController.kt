package com.crm.enterprise.controller

import com.crm.enterprise.service.ConnectionCleanupService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/health/database")
@Tag(name = "Database Health", description = "Database connection pool monitoring and health checks")
class DatabaseHealthController(
    private val connectionCleanupService: ConnectionCleanupService,
    private val jdbcTemplate: JdbcTemplate
) {

    @GetMapping("/status")
    @Operation(
        summary = "Get Database Connection Pool Status",
        description = "Retrieve detailed information about the database connection pool"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Connection pool status retrieved successfully"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Error retrieving connection pool status"
            )
        ]
    )
    fun getConnectionPoolStatus(): ResponseEntity<Map<String, Any>> {
        return try {
            val status = connectionCleanupService.getConnectionPoolStatus()
            ResponseEntity.ok(status)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf(
                    "error" to "Failed to retrieve connection pool status",
                    "message" to (e.message ?: "Unknown error")
                )
            )
        }
    }

    @GetMapping("/test")
    @Operation(
        summary = "Test Database Connection",
        description = "Test the database connection and return basic connectivity status"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Database connection test successful"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Database connection test failed"
            )
        ]
    )
    fun testDatabaseConnection(): ResponseEntity<Map<String, Any>> {
        return try {
            val status = connectionCleanupService.getConnectionPoolStatus()
            val isHealthy = when {
                status.containsKey("error") -> false
                status["activeConnections"] as? Int ?: 0 > 0 -> true
                else -> false
            }
            
            ResponseEntity.ok(
                mapOf(
                    "status" to if (isHealthy) "healthy" else "unhealthy",
                    "timestamp" to System.currentTimeMillis(),
                    "details" to status
                )
            )
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf(
                    "status" to "unhealthy",
                    "error" to "Database connection test failed",
                    "message" to (e.message ?: "Unknown error"),
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
    }

    @GetMapping("/describe/{tableName}")
    @Operation(
        summary = "Describe Table Structure",
        description = "Get the structure of a specific database table"
    )
    fun describeTable(@org.springframework.web.bind.annotation.PathVariable tableName: String): ResponseEntity<Map<String, Any>> {
        return try {
            val result = jdbcTemplate.queryForList("DESCRIBE $tableName")
            ResponseEntity.ok(mapOf(
                "table" to tableName,
                "columns" to result
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf(
                    "error" to "Failed to describe table $tableName",
                    "message" to (e.message ?: "Unknown error")
                )
            )
        }
    }

    @GetMapping("/constraints/{tableName}")
    @Operation(
        summary = "Show Foreign Key Constraints",
        description = "Get foreign key constraints for a specific table"
    )
    fun getTableConstraints(@org.springframework.web.bind.annotation.PathVariable tableName: String): ResponseEntity<Map<String, Any>> {
        return try {
            val result = jdbcTemplate.queryForList("""
                SELECT 
                    CONSTRAINT_NAME,
                    COLUMN_NAME,
                    REFERENCED_TABLE_NAME,
                    REFERENCED_COLUMN_NAME
                FROM information_schema.KEY_COLUMN_USAGE 
                WHERE TABLE_SCHEMA = DATABASE() 
                AND TABLE_NAME = '$tableName' 
                AND REFERENCED_TABLE_NAME IS NOT NULL
            """)
            ResponseEntity.ok(mapOf(
                "table" to tableName,
                "constraints" to result
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf(
                    "error" to "Failed to get constraints for table $tableName",
                    "message" to (e.message ?: "Unknown error")
                )
            )
        }
    }

    @GetMapping("/migrate/notes")
    @Operation(
        summary = "Migrate Notes Table",
        description = "Fix the notes table structure to match the Note entity"
    )
    fun migrateNotesTable(): ResponseEntity<Map<String, Any>> {
        return try {
            // Check current table structure
            val columns = jdbcTemplate.queryForList("DESCRIBE notes")
            val hasEntityType = columns.any { it["Field"] == "entity_type" }
            val hasOldColumns = columns.any { it["Field"] in listOf("lead_id", "contact_id", "deal_id", "title", "is_private") }
            
            if (hasEntityType && !hasOldColumns) {
                ResponseEntity.ok(mapOf(
                    "message" to "Notes table already fully migrated",
                    "status" to "skipped"
                ))
            } else {
                // Execute migration
                // First, drop foreign key constraints
                try { jdbcTemplate.execute("ALTER TABLE notes DROP FOREIGN KEY notes_ibfk_2") } catch (e: Exception) { /* Ignore if doesn't exist */ }
                try { jdbcTemplate.execute("ALTER TABLE notes DROP FOREIGN KEY notes_ibfk_3") } catch (e: Exception) { /* Ignore if doesn't exist */ }
                try { jdbcTemplate.execute("ALTER TABLE notes DROP FOREIGN KEY notes_ibfk_4") } catch (e: Exception) { /* Ignore if doesn't exist */ }
                
                // Add new columns if they don't exist
                if (!hasEntityType) {
                    jdbcTemplate.execute("ALTER TABLE notes ADD COLUMN entity_type VARCHAR(50) AFTER content")
                    jdbcTemplate.execute("ALTER TABLE notes ADD COLUMN entity_id BIGINT AFTER entity_type")
                }
                
                // Rename user_id to created_by if needed
                val hasUserId = columns.any { it["Field"] == "user_id" }
                if (hasUserId) {
                    jdbcTemplate.execute("ALTER TABLE notes CHANGE COLUMN user_id created_by BIGINT NOT NULL")
                }
                
                // Drop old columns if they exist
                if (columns.any { it["Field"] == "lead_id" }) jdbcTemplate.execute("ALTER TABLE notes DROP COLUMN lead_id")
                if (columns.any { it["Field"] == "contact_id" }) jdbcTemplate.execute("ALTER TABLE notes DROP COLUMN contact_id")
                if (columns.any { it["Field"] == "deal_id" }) jdbcTemplate.execute("ALTER TABLE notes DROP COLUMN deal_id")
                if (columns.any { it["Field"] == "title" }) jdbcTemplate.execute("ALTER TABLE notes DROP COLUMN title")
                if (columns.any { it["Field"] == "is_private" }) jdbcTemplate.execute("ALTER TABLE notes DROP COLUMN is_private")
                
                // Make new columns NOT NULL
                jdbcTemplate.execute("ALTER TABLE notes MODIFY COLUMN entity_type VARCHAR(50) NOT NULL")
                jdbcTemplate.execute("ALTER TABLE notes MODIFY COLUMN entity_id BIGINT NOT NULL")
                
                // Add foreign key constraint for created_by
                try { jdbcTemplate.execute("ALTER TABLE notes ADD CONSTRAINT fk_notes_created_by FOREIGN KEY (created_by) REFERENCES users(id)") } catch (e: Exception) { /* Ignore if already exists */ }
                
                ResponseEntity.ok(mapOf(
                    "message" to "Notes table migrated successfully",
                    "status" to "completed"
                ))
            }
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                mapOf(
                    "error" to "Failed to migrate notes table",
                    "message" to (e.message ?: "Unknown error")
                )
            )
        }
    }
}
