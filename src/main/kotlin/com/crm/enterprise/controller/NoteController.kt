package com.crm.enterprise.controller

import com.crm.enterprise.dto.NoteRequest
import com.crm.enterprise.dto.NoteResponse
import com.crm.enterprise.dto.NoteUpdateRequest
import com.crm.enterprise.service.NoteService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/crm/api/notes")
@Tag(name = "Notes", description = "APIs for managing notes on leads, contacts, and deals")
class NoteController(
    private val noteService: NoteService
) {

    @PostMapping
    @Operation(summary = "Create a new note",
        responses = [
            ApiResponse(responseCode = "201", description = "Note created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ])
    fun createNote(
        @Parameter(description = "Note creation request", required = true)
        @RequestBody noteRequest: NoteRequest,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long,
        @Parameter(description = "User ID", required = true)
        @RequestParam userId: Long
    ): ResponseEntity<NoteResponse> {
        val note = noteService.createNote(noteRequest, companyId, userId)
        return ResponseEntity.status(201).body(note)
    }

    @GetMapping
    @Operation(summary = "Get notes by entity",
        responses = [
            ApiResponse(responseCode = "200", description = "List of notes"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ])
    fun getNotesByEntity(
        @Parameter(description = "Entity type (LEAD, CONTACT, DEAL)", required = true)
        @RequestParam entityType: String,
        @Parameter(description = "Entity ID", required = true)
        @RequestParam entityId: Long
    ): ResponseEntity<List<NoteResponse>> {
        val notes = noteService.getNotesByEntity(entityType, entityId)
        return ResponseEntity.ok(notes)
    }

    @GetMapping("/company")
    @Operation(summary = "Get all notes for a company",
        responses = [
            ApiResponse(responseCode = "200", description = "List of notes"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ])
    fun getNotesByCompany(
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<List<NoteResponse>> {
        val notes = noteService.getNotesByCompany(companyId)
        return ResponseEntity.ok(notes)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing note",
        responses = [
            ApiResponse(responseCode = "200", description = "Note updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request"),
            ApiResponse(responseCode = "404", description = "Note not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ])
    fun updateNote(
        @Parameter(description = "Note ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Note update request", required = true)
        @RequestBody updateRequest: NoteUpdateRequest,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<NoteResponse> {
        val updatedNote = noteService.updateNote(id, updateRequest, companyId)
        return if (updatedNote != null) {
            ResponseEntity.ok(updatedNote)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a note",
        responses = [
            ApiResponse(responseCode = "204", description = "Note deleted successfully"),
            ApiResponse(responseCode = "404", description = "Note not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ])
    fun deleteNote(
        @Parameter(description = "Note ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<Void> {
        val deleted = noteService.deleteNote(id, companyId)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
