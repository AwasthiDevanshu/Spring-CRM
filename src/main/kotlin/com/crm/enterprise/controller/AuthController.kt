package com.crm.enterprise.controller

import com.crm.enterprise.dto.LoginRequest
import com.crm.enterprise.dto.LoginResponse
import com.crm.enterprise.dto.UserResponse
import com.crm.enterprise.service.UserService
import com.crm.enterprise.util.JwtUtils
import com.crm.enterprise.util.RequestUtils
import jakarta.servlet.http.HttpServletRequest
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
import java.util.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
class AuthController(
    private val userService: UserService,
    private val jwtUtils: JwtUtils,
    private val requestUtils: RequestUtils
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
            if(user.hashedPassword != loginRequest.password){
                return ResponseEntity.badRequest().body(
                    LoginResponse(success = false, message = "Password mismatch")
                )
            }
            
            // For demo purposes, accept any password
            // In production, you would verify the password hash
            val token = generateJwtToken(user)
            
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
        request: HttpServletRequest
    ): ResponseEntity<UserResponse> {
        return try {
            val companyId = requestUtils.extractCompanyIdFromToken(request)
            val userId = requestUtils.extractUserIdFromToken(request)
            
            if (companyId == null || userId == null) {
                return ResponseEntity.status(401).build()
            }

            // Get user by ID
            val user = userService.findById(userId)
            if (user == null) {
                return ResponseEntity.status(404).build()
            }

            ResponseEntity.ok(userService.toUserResponse(user))
        } catch (e: Exception) {
            ResponseEntity.status(401).build()
        }
    }

    @PostMapping("/logout")
    @Operation(
        summary = "User Logout",
        description = "Logout user and invalidate session/token"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Logout successful"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Logout failed"
            )
        ]
    )
    fun logout(): ResponseEntity<Map<String, String>> {
        return try {
            // In a production environment, you would:
            // 1. Add the token to a blacklist
            // 2. Invalidate the session
            // 3. Clear any server-side session data
            
            // For now, we'll just return a success response
            // The client will handle token removal
            ResponseEntity.ok(mapOf(
                "success" to "true",
                "message" to "Logout successful"
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to "false",
                "message" to "Logout failed: ${e.message}"
            ))
        }
    }

    private fun generateJwtToken(user: com.crm.enterprise.entity.User): String {
        val now = Date()
        val expiration = Date(now.time + 24 * 60 * 60 * 1000) // 24 hours

        return io.jsonwebtoken.Jwts.builder()
            .setSubject(user.username)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .claim("userId", user.id)
            .claim("username", user.username)
            .claim("email", user.email)
            .claim("companyId", user.companyId)
            .claim("firstName", user.firstName)
            .claim("lastName", user.lastName)
            .claim("isSuperuser", user.isSuperuser)
            .claim("isCompanyAdmin", user.isCompanyAdmin)
            .signWith(jwtUtils.key, io.jsonwebtoken.SignatureAlgorithm.HS256)
            .compact()
    }
}
