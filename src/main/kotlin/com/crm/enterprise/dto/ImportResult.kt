package com.crm.enterprise.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Result of an import operation")
data class ImportResult(
    @field:Schema(description = "Unique identifier for the import operation", example = "import_123")
    val id: String,
    
    @field:Schema(description = "Name of the imported file", example = "leads_import.csv")
    val fileName: String,
    
    @field:Schema(description = "Type of entity imported", example = "LEAD")
    val entityType: String,
    
    @field:Schema(description = "Status of the import operation", example = "completed")
    val status: String, // pending, processing, completed, failed
    
    @field:Schema(description = "Total number of records in the file", example = "100")
    val totalRecords: Int,
    
    @field:Schema(description = "Number of records processed", example = "100")
    val processedRecords: Int,
    
    @field:Schema(description = "Number of successfully imported records", example = "95")
    val successRecords: Int,
    
    @field:Schema(description = "Number of records that failed to import", example = "5")
    val errorRecords: Int,
    
    @field:Schema(description = "List of error messages", example = "[\"Invalid email format on row 23\"]")
    val errors: List<String>,
    
    @field:Schema(description = "Timestamp when import started", example = "2024-01-15T10:30:00Z")
    val createdAt: String,
    
    @field:Schema(description = "Timestamp when import completed", example = "2024-01-15T10:32:15Z")
    val completedAt: String? = null
)
