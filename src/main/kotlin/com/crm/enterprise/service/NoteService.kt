package com.crm.enterprise.service

import com.crm.enterprise.dto.NoteRequest
import com.crm.enterprise.dto.NoteResponse
import com.crm.enterprise.dto.NoteUpdateRequest
import com.crm.enterprise.entity.Note
import com.crm.enterprise.repository.NoteRepository
import com.crm.enterprise.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class NoteService(
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository
) {
    
    fun createNote(noteRequest: NoteRequest, companyId: Long, userId: Long): NoteResponse {
        val note = Note(
            content = noteRequest.content,
            entityType = noteRequest.entityType,
            entityId = noteRequest.entityId,
            createdBy = userId,
            companyId = companyId
        )
        val savedNote = noteRepository.save(note)
        return toNoteResponse(savedNote)
    }
    
    fun getNotesByEntity(entityType: String, entityId: Long): List<NoteResponse> {
        return noteRepository.findByEntityTypeAndEntityId(entityType, entityId)
            .map { toNoteResponse(it) }
    }
    
    fun getNotesByCompany(companyId: Long): List<NoteResponse> {
        return noteRepository.findByCompanyId(companyId)
            .map { toNoteResponse(it) }
    }
    
    fun updateNote(id: Long, updateRequest: NoteUpdateRequest, companyId: Long): NoteResponse? {
        return noteRepository.findById(id)
            .filter { it.companyId == companyId }
            .map { existingNote ->
                val updatedNote = existingNote.copy(
                    content = updateRequest.content,
                    updatedAt = LocalDateTime.now()
                )
                val savedNote = noteRepository.save(updatedNote)
                toNoteResponse(savedNote)
            }
            .orElse(null)
    }
    
    fun deleteNote(id: Long, companyId: Long): Boolean {
        return noteRepository.findById(id)
            .filter { it.companyId == companyId }
            .map {
                noteRepository.delete(it)
                true
            }
            .orElse(false)
    }
    
    private fun toNoteResponse(note: Note): NoteResponse {
        val createdByUser = userRepository.findById(note.createdBy).orElse(null)
        val createdByName = createdByUser?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown User"
        
        return NoteResponse(
            id = note.id ?: 0L,
            content = note.content,
            entityType = note.entityType,
            entityId = note.entityId,
            createdBy = note.createdBy,
            createdByName = createdByName,
            companyId = note.companyId,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt
        )
    }
}
