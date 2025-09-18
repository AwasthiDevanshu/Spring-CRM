package com.crm.enterprise.controller

import com.crm.enterprise.dto.LoginRequest
import com.crm.enterprise.dto.LoginResponse
import com.crm.enterprise.dto.UserResponse
import com.crm.enterprise.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
class AuthController(
    private val userService: UserService
) {

    @PostMapping("/login")
    @Operation(
        summary = "User Login",
        description = "Authenticate user with email and password, returns JWT token and user information"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Login successful",
                content = [Content(schema = Schema(implementation = LoginResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid credentials or request",
                content = [Content(schema = Schema(implementation = LoginResponse::class))]
            )
        ]
    )
    fun login(
        @Parameter(description = "Login credentials", required = true)
        @RequestBody loginRequest: LoginRequest
    ): ResponseEntity<LoginResponse> {
        return try {
            val user = userService.findByEmail(loginRequest.email)
            
            if (user == null || !user.isActive) {
                return ResponseEntity.badRequest().body(
                    LoginResponse(success = false, message = "Invalid credentials")
                )
            }
            
            // For demo purposes, accept any password
            // In production, you would verify the password hash
            val token = "jwt-token-${System.currentTimeMillis()}"
            
            ResponseEntity.ok(
                LoginResponse(
                    success = true,
                    token = token,
                    user = userService.toUserResponse(user)
                )
            )
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                LoginResponse(success = false, message = "Login failed: ${e.message}")
            )
        }
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get Current User",
        description = "Get current authenticated user information using JWT token"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User information retrieved successfully",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Invalid or missing token"
            )
        ]
    )
    fun getCurrentUser(
        @Parameter(description = "JWT Bearer token", required = true)
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<UserResponse> {
        return try {
            // Simple token validation - in production, use JWT validation
            if (!token.startsWith("Bearer jwt-token-")) {
                return ResponseEntity.status(401).build()
            }

            // For demo, return the first user
            val users = userService.findAll()
            if (users.isEmpty()) {
                return ResponseEntity.status(401).build()
            }

            ResponseEntity.ok(userService.toUserResponse(users.first()))
        } catch (e: Exception) {
            ResponseEntity.status(401).build()
        }
    }

    @GetMapping("/debug/users")
    fun debugUsers(): ResponseEntity<List<UserResponse>> {
        return try {
            val users = userService.findAll()
            val userResponses = users.map { userService.toUserResponse(it) }
            ResponseEntity.ok(userResponses)
        } catch (e: Exception) {
            ResponseEntity.status(500).build()
        }
    }
}
