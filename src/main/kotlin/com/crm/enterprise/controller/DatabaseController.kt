package com.crm.enterprise.controller

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/database")
class DatabaseController(
    private val jdbcTemplate: JdbcTemplate
) {
    
    @GetMapping("/health")
    fun healthCheck(): Map<String, Any> {
        return try {
            val result = jdbcTemplate.queryForObject("SELECT 1", Int::class.java)
            mapOf("success" to true, "message" to "Database connection is healthy", "test" to result)
        } catch (e: Exception) {
            mapOf("success" to false, "message" to "Database connection failed: ${e.message}")
        }
    }
    
    @GetMapping("/tables")
    fun listTables(): Map<String, Any> {
        return try {
            val tables = jdbcTemplate.queryForList("SHOW TABLES", String::class.java)
            mapOf("success" to true, "tables" to tables)
        } catch (e: Exception) {
            mapOf("success" to false, "message" to "Error listing tables: ${e.message}")
        }
    }
    
    @GetMapping("/stats")
    fun getDatabaseStats(): Map<String, Any> {
        return try {
            val stats = mutableMapOf<String, Any>()
            
            // Get table row counts
            val tables = listOf("leads", "contacts", "deals", "activities", "users", "companies")
            tables.forEach { table ->
                try {
                    val count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM $table", Int::class.java)
                    stats[table] = count ?: 0
                } catch (e: Exception) {
                    stats[table] = "Error: ${e.message}"
                }
            }
            
            mapOf("success" to true, "stats" to stats)
        } catch (e: Exception) {
            mapOf("success" to false, "message" to "Error getting database stats: ${e.message}")
        }
    }
    
    @PostMapping("/execute")
    fun executeSql(@RequestBody sqlRequest: Map<String, String>): Map<String, Any> {
        return try {
            val sql = sqlRequest["sql"] ?: throw IllegalArgumentException("SQL query is required")
            val result = jdbcTemplate.execute(sql)
            mapOf("success" to true, "message" to "SQL executed successfully", "result" to result)
        } catch (e: Exception) {
            mapOf("success" to false, "message" to "Error executing SQL: ${e.message}")
        }
    }
    
    @GetMapping("/describe/{tableName}")
    fun describeTable(@PathVariable tableName: String): Map<String, Any> {
        return try {
            val columns = jdbcTemplate.queryForList("DESCRIBE $tableName")
            mapOf("success" to true, "table" to tableName, "columns" to columns)
        } catch (e: Exception) {
            mapOf("success" to false, "message" to "Error describing table $tableName: ${e.message}")
        }
    }
}