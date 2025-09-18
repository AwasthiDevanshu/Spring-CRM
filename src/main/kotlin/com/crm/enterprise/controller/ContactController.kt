package com.crm.enterprise.controller

import com.crm.enterprise.dto.ContactRequest
import com.crm.enterprise.dto.ContactResponse
import com.crm.enterprise.dto.ContactUpdateRequest
import com.crm.enterprise.service.ContactService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/contacts")
@Tag(name = "Contacts Management", description = "Contact management endpoints for creating, reading, updating, and deleting contacts")
class ContactController(
    private val contactService: ContactService
) {
    
    @PostMapping
    @Operation(
        summary = "Create New Contact",
        description = "Create a new contact in the CRM system"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Contact created successfully",
                content = [Content(schema = Schema(implementation = ContactResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data"
            )
        ]
    )
    fun createContact(
        @Parameter(description = "Contact information", required = true)
        @RequestBody contactRequest: ContactRequest,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<ContactResponse> {
        return try {
            val contact = contactService.createContact(contactRequest, companyId)
            ResponseEntity.ok(contact)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @GetMapping
    @Operation(
        summary = "Get Contacts",
        description = "Get contacts with optional filtering by assigned user or search term"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Contacts retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<ContactResponse>::class))]
            )
        ]
    )
    fun getContacts(
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long,
        @Parameter(description = "Assigned user ID", required = false)
        @RequestParam(required = false) assignedUserId: Long?,
        @Parameter(description = "Search term", required = false)
        @RequestParam(required = false) search: String?
    ): ResponseEntity<List<ContactResponse>> {
        return try {
            val contacts = when {
                assignedUserId != null -> contactService.getContactsByAssignedUser(companyId, assignedUserId)
                !search.isNullOrBlank() -> contactService.searchContacts(companyId, search)
                else -> contactService.getContactsByCompany(companyId)
            }
            ResponseEntity.ok(contacts)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get Contact by ID",
        description = "Get a specific contact by its ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Contact retrieved successfully",
                content = [Content(schema = Schema(implementation = ContactResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Contact not found"
            )
        ]
    )
    fun getContact(
        @Parameter(description = "Contact ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<ContactResponse> {
        return try {
            val contact = contactService.getContactById(id, companyId)
            if (contact != null) {
                ResponseEntity.ok(contact)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Update Contact",
        description = "Update an existing contact"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Contact updated successfully",
                content = [Content(schema = Schema(implementation = ContactResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Contact not found"
            )
        ]
    )
    fun updateContact(
        @Parameter(description = "Contact ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Updated contact information", required = true)
        @RequestBody updateRequest: ContactUpdateRequest,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<ContactResponse> {
        return try {
            val contact = contactService.updateContact(id, updateRequest, companyId)
            if (contact != null) {
                ResponseEntity.ok(contact)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete Contact",
        description = "Delete a contact from the system"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Contact deleted successfully"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Contact not found"
            )
        ]
    )
    fun deleteContact(
        @Parameter(description = "Contact ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Company ID", required = true)
        @RequestParam companyId: Long
    ): ResponseEntity<Void> {
        return try {
            val deleted = contactService.deleteContact(id, companyId)
            if (deleted) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
}
