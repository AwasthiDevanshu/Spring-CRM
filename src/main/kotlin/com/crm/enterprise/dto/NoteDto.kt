package com.crm.enterprise.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Request DTO for creating a new note")
data class NoteRequest(
    @field:Schema(description = "Note content", example = "Customer showed interest in premium features")
    val content: String,
    @field:Schema(description = "Entity type", example = "LEAD")
    val entityType: String,
    @field:Schema(description = "Entity ID", example = "1")
    val entityId: Long
)

@Schema(description = "Response DTO for a note")
data class NoteResponse(
    @field:Schema(description = "Unique identifier of the note", example = "1")
    val id: Long,
    @field:Schema(description = "Note content", example = "Customer showed interest in premium features")
    val content: String,
    @field:Schema(description = "Entity type", example = "LEAD")
    val entityType: String,
    @field:Schema(description = "Entity ID", example = "1")
    val entityId: Long,
    @field:Schema(description = "ID of the user who created the note", example = "1")
    val createdBy: Long,
    @field:Schema(description = "Name of the user who created the note", example = "John Doe")
    val createdByName: String,
    @field:Schema(description = "Company ID", example = "1")
    val companyId: Long,
    @field:Schema(description = "Timestamp when the note was created", example = "2024-01-15T10:30:00")
    val createdAt: LocalDateTime,
    @field:Schema(description = "Timestamp when the note was last updated", example = "2024-01-15T10:30:00")
    val updatedAt: LocalDateTime
)

@Schema(description = "Request DTO for updating an existing note")
data class NoteUpdateRequest(
    @field:Schema(description = "Updated note content", example = "Customer confirmed interest and requested a demo")
    val content: String
)
