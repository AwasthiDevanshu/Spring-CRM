package com.crm.enterprise.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.sql.SQLException

@Service
class DatabaseSchemaManager(
    private val databaseHandler: DatabaseHandler,
    private val queryGenerator: QueryGenerator
) {

    private val logger = LoggerFactory.getLogger(DatabaseSchemaManager::class.java)

    /**
     * Validates and fixes database schema for a table
     */
    fun validateAndFixTableSchema(tableName: String, expectedColumns: List<ColumnDefinition>): Result<Boolean> {
        return try {
            logger.info("Validating schema for table: $tableName")
            
            // Check if table exists
            val tableExistsResult = databaseHandler.tableExists(tableName)
            if (tableExistsResult.isFailure) {
                logger.error("Failed to check if table exists: ${tableExistsResult.exceptionOrNull()?.message}")
                return Result.failure(tableExistsResult.exceptionOrNull()!!)
            }
            
            if (!tableExistsResult.getOrNull()!!) {
                logger.info("Table $tableName does not exist, creating it")
                return createTable(tableName, expectedColumns)
            }
            
            // Get current schema
            val currentSchemaResult = databaseHandler.getTableSchema(tableName)
            if (currentSchemaResult.isFailure) {
                logger.error("Failed to get table schema: ${currentSchemaResult.exceptionOrNull()?.message}")
                return Result.failure(currentSchemaResult.exceptionOrNull()!!)
            }
            
            val currentColumns = currentSchemaResult.getOrNull()!!
            val missingColumns = findMissingColumns(expectedColumns, currentColumns)
            val incorrectColumns = findIncorrectColumns(expectedColumns, currentColumns)
            
            if (missingColumns.isEmpty() && incorrectColumns.isEmpty()) {
                logger.info("Table $tableName schema is up to date")
                return Result.success(true)
            }
            
            // Fix missing columns
            missingColumns.forEach { column ->
                val addColumnResult = addColumn(tableName, column)
                if (addColumnResult.isFailure) {
                    logger.error("Failed to add column ${column.name}: ${addColumnResult.exceptionOrNull()?.message}")
                    return Result.failure(addColumnResult.exceptionOrNull()!!)
                }
            }
            
            // Fix incorrect columns
            incorrectColumns.forEach { (expected, actual) ->
                val modifyColumnResult = modifyColumn(tableName, expected, actual)
                if (modifyColumnResult.isFailure) {
                    logger.error("Failed to modify column ${expected.name}: ${modifyColumnResult.exceptionOrNull()?.message}")
                    return Result.failure(modifyColumnResult.exceptionOrNull()!!)
                }
            }
            
            logger.info("Successfully validated and fixed schema for table: $tableName")
            Result.success(true)
        } catch (e: Exception) {
            logger.error("Error validating schema for table $tableName: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Creates a table with the specified columns
     */
    private fun createTable(tableName: String, columns: List<ColumnDefinition>): Result<Boolean> {
        return try {
            val columnDefinitions = columns.joinToString(",\n    ") { column ->
                val definition = buildString {
                    append("${column.name} ${column.type}")
                    if (column.length != null) {
                        append("(${column.length})")
                    }
                    if (!column.nullable) {
                        append(" NOT NULL")
                    }
                    if (column.defaultValue != null) {
                        append(" DEFAULT '${column.defaultValue}'")
                    }
                    if (column.autoIncrement) {
                        append(" AUTO_INCREMENT")
                    }
                    if (column.primaryKey) {
                        append(" PRIMARY KEY")
                    }
                }
                definition
            }
            
            val sql = """
                CREATE TABLE $tableName (
                    $columnDefinitions
                )
            """.trimIndent()
            
            val result = databaseHandler.update(sql, emptyMap(), "CREATE_TABLE")
            if (result.isSuccess) {
                logger.info("Successfully created table: $tableName")
                Result.success(true)
            } else {
                Result.failure(result.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            logger.error("Error creating table $tableName: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Adds a missing column to a table
     */
    private fun addColumn(tableName: String, column: ColumnDefinition): Result<Boolean> {
        return try {
            val columnDef = buildString {
                append("${column.name} ${column.type}")
                if (column.length != null) {
                    append("(${column.length})")
                }
                if (!column.nullable) {
                    append(" NOT NULL")
                }
                if (column.defaultValue != null) {
                    append(" DEFAULT '${column.defaultValue}'")
                }
                if (column.autoIncrement) {
                    append(" AUTO_INCREMENT")
                }
            }
            
            val sql = "ALTER TABLE $tableName ADD COLUMN $columnDef"
            val result = databaseHandler.update(sql, emptyMap(), "ADD_COLUMN")
            
            if (result.isSuccess) {
                logger.info("Successfully added column ${column.name} to table $tableName")
                Result.success(true)
            } else {
                Result.failure(result.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            logger.error("Error adding column ${column.name} to table $tableName: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Modifies an existing column
     */
    private fun modifyColumn(tableName: String, expected: ColumnDefinition, actual: DatabaseHandler.ColumnInfo): Result<Boolean> {
        return try {
            val columnDef = buildString {
                append("${expected.name} ${expected.type}")
                if (expected.length != null) {
                    append("(${expected.length})")
                }
                if (!expected.nullable) {
                    append(" NOT NULL")
                }
                if (expected.defaultValue != null) {
                    val defaultValue = if (expected.type == "BOOLEAN") {
                        expected.defaultValue.toString()
                    } else {
                        "'${expected.defaultValue}'"
                    }
                    append(" DEFAULT $defaultValue")
                }
            }
            
            val sql = "ALTER TABLE `$tableName` MODIFY COLUMN $columnDef"
            val result = databaseHandler.update(sql, emptyMap(), "MODIFY_COLUMN")
            
            if (result.isSuccess) {
                logger.info("Successfully modified column ${expected.name} in table $tableName")
                Result.success(true)
            } else {
                Result.failure(result.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            logger.error("Error modifying column ${expected.name} in table $tableName: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Finds missing columns by comparing expected vs actual
     */
    private fun findMissingColumns(
        expected: List<ColumnDefinition>,
        actual: List<DatabaseHandler.ColumnInfo>
    ): List<ColumnDefinition> {
        val actualNames = actual.map { it.name }.toSet()
        return expected.filter { it.name !in actualNames }
    }

    /**
     * Finds incorrect columns by comparing expected vs actual
     */
    private fun findIncorrectColumns(
        expected: List<ColumnDefinition>,
        actual: List<DatabaseHandler.ColumnInfo>
    ): List<Pair<ColumnDefinition, DatabaseHandler.ColumnInfo>> {
        val actualMap = actual.associateBy { it.name }
        return expected.mapNotNull { expectedCol ->
            val actualCol = actualMap[expectedCol.name]
            if (actualCol != null && !isColumnCompatible(expectedCol, actualCol)) {
                Pair(expectedCol, actualCol)
            } else null
        }
    }

    /**
     * Checks if a column is compatible with expected definition
     */
    private fun isColumnCompatible(expected: ColumnDefinition, actual: DatabaseHandler.ColumnInfo): Boolean {
        // Check type compatibility
        val typeCompatible = when (expected.type.uppercase()) {
            "VARCHAR" -> actual.type.uppercase().startsWith("VARCHAR")
            "INT" -> actual.type.uppercase().startsWith("INT")
            "BIGINT" -> actual.type.uppercase().startsWith("BIGINT")
            "TEXT" -> actual.type.uppercase().startsWith("TEXT")
            "DATETIME" -> actual.type.uppercase().startsWith("DATETIME")
            "TIMESTAMP" -> actual.type.uppercase().startsWith("TIMESTAMP")
            "BOOLEAN" -> actual.type.uppercase().startsWith("TINYINT")
            else -> expected.type.uppercase() == actual.type.uppercase()
        }
        
        // Check nullable compatibility
        val nullableCompatible = expected.nullable == actual.nullable
        
        return typeCompatible && nullableCompatible
    }

    /**
     * Gets the expected schema for a table
     */
    fun getExpectedSchema(tableName: String): List<ColumnDefinition> {
        return when (tableName.lowercase()) {
            "contacts" -> getContactsSchema()
            "leads" -> getLeadsSchema()
            "deals" -> getDealsSchema()
            "companies" -> getCompaniesSchema()
            "activities" -> getActivitiesSchema()
            "users" -> getUsersSchema()
            "custom_form_access" -> getCustomFormAccessSchema()
            else -> emptyList()
        }
    }

    private fun getContactsSchema(): List<ColumnDefinition> {
        return listOf(
            ColumnDefinition("id", "BIGINT", nullable = false, primaryKey = true, autoIncrement = true),
            ColumnDefinition("first_name", "VARCHAR", length = "100", nullable = false),
            ColumnDefinition("last_name", "VARCHAR", length = "100", nullable = false),
            ColumnDefinition("email", "VARCHAR", length = "255", nullable = false),
            ColumnDefinition("phone", "VARCHAR", length = "20", nullable = true),
            ColumnDefinition("job_title", "VARCHAR", length = "100", nullable = true),
            ColumnDefinition("department", "VARCHAR", length = "100", nullable = true),
            ColumnDefinition("company_id", "BIGINT", nullable = false),
            ColumnDefinition("lead_id", "BIGINT", nullable = true),
            ColumnDefinition("is_active", "BOOLEAN", nullable = false, defaultValue = "true"),
            ColumnDefinition("notes", "TEXT", nullable = true),
            ColumnDefinition("assigned_user_id", "BIGINT", nullable = true),
            ColumnDefinition("created_by_id", "BIGINT", nullable = true),
            ColumnDefinition("created_at", "DATETIME", nullable = false),
            ColumnDefinition("updated_at", "DATETIME", nullable = false)
        )
    }

    private fun getLeadsSchema(): List<ColumnDefinition> {
        return listOf(
            ColumnDefinition("id", "BIGINT", nullable = false, primaryKey = true, autoIncrement = true),
            ColumnDefinition("first_name", "VARCHAR", length = "100", nullable = false),
            ColumnDefinition("last_name", "VARCHAR", length = "100", nullable = false),
            ColumnDefinition("email", "VARCHAR", length = "255", nullable = false),
            ColumnDefinition("phone", "VARCHAR", length = "20", nullable = true),
            ColumnDefinition("company", "VARCHAR", length = "255", nullable = true),
            ColumnDefinition("job_title", "VARCHAR", length = "100", nullable = true),
            ColumnDefinition("status", "VARCHAR", length = "50", nullable = false, defaultValue = "NEW"),
            ColumnDefinition("source", "VARCHAR", length = "50", nullable = false, defaultValue = "OTHER"),
            ColumnDefinition("score", "INT", nullable = false, defaultValue = "0"),
            ColumnDefinition("notes", "TEXT", nullable = true),
            ColumnDefinition("assigned_user_id", "BIGINT", nullable = true),
            ColumnDefinition("company_id", "BIGINT", nullable = false),
            ColumnDefinition("created_at", "DATETIME", nullable = false),
            ColumnDefinition("updated_at", "DATETIME", nullable = false)
        )
    }

    private fun getDealsSchema(): List<ColumnDefinition> {
        return listOf(
            ColumnDefinition("id", "BIGINT", nullable = false, primaryKey = true, autoIncrement = true),
            ColumnDefinition("name", "VARCHAR", length = "255", nullable = false),
            ColumnDefinition("value", "DECIMAL", length = "15,2", nullable = false),
            ColumnDefinition("stage", "VARCHAR", length = "50", nullable = false),
            ColumnDefinition("status", "VARCHAR", length = "50", nullable = false, defaultValue = "OPEN"),
            ColumnDefinition("probability", "INT", nullable = false, defaultValue = "0"),
            ColumnDefinition("close_date", "DATE", nullable = true),
            ColumnDefinition("description", "TEXT", nullable = true),
            ColumnDefinition("assigned_user_id", "BIGINT", nullable = true),
            ColumnDefinition("company_id", "BIGINT", nullable = false),
            ColumnDefinition("contact_id", "BIGINT", nullable = true),
            ColumnDefinition("created_at", "DATETIME", nullable = false),
            ColumnDefinition("updated_at", "DATETIME", nullable = false)
        )
    }

    private fun getCompaniesSchema(): List<ColumnDefinition> {
        return listOf(
            ColumnDefinition("id", "BIGINT", nullable = false, primaryKey = true, autoIncrement = true),
            ColumnDefinition("name", "VARCHAR", length = "255", nullable = false),
            ColumnDefinition("description", "TEXT", nullable = true),
            ColumnDefinition("website", "VARCHAR", length = "255", nullable = true),
            ColumnDefinition("phone", "VARCHAR", length = "20", nullable = true),
            ColumnDefinition("email", "VARCHAR", length = "255", nullable = true),
            ColumnDefinition("address", "VARCHAR", length = "500", nullable = true),
            ColumnDefinition("city", "VARCHAR", length = "100", nullable = true),
            ColumnDefinition("state", "VARCHAR", length = "100", nullable = true),
            ColumnDefinition("country", "VARCHAR", length = "100", nullable = true),
            ColumnDefinition("postal_code", "VARCHAR", length = "20", nullable = true),
            ColumnDefinition("is_active", "BOOLEAN", nullable = false, defaultValue = "true"),
            ColumnDefinition("created_at", "DATETIME", nullable = false),
            ColumnDefinition("updated_at", "DATETIME", nullable = false)
        )
    }

    private fun getActivitiesSchema(): List<ColumnDefinition> {
        return listOf(
            ColumnDefinition("id", "BIGINT", nullable = false, primaryKey = true, autoIncrement = true),
            ColumnDefinition("type", "VARCHAR", length = "50", nullable = false),
            ColumnDefinition("subject", "VARCHAR", length = "255", nullable = false),
            ColumnDefinition("description", "TEXT", nullable = true),
            ColumnDefinition("outcome", "TEXT", nullable = true),
            ColumnDefinition("duration", "INT", nullable = true),
            ColumnDefinition("status", "VARCHAR", length = "50", nullable = false, defaultValue = "PENDING"),
            ColumnDefinition("priority", "VARCHAR", length = "50", nullable = false, defaultValue = "MEDIUM"),
            ColumnDefinition("assigned_to", "BIGINT", nullable = false),
            ColumnDefinition("assigned_by", "BIGINT", nullable = false),
            ColumnDefinition("entity_type", "VARCHAR", length = "50", nullable = false),
            ColumnDefinition("entity_id", "BIGINT", nullable = false),
            ColumnDefinition("company_id", "BIGINT", nullable = false),
            ColumnDefinition("activity_date", "DATETIME", nullable = false),
            ColumnDefinition("due_date", "DATETIME", nullable = true),
            ColumnDefinition("completed_at", "DATETIME", nullable = true),
            ColumnDefinition("created_at", "DATETIME", nullable = false),
            ColumnDefinition("updated_at", "DATETIME", nullable = false),
            ColumnDefinition("deleted_at", "DATETIME", nullable = true)
        )
    }

    private fun getUsersSchema(): List<ColumnDefinition> {
        return listOf(
            ColumnDefinition("id", "BIGINT", nullable = false, primaryKey = true, autoIncrement = true),
            ColumnDefinition("username", "VARCHAR", length = "100", nullable = false),
            ColumnDefinition("email", "VARCHAR", length = "255", nullable = false),
            ColumnDefinition("password_hash", "VARCHAR", length = "255", nullable = false),
            ColumnDefinition("first_name", "VARCHAR", length = "100", nullable = true),
            ColumnDefinition("last_name", "VARCHAR", length = "100", nullable = true),
            ColumnDefinition("is_active", "BOOLEAN", nullable = false, defaultValue = "true"),
            ColumnDefinition("company_id", "BIGINT", nullable = false),
            ColumnDefinition("created_at", "DATETIME", nullable = false),
            ColumnDefinition("updated_at", "DATETIME", nullable = false)
        )
    }

    private fun getCustomFormAccessSchema(): List<ColumnDefinition> {
        return listOf(
            ColumnDefinition("id", "BIGINT", nullable = false, primaryKey = true, autoIncrement = true),
            ColumnDefinition("form_id", "BIGINT", nullable = false),
            ColumnDefinition("lead_id", "BIGINT", nullable = true),
            ColumnDefinition("contact_id", "BIGINT", nullable = true),
            ColumnDefinition("access_token", "VARCHAR", length = "255", nullable = false),
            ColumnDefinition("is_active", "BOOLEAN", nullable = false, defaultValue = "true"),
            ColumnDefinition("submission_count", "INT", nullable = false, defaultValue = "0"),
            ColumnDefinition("max_submissions", "INT", nullable = true),
            ColumnDefinition("expires_at", "DATETIME", nullable = true),
            ColumnDefinition("last_accessed_at", "DATETIME", nullable = true),
            ColumnDefinition("company_id", "BIGINT", nullable = false),
            ColumnDefinition("created_by", "BIGINT", nullable = false),
            ColumnDefinition("created_at", "DATETIME", nullable = false),
            ColumnDefinition("updated_at", "DATETIME", nullable = false)
        )
    }

    /**
     * Data class for column definitions
     */
    data class ColumnDefinition(
        val name: String,
        val type: String,
        val length: String? = null,
        val nullable: Boolean = true,
        val defaultValue: String? = null,
        val primaryKey: Boolean = false,
        val autoIncrement: Boolean = false
    )
}
