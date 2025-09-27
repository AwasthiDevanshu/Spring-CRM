package com.crm.enterprise.service

import com.crm.enterprise.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.stereotype.Service
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*

@Service
class DatabaseHandler(
    private val jdbcTemplate: JdbcTemplate,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {

    private val logger = LoggerFactory.getLogger(DatabaseHandler::class.java)

    /**
     * Executes a SELECT query with proper error handling
     */
    fun <T> queryForList(
        sql: String,
        params: Map<String, Any> = emptyMap(),
        rowMapper: (Map<String, Any>) -> T,
        operation: String = "SELECT"
    ): Result<List<T>> {
        return try {
            logger.debug("Executing $operation query: $sql with params: $params")
            
            val paramSource = MapSqlParameterSource(params)
            val results = namedParameterJdbcTemplate.query(sql, paramSource) { rs, _ ->
                val rowData = mutableMapOf<String, Any>()
                val metaData = rs.metaData
                for (i in 1..metaData.columnCount) {
                    val columnName = metaData.getColumnName(i)
                    val value = rs.getObject(i)
                    rowData[columnName] = value ?: ""
                }
                rowMapper(rowData)
            }
            
            logger.debug("Query executed successfully, returned ${results.size} rows")
            Result.success(results)
        } catch (e: SQLException) {
            val error = createDatabaseError("SELECT", sql, e)
            logger.error("SQL Error in query: ${error.message}", e)
            Result.failure(error)
        } catch (e: DataAccessException) {
            val error = createDatabaseError("SELECT", sql, e)
            logger.error("Data Access Error in query: ${error.message}", e)
            Result.failure(error)
        } catch (e: Exception) {
            val error = createDatabaseError("SELECT", sql, e)
            logger.error("Unexpected error in query: ${error.message}", e)
            Result.failure(error)
        }
    }

    /**
     * Executes a SELECT query for a single object
     */
    fun <T> queryForObject(
        sql: String,
        params: Map<String, Any> = emptyMap(),
        rowMapper: (Map<String, Any>) -> T,
        operation: String = "SELECT"
    ): Result<T?> {
        return try {
            logger.debug("Executing $operation query for single object: $sql with params: $params")
            
            val paramSource = MapSqlParameterSource(params)
            val results = namedParameterJdbcTemplate.query(sql, paramSource) { rs, _ ->
                val rowData = mutableMapOf<String, Any>()
                val metaData = rs.metaData
                for (i in 1..metaData.columnCount) {
                    val columnName = metaData.getColumnName(i)
                    val value = rs.getObject(i)
                    rowData[columnName] = value ?: ""
                }
                rowMapper(rowData)
            }
            
            val result = if (results.isNotEmpty()) results.first() else null
            logger.debug("Query executed successfully, returned single object")
            Result.success(result)
        } catch (e: SQLException) {
            val error = createDatabaseError("SELECT", sql, e)
            logger.error("SQL Error in query: ${error.message}", e)
            Result.failure(error)
        } catch (e: DataAccessException) {
            val error = createDatabaseError("SELECT", sql, e)
            logger.error("Data Access Error in query: ${error.message}", e)
            Result.failure(error)
        } catch (e: Exception) {
            val error = createDatabaseError("SELECT", sql, e)
            logger.error("Unexpected error in query: ${error.message}", e)
            Result.failure(error)
        }
    }

    /**
     * Executes an INSERT query with proper error handling
     */
    fun insert(
        sql: String,
        params: Map<String, Any> = emptyMap(),
        operation: String = "INSERT"
    ): Result<Long> {
        return try {
            logger.debug("Executing {} query: {} with params: {}", operation, sql, params)
            
            val paramSource = MapSqlParameterSource(params)
            val keyHolder = org.springframework.jdbc.support.GeneratedKeyHolder()
            
            val rowsAffected = namedParameterJdbcTemplate.update(sql, paramSource, keyHolder)
            
            if (rowsAffected == 0) {
                val error = DatabaseException("No rows were inserted", operation, sql)
                logger.warn("Insert operation affected 0 rows: $sql")
                return Result.failure(error)
            }
            
            val generatedId = keyHolder.key?.toLong() ?: 0L
            logger.debug("Insert executed successfully, generated ID: $generatedId")
            Result.success(generatedId)
        } catch (e: SQLException) {
            val error = createDatabaseError("INSERT", sql, e)
            logger.error("SQL Error in insert: ${error.message}", e)
            Result.failure(error)
        } catch (e: DataAccessException) {
            val error = createDatabaseError("INSERT", sql, e)
            logger.error("Data Access Error in insert: ${error.message}", e)
            Result.failure(error)
        } catch (e: Exception) {
            val error = createDatabaseError("INSERT", sql, e)
            logger.error("Unexpected error in insert: ${error.message}", e)
            Result.failure(error)
        }
    }

    /**
     * Executes an UPDATE query with proper error handling
     */
    fun update(
        sql: String,
        params: Map<String, Any> = emptyMap(),
        operation: String = "UPDATE"
    ): Result<Int> {
        return try {
            logger.debug("Executing {} query: {} with params: {}", operation, sql, params)
            
            val paramSource = MapSqlParameterSource(params)
            val rowsAffected = namedParameterJdbcTemplate.update(sql, paramSource)
            
            logger.debug("Update executed successfully, affected $rowsAffected rows")
            Result.success(rowsAffected)
        } catch (e: SQLException) {
            val error = createDatabaseError("UPDATE", sql, e)
            logger.error("SQL Error in update: ${error.message}", e)
            Result.failure(error)
        } catch (e: DataAccessException) {
            val error = createDatabaseError("UPDATE", sql, e)
            logger.error("Data Access Error in update: ${error.message}", e)
            Result.failure(error)
        } catch (e: Exception) {
            val error = createDatabaseError("UPDATE", sql, e)
            logger.error("Unexpected error in update: ${error.message}", e)
            Result.failure(error)
        }
    }

    /**
     * Executes a DELETE query with proper error handling
     */
    fun delete(
        sql: String,
        params: Map<String, Any> = emptyMap(),
        operation: String = "DELETE"
    ): Result<Int> {
        return try {
            logger.debug("Executing {} query: {} with params: {}", operation, sql, params)
            
            val paramSource = MapSqlParameterSource(params)
            val rowsAffected = namedParameterJdbcTemplate.update(sql, paramSource)
            
            logger.debug("Delete executed successfully, affected $rowsAffected rows")
            Result.success(rowsAffected)
        } catch (e: SQLException) {
            val error = createDatabaseError("DELETE", sql, e)
            logger.error("SQL Error in delete: ${error.message}", e)
            Result.failure(error)
        } catch (e: DataAccessException) {
            val error = createDatabaseError("DELETE", sql, e)
            logger.error("Data Access Error in delete: ${error.message}", e)
            Result.failure(error)
        } catch (e: Exception) {
            val error = createDatabaseError("DELETE", sql, e)
            logger.error("Unexpected error in delete: ${error.message}", e)
            Result.failure(error)
        }
    }

    /**
     * Executes a batch operation with proper error handling
     */
    fun batchUpdate(
        sql: String,
        batchParams: List<Map<String, Any>>,
        operation: String = "BATCH_UPDATE"
    ): Result<IntArray> {
        return try {
            logger.debug("Executing $operation query: $sql with ${batchParams.size} batch operations")
            
            val paramSources = batchParams.map { MapSqlParameterSource(it) }
            val results = namedParameterJdbcTemplate.batchUpdate(sql, paramSources.toTypedArray())
            
            logger.debug("Batch update executed successfully, affected ${results.sum()} total rows")
            Result.success(results)
        } catch (e: SQLException) {
            val error = createDatabaseError("BATCH_UPDATE", sql, e)
            logger.error("SQL Error in batch update: ${error.message}", e)
            Result.failure(error)
        } catch (e: DataAccessException) {
            val error = createDatabaseError("BATCH_UPDATE", sql, e)
            logger.error("Data Access Error in batch update: ${error.message}", e)
            Result.failure(error)
        } catch (e: Exception) {
            val error = createDatabaseError("BATCH_UPDATE", sql, e)
            logger.error("Unexpected error in batch update: ${error.message}", e)
            Result.failure(error)
        }
    }

    /**
     * Checks if a table exists
     */
    fun tableExists(tableName: String): Result<Boolean> {
        val sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = :tableName"
        return queryForObject(sql, mapOf("tableName" to tableName), { it["COUNT(*)"] as Long == 1L }, "TABLE_EXISTS")
            .map { it ?: false }
    }

    /**
     * Gets table schema information
     */
    fun getTableSchema(tableName: String): Result<List<ColumnInfo>> {
        val sql = """
            SELECT 
                COLUMN_NAME,
                DATA_TYPE,
                IS_NULLABLE,
                COLUMN_DEFAULT,
                COLUMN_KEY,
                EXTRA
            FROM information_schema.columns 
            WHERE table_schema = DATABASE() 
            AND table_name = :tableName
            ORDER BY ORDINAL_POSITION
        """.trimIndent()
        
        return queryForList(sql, mapOf("tableName" to tableName), { row ->
            ColumnInfo(
                name = row["COLUMN_NAME"] as String,
                type = row["DATA_TYPE"] as String,
                nullable = (row["IS_NULLABLE"] as String) == "YES",
                defaultValue = row["COLUMN_DEFAULT"] as? String,
                key = row["COLUMN_KEY"] as? String,
                extra = row["EXTRA"] as? String
            )
        }, "GET_SCHEMA")
    }

    /**
     * Creates a user-friendly database error
     */
    private fun createDatabaseError(operation: String, sql: String, exception: Exception): DatabaseException {
        val message = when (exception) {
            is SQLException -> {
                when (exception.errorCode) {
                    1054 -> "Database schema error: Column '${extractColumnName(exception.message)}' does not exist in table"
                    1062 -> "Duplicate entry error: The record already exists"
                    1452 -> "Foreign key constraint error: Referenced record does not exist"
                    1048 -> "Required field error: A required field is missing or null"
                    1064 -> "SQL syntax error: ${exception.message}"
                    else -> "Database error (${exception.errorCode}): ${exception.message}"
                }
            }
            is DataAccessException -> "Data access error: ${exception.message}"
            else -> "Database operation failed: ${exception.message}"
        }
        
        return DatabaseException(message, operation, sql, exception)
    }

    /**
     * Extracts column name from SQL error message
     */
    private fun extractColumnName(message: String?): String {
        return message?.substringAfter("Unknown column '")?.substringBefore("'") ?: "unknown"
    }

    /**
     * Data class for column information
     */
    data class ColumnInfo(
        val name: String,
        val type: String,
        val nullable: Boolean,
        val defaultValue: String?,
        val key: String?,
        val extra: String?
    )

    /**
     * Custom database exception with better error messages
     */
    class DatabaseException(
        message: String,
        val operation: String,
        val sql: String,
        cause: Throwable? = null
    ) : Exception(message, cause) {
        fun toErrorResponse(): ErrorResponse {
            return ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = 500,
                error = "Database Error",
                message = message ?: "Unknown database error",
                path = "/api/database",
                details = mapOf(
                    "operation" to operation,
                    "sql" to sql.take(200) + if (sql.length > 200) "..." else "",
                    "cause" to (cause?.message ?: "Unknown")
                )
            )
        }
    }
}
