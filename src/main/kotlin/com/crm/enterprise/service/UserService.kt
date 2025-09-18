package com.crm.enterprise.service

import com.crm.enterprise.dto.UserResponse
import com.crm.enterprise.dto.UserCreateRequest
import com.crm.enterprise.dto.UserUpdateRequest
import com.crm.enterprise.entity.User
import com.crm.enterprise.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class UserService(
    private val userRepository: UserRepository
) {
    
    fun findById(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }
    
    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
    
    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }
    
    fun findByCompanyId(companyId: Long): List<User> {
        return userRepository.findByCompanyId(companyId).toList()
    }
    
    fun findActiveByCompanyId(companyId: Long): List<User> {
        return userRepository.findActiveByCompanyId(companyId).toList()
    }
    
    fun findAll(): List<User> {
        return userRepository.findAll().toList()
    }
    
    fun save(user: User): User {
        return userRepository.save(user)
    }
    
    fun deleteById(id: Long) {
        userRepository.deleteById(id)
    }
    
    fun toUserResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id ?: 0L,
            email = user.email,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            fullName = user.fullName,
            phone = user.phone,
            isActive = user.isActive,
            isSuperuser = user.isSuperuser,
            isCompanyAdmin = user.isCompanyAdmin,
            companyId = user.companyId,
            lastLogin = user.lastLogin?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }

    fun findUsersByCompanyId(companyId: Long): List<UserResponse> {
        return userRepository.findByCompanyId(companyId)
            .map { toUserResponse(it) }
    }

    fun findByIdAndCompanyId(id: Long, companyId: Long): UserResponse? {
        return userRepository.findById(id)
            .filter { it.companyId == companyId }
            .map { toUserResponse(it) }
            .orElse(null)
    }

    fun createUser(userRequest: UserCreateRequest, companyId: Long): UserResponse {
        val user = User(
            email = userRequest.email,
            username = userRequest.username,
            hashedPassword = userRequest.password, // In production, this should be hashed
            firstName = userRequest.firstName,
            lastName = userRequest.lastName,
            phone = userRequest.phone,
            isActive = userRequest.isActive,
            isCompanyAdmin = userRequest.isCompanyAdmin,
            companyId = companyId
        )
        val savedUser = userRepository.save(user)
        return toUserResponse(savedUser)
    }

    fun updateUser(id: Long, updateRequest: UserUpdateRequest, companyId: Long): UserResponse? {
        return userRepository.findById(id)
            .filter { it.companyId == companyId }
            .map { existingUser ->
                val updatedUser = existingUser.copy(
                    email = updateRequest.email ?: existingUser.email,
                    username = updateRequest.username ?: existingUser.username,
                    firstName = updateRequest.firstName ?: existingUser.firstName,
                    lastName = updateRequest.lastName ?: existingUser.lastName,
                    phone = updateRequest.phone ?: existingUser.phone,
                    isActive = updateRequest.isActive ?: existingUser.isActive,
                    isCompanyAdmin = updateRequest.isCompanyAdmin ?: existingUser.isCompanyAdmin,
                    updatedAt = LocalDateTime.now()
                )
                val savedUser = userRepository.save(updatedUser)
                toUserResponse(savedUser)
            }
            .orElse(null)
    }

    fun deleteUser(id: Long, companyId: Long): Boolean {
        return userRepository.findById(id)
            .filter { it.companyId == companyId }
            .map {
                userRepository.delete(it)
                true
            }
            .orElse(false)
    }

    fun activateUser(id: Long, companyId: Long): UserResponse? {
        return updateUser(id, UserUpdateRequest(isActive = true), companyId)
    }

    fun deactivateUser(id: Long, companyId: Long): UserResponse? {
        return updateUser(id, UserUpdateRequest(isActive = false), companyId)
    }
}
