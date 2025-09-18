package com.crm.enterprise.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val user: UserResponse? = null
)

data class UserResponse(
    val id: Long,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val phone: String? = null,
    val isActive: Boolean,
    val isSuperuser: Boolean,
    val isCompanyAdmin: Boolean,
    val companyId: Long,
    val lastLogin: String? = null
)

data class UserCreateRequest(
    val email: String,
    val username: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val isCompanyAdmin: Boolean = false,
    val isActive: Boolean = true
)

data class UserUpdateRequest(
    val email: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val isCompanyAdmin: Boolean? = null,
    val isActive: Boolean? = null
)

