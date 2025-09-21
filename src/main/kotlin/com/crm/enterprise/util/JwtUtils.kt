package com.crm.enterprise.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class JwtUtils(
    @Value("\${jwt.secret:your-secret-key-here-make-it-long-enough-for-hs256-algorithm}")
    private val jwtSecret: String
) {
    
    private val logger = LoggerFactory.getLogger(JwtUtils::class.java)
    val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray(StandardCharsets.UTF_8))

    fun extractCompanyId(token: String): Long? {
        return try {
            val claims = extractAllClaims(token)
            logger.debug("JWT Claims in extractCompanyId: {}", claims)
            val companyIdValue = claims["companyId"]
            logger.debug("companyId value: {} (type: {})", companyIdValue, companyIdValue?.javaClass?.simpleName)
            
            // Try to handle different number types
            when (val value = companyIdValue) {
                is Long -> {
                    logger.debug("Found Long companyId: {}", value)
                    value
                }
                is Int -> {
                    logger.debug("Found Int companyId: {}, converting to Long", value)
                    value.toLong()
                }
                is Number -> {
                    logger.debug("Found Number companyId: {}, converting to Long", value)
                    value.toLong()
                }
                is String -> {
                    logger.debug("Found String companyId: {}, converting to Long", value)
                    value.toLongOrNull()
                }
                else -> {
                    logger.warn("Unexpected companyId type: {} (value: {})", value?.javaClass?.simpleName, value)
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("Exception in extractCompanyId: {}", e.message, e)
            null
        }
    }

    fun extractUserId(token: String): Long? {
        return try {
            val claims = extractAllClaims(token)
            when (val value = claims["userId"]) {
                is Long -> value
                is Int -> value.toLong()
                is Number -> value.toLong()
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun extractUsername(token: String): String? {
        return try {
            val claims = extractAllClaims(token)
            claims["username"] as? String
        } catch (e: Exception) {
            null
        }
    }

    fun extractIsSuperuser(token: String): Boolean? {
        return try {
            val claims = extractAllClaims(token)
            claims["isSuperuser"] as? Boolean
        } catch (e: Exception) {
            null
        }
    }

    fun extractIsCompanyAdmin(token: String): Boolean? {
        return try {
            val claims = extractAllClaims(token)
            claims["isCompanyAdmin"] as? Boolean
        } catch (e: Exception) {
            null
        }
    }

    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = extractAllClaims(token)
            claims.expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
}
