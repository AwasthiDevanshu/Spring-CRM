package com.crm.enterprise.service

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class QueryGenerator {

    /**
     * Generates a safe SELECT query with WHERE conditions
     */
    fun generateSelectQuery(
        tableName: String,
        columns: List<String> = listOf("*"),
        whereConditions: Map<String, Any> = emptyMap(),
        orderBy: String? = null,
        limit: Int? = null,
        offset: Int? = null
    ): String {
        val columnList = if (columns.contains("*")) "*" else columns.joinToString(", ")
        var query = "SELECT $columnList FROM `$tableName`"
        
        if (whereConditions.isNotEmpty()) {
            val whereClause = whereConditions.keys.joinToString(" AND ") { "`$it` = :$it" }
            query += " WHERE $whereClause"
        }
        
        orderBy?.let { query += " ORDER BY $it" }
        limit?.let { query += " LIMIT $it" }
        offset?.let { query += " OFFSET $it" }
        
        return query
    }

    /**
     * Generates a safe INSERT query
     */
    fun generateInsertQuery(
        tableName: String,
        data: Map<String, Any>
    ): String {
        val columns = data.keys.joinToString(", ") { "`$it`" }
        val placeholders = data.keys.joinToString(", ") { ":$it" }
        
        return "INSERT INTO `$tableName` ($columns) VALUES ($placeholders)"
    }

    /**
     * Generates a safe UPDATE query
     */
    fun generateUpdateQuery(
        tableName: String,
        data: Map<String, Any>,
        whereConditions: Map<String, Any>
    ): String {
        val setClause = data.keys.joinToString(", ") { "`$it` = :$it" }
        val whereClause = whereConditions.keys.joinToString(" AND ") { "`$it` = :where_$it" }
        
        // Prefix where parameters to avoid conflicts
        val prefixedWhereConditions = whereConditions.mapKeys { "where_${it.key}" }
        
        return "UPDATE `$tableName` SET $setClause WHERE $whereClause"
    }

  

    /**
     * Generates a COUNT query
     */
    fun generateCountQuery(
        tableName: String,
        whereConditions: Map<String, Any> = emptyMap()
    ): String {
        var query = "SELECT COUNT(*) as count FROM `$tableName`"
        
        if (whereConditions.isNotEmpty()) {
            val whereClause = whereConditions.keys.joinToString(" AND ") { "`$it` = :$it" }
            query += " WHERE $whereClause"
        }
        
        return query
    }

    /**
     * Generates a query to check if a record exists
     */
    fun generateExistsQuery(
        tableName: String,
        whereConditions: Map<String, Any>
    ): String {
        val whereClause = whereConditions.keys.joinToString(" AND ") { "`$it` = :$it" }
        return "SELECT 1 FROM `$tableName` WHERE $whereClause LIMIT 1"
    }

    /**
     * Generates a query to get table schema
     */
    fun generateSchemaQuery(tableName: String): String {
        return """
            SELECT 
                COLUMN_NAME,
                DATA_TYPE,
                IS_NULLABLE,
                COLUMN_DEFAULT,
                COLUMN_KEY,
                EXTRA,
                ORDINAL_POSITION
            FROM information_schema.columns 
            WHERE table_schema = DATABASE() 
            AND table_name = :tableName
            ORDER BY ORDINAL_POSITION
        """.trimIndent()
    }

    /**
     * Generates a query to check if table exists
     */
    fun generateTableExistsQuery(tableName: String): String {
        return "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = :tableName"
    }

    /**
     * Generates a query to get foreign key constraints
     */
    fun generateForeignKeyQuery(tableName: String): String {
        return """
            SELECT 
                COLUMN_NAME,
                REFERENCED_TABLE_NAME,
                REFERENCED_COLUMN_NAME,
                CONSTRAINT_NAME
            FROM information_schema.KEY_COLUMN_USAGE 
            WHERE table_schema = DATABASE() 
            AND table_name = :tableName
            AND REFERENCED_TABLE_NAME IS NOT NULL
        """.trimIndent()
    }

    /**
     * Generates a query to get indexes
     */
    fun generateIndexQuery(tableName: String): String {
        return """
            SELECT 
                INDEX_NAME,
                COLUMN_NAME,
                NON_UNIQUE,
                SEQ_IN_INDEX
            FROM information_schema.STATISTICS 
            WHERE table_schema = DATABASE() 
            AND table_name = :tableName
            ORDER BY INDEX_NAME, SEQ_IN_INDEX
        """.trimIndent()
    }

    /**
     * Generates a safe search query with LIKE conditions
     */
    fun generateSearchQuery(
        tableName: String,
        columns: List<String> = listOf("*"),
        searchFields: Map<String, String>,
        exactMatchFields: Map<String, Any> = emptyMap(),
        orderBy: String? = null,
        limit: Int? = null
    ): Pair<String, Map<String, Any>> {
        val columnList = if (columns.contains("*")) "*" else columns.joinToString(", ")
        var query = "SELECT $columnList FROM `$tableName`"
        val params = mutableMapOf<String, Any>()
        
        val conditions = mutableListOf<String>()
        
        // Add exact match conditions
        exactMatchFields.forEach { (field, value) ->
            conditions.add("`$field` = :exact_$field")
            params["exact_$field"] = value
        }
        
        // Add search conditions
        searchFields.forEach { (field, searchTerm) ->
            conditions.add("`$field` LIKE :search_$field")
            params["search_$field"] = "%$searchTerm%"
        }
        
        if (conditions.isNotEmpty()) {
            query += " WHERE ${conditions.joinToString(" AND ")}"
        }
        
        orderBy?.let { query += " ORDER BY $it" }
        limit?.let { query += " LIMIT $it" }
        
        return Pair(query, params)
    }

    /**
     * Generates a query for pagination
     */
    fun generatePaginationQuery(
        baseQuery: String,
        page: Int,
        size: Int
    ): String {
        val offset = (page - 1) * size
        return "$baseQuery LIMIT $size OFFSET $offset"
    }

    /**
     * Generates a query to get record count for pagination
     */
    fun generateCountQueryForPagination(
        tableName: String,
        whereConditions: Map<String, Any> = emptyMap(),
        searchFields: Map<String, String> = emptyMap()
    ): Pair<String, Map<String, Any>> {
        var query = "SELECT COUNT(*) as total FROM `$tableName`"
        val params = mutableMapOf<String, Any>()
        
        val conditions = mutableListOf<String>()
        
        // Add exact match conditions
        whereConditions.forEach { (field, value) ->
            conditions.add("`$field` = :exact_$field")
            params["exact_$field"] = value
        }
        
        // Add search conditions
        searchFields.forEach { (field, searchTerm) ->
            conditions.add("`$field` LIKE :search_$field")
            params["search_$field"] = "%$searchTerm%"
        }
        
        if (conditions.isNotEmpty()) {
            query += " WHERE ${conditions.joinToString(" AND ")}"
        }
        
        return Pair(query, params)
    }

    /**
     * Generates a query to get recent records
     */
    fun generateRecentRecordsQuery(
        tableName: String,
        dateColumn: String = "created_at",
        limit: Int = 10
    ): String {
        return "SELECT * FROM `$tableName` ORDER BY `$dateColumn` DESC LIMIT $limit"
    }

    /**
     * Generates a query to get records by date range
     */
    fun generateDateRangeQuery(
        tableName: String,
        dateColumn: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        additionalConditions: Map<String, Any> = emptyMap()
    ): Pair<String, Map<String, Any>> {
        var query = "SELECT * FROM `$tableName` WHERE `$dateColumn` BETWEEN :start_date AND :end_date"
        val params = mutableMapOf<String, Any>(
            "start_date" to startDate,
            "end_date" to endDate
        )
        
        additionalConditions.forEach { (field, value) ->
            query += " AND `$field` = :$field"
            params[field] = value
        }
        
        return Pair(query, params)
    }
}
