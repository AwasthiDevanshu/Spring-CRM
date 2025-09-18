package com.crm.enterprise.controller

import com.crm.enterprise.dto.ImportResult
import com.crm.enterprise.service.ImportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/import")
@Tag(name = "Import", description = "Data import operations")
class ImportController(
    private val importService: ImportService
) {

    @PostMapping("/csv")
    @Operation(summary = "Import data from CSV file", description = "Upload and process a CSV file to import leads, contacts, or deals")
    fun importCsv(
        @Parameter(description = "CSV file to import", required = true)
        @RequestParam("file") file: MultipartFile,
        @Parameter(description = "Entity type to import (LEAD, CONTACT, DEAL)", required = true)
        @RequestParam("entityType") entityType: String,
        @Parameter(description = "Company ID", required = true)
        @RequestParam("companyId") companyId: Long
    ): ResponseEntity<ImportResult> {
        return try {
            val result = importService.importCsv(file, entityType, companyId)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                ImportResult(
                    id = "",
                    fileName = file.originalFilename ?: "unknown",
                    entityType = entityType,
                    status = "failed",
                    totalRecords = 0,
                    processedRecords = 0,
                    successRecords = 0,
                    errorRecords = 0,
                    errors = listOf(e.message ?: "Unknown error"),
                    createdAt = java.time.Instant.now().toString(),
                    completedAt = java.time.Instant.now().toString()
                )
            )
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Get import history", description = "Retrieve the history of all import operations")
    fun getImportHistory(
        @Parameter(description = "Company ID", required = true)
        @RequestParam("companyId") companyId: Long
    ): ResponseEntity<List<ImportResult>> {
        return try {
            val history = importService.getImportHistory(companyId)
            ResponseEntity.ok(history)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(emptyList())
        }
    }
}
