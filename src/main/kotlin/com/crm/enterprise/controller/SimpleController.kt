package com.crm.enterprise.controller

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class SimpleController {

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "message" to "Backend is running",
            "timestamp" to System.currentTimeMillis().toString()
        )
    }

    @GetMapping("/hello")
    fun hello(): String {
        return "Hello from Spring Boot CRM!"
    }

    @PostMapping("/test")
    fun test(@RequestBody data: Map<String, Any>): Map<String, Any> {
        return mapOf(
            "message" to "Test endpoint working",
            "received" to data,
            "timestamp" to System.currentTimeMillis()
        )
    }
}
