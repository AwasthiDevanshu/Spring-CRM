package com.crm.enterprise.controller

import com.crm.enterprise.repository.LeadRepository
import com.crm.enterprise.repository.UserRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class TestController(
    private val userRepository: UserRepository,
    private val leadRepository: LeadRepository
) {
    
    @GetMapping("/users")
    fun getUsers(): Map<String, Any> {
        val users = userRepository.findAll().toList()
        return mapOf(
            "count" to users.size,
            "users" to users.map { mapOf(
                "id" to it.id,
                "email" to it.email,
                "firstName" to it.firstName,
                "lastName" to it.lastName,
                "companyId" to it.companyId
            ) }
        )
    }
    
    @GetMapping("/leads")
    fun getLeads(): Map<String, Any> {
        val leads = leadRepository.findAll().toList()
        return mapOf(
            "count" to leads.size,
            "leads" to leads.map { mapOf(
                "id" to it.id,
                "firstName" to it.firstName,
                "lastName" to it.lastName,
                "email" to it.email,
                "companyId" to it.companyId
            ) }
        )
    }
}

