package com.crm.enterprise.service

import com.crm.enterprise.dto.ImportResult
import com.crm.enterprise.dto.LeadRequest
import com.crm.enterprise.entity.LeadStatus
import com.crm.enterprise.entity.LeadSource
import com.crm.enterprise.service.LeadService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Instant
import java.util.*

@Service
class ImportService(
    private val leadService: LeadService
) {

    fun importCsv(file: MultipartFile, entityType: String, companyId: Long): ImportResult {
        val importId = "import_${System.currentTimeMillis()}"
        val errors = mutableListOf<String>()
        var totalRecords = 0
        var successRecords = 0
        var errorRecords = 0

        try {
            val reader = BufferedReader(InputStreamReader(file.inputStream))
            val lines = reader.readLines()
            
            if (lines.isEmpty()) {
                throw IllegalArgumentException("CSV file is empty")
            }

            val headers = lines[0].split(",").map { it.trim() }
            val dataLines = lines.drop(1)
            totalRecords = dataLines.size

            when (entityType.uppercase()) {
                "LEAD" -> {
                    val result = importLeads(dataLines, headers, companyId)
                    successRecords = result.first
                    errorRecords = result.second
                    errors.addAll(result.third)
                }
                "CONTACT" -> {
                    // TODO: Implement contact import
                    errors.add("Contact import not implemented yet")
                    errorRecords = totalRecords
                }
                "DEAL" -> {
                    // TODO: Implement deal import
                    errors.add("Deal import not implemented yet")
                    errorRecords = totalRecords
                }
                else -> {
                    throw IllegalArgumentException("Unsupported entity type: $entityType")
                }
            }

        } catch (e: Exception) {
            errors.add("Error processing CSV file: ${e.message}")
            errorRecords = totalRecords
        }

        return ImportResult(
            id = importId,
            fileName = file.originalFilename ?: "unknown.csv",
            entityType = entityType,
            status = if (errorRecords == 0) "completed" else if (successRecords > 0) "completed" else "failed",
            totalRecords = totalRecords,
            processedRecords = totalRecords,
            successRecords = successRecords,
            errorRecords = errorRecords,
            errors = errors,
            createdAt = Instant.now().toString(),
            completedAt = Instant.now().toString()
        )
    }

    @Transactional
    private fun importLeads(dataLines: List<String>, headers: List<String>, companyId: Long): Triple<Int, Int, List<String>> {
        val errors = mutableListOf<String>()
        var successCount = 0
        var errorCount = 0

        dataLines.forEachIndexed { index, line ->
            try {
                val values = parseCsvLine(line)
                if (values.size != headers.size) {
                    errors.add("Row ${index + 2}: Column count mismatch")
                    errorCount++
                    return@forEachIndexed
                }

                val leadData = mapToLeadRequest(values, headers)
                
                // Check if lead already exists by email
                try {
                    val existingLeads = leadService.getLeadsByCompany(companyId)
                    val duplicateLead = existingLeads.find { it.email.equals(leadData.email, ignoreCase = true) }
                    if (duplicateLead != null) {
                        errors.add("Row ${index + 2}: Lead with email ${leadData.email} already exists (ID: ${duplicateLead.id})")
                        errorCount++
                        return@forEachIndexed
                    }
                    
                    leadService.createLead(leadData, companyId)
                    successCount++
                } catch (e: Exception) {
                    if (e.message?.contains("Duplicate entry") == true || e.message?.contains("unique constraint") == true) {
                        errors.add("Row ${index + 2}: Lead with email ${leadData.email} already exists in database")
                    } else {
                        errors.add("Row ${index + 2}: ${e.message}")
                    }
                    errorCount++
                }
            } catch (e: Exception) {
                errors.add("Row ${index + 2}: ${e.message}")
                errorCount++
            }
        }

        return Triple(successCount, errorCount, errors)
    }

    private fun mapToLeadRequest(values: List<String>, headers: List<String>): LeadRequest {
        val dataMap = headers.zip(values).toMap()
        
        return LeadRequest(
            firstName = dataMap["firstName"] ?: "",
            lastName = dataMap["lastName"] ?: "",
            email = dataMap["email"] ?: "",
            phone = dataMap["phone"]?.takeIf { it.isNotEmpty() },
            company = dataMap["company"] ?: "",
            jobTitle = dataMap["jobTitle"] ?: "",
            status = try {
                LeadStatus.valueOf(dataMap["status"]?.uppercase() ?: "NEW")
            } catch (e: Exception) {
                LeadStatus.NEW
            },
            source = try {
                LeadSource.valueOf(dataMap["source"]?.uppercase() ?: "WEBSITE")
            } catch (e: Exception) {
                LeadSource.WEBSITE
            },
            score = dataMap["score"]?.toIntOrNull() ?: 0,
            notes = dataMap["notes"] ?: ""
        )
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        
        for (i in line.indices) {
            val char = line[i]
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim())
        return result
    }

    fun getImportHistory(companyId: Long): List<ImportResult> {
        // TODO: Implement import history storage and retrieval
        return emptyList()
    }
}
