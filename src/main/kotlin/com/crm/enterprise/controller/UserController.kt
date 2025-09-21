package com.crm.enterprise.controller

import com.crm.enterprise.dto.UserCreateRequest
import com.crm.enterprise.dto.UserResponse
import com.crm.enterprise.dto.UserUpdateRequest
import com.crm.enterprise.service.UserService
import com.crm.enterprise.util.RequestUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/crm/api/users")
@Tag(name = "User Management", description = "APIs for managing users within a company")
class UserController(
    private val userService: UserService,
    private val requestUtils: RequestUtils
) {

    @GetMapping
    @Operation(summary = "Get all users for a company",
        responses = [
            ApiResponse(responseCode = "200", description = "List of users"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ])
    fun getUsers(
        request: HttpServletRequest
    ): ResponseEntity<List<UserResponse>> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val users = userService.findUsersByCompanyId(companyId)
        return ResponseEntity.ok(users)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID",
        responses = [
            ApiResponse(responseCode = "200", description = "User found"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ])
    fun getUser(
        @Parameter(description = "User ID", required = true)
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<UserResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val user = userService.findByIdAndCompanyId(id, companyId)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    @Operation(summary = "Create a new user",
        responses = [
            ApiResponse(responseCode = "201", description = "User created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ])
    fun createUser(
        @Parameter(description = "User creation request", required = true)
        @RequestBody userRequest: UserCreateRequest,
        request: HttpServletRequest
    ): ResponseEntity<UserResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val user = userService.createUser(userRequest, companyId)
        return ResponseEntity.status(201).body(user)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user",
        responses = [
            ApiResponse(responseCode = "200", description = "User updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ])
    fun updateUser(
        @Parameter(description = "User ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "User update request", required = true)
        @RequestBody updateRequest: UserUpdateRequest,
        request: HttpServletRequest
    ): ResponseEntity<UserResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val updatedUser = userService.updateUser(id, updateRequest, companyId)
        return if (updatedUser != null) {
            ResponseEntity.ok(updatedUser)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user",
        responses = [
            ApiResponse(responseCode = "204", description = "User deleted successfully"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "400", description = "Cannot delete user"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ])
    fun deleteUser(
        @Parameter(description = "User ID", required = true)
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val deleted = userService.deleteUser(id, companyId)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a user",
        responses = [
            ApiResponse(responseCode = "200", description = "User activated successfully"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ])
    fun activateUser(
        @Parameter(description = "User ID", required = true)
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<UserResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val user = userService.activateUser(id, companyId)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a user",
        responses = [
            ApiResponse(responseCode = "200", description = "User deactivated successfully"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ])
    fun deactivateUser(
        @Parameter(description = "User ID", required = true)
        @PathVariable id: Long,
        request: HttpServletRequest
    ): ResponseEntity<UserResponse> {
        val companyId = requestUtils.extractCompanyIdFromToken(request)
        if (companyId == null) {
            return ResponseEntity.badRequest().build()
        }
        val user = userService.deactivateUser(id, companyId)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
