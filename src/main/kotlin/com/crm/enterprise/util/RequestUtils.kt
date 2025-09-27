package com.crm.enterprise.util

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class RequestUtils(
    private val jwtUtils: JwtUtils
) {

    fun extractCompanyIdFromToken(request: HttpServletRequest): Long? {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            return jwtUtils.extractCompanyId(token)
        }
        return null
    }

    fun extractUserIdFromToken(request: HttpServletRequest): Long? {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            return jwtUtils.extractUserId(token)
        }
        return null
    }

    fun extractUsernameFromToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            return jwtUtils.extractUsername(token)
        }
        return null
    }

    fun extractIsSuperuserFromToken(request: HttpServletRequest): Boolean? {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            return jwtUtils.extractIsSuperuser(token)
        }
        return null
    }

    fun extractIsCompanyAdminFromToken(request: HttpServletRequest): Boolean? {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            return jwtUtils.extractIsCompanyAdmin(token)
        }
        return null
    }
}
