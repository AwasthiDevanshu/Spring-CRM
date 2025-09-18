package com.crm.enterprise

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EnterpriseCrmApplication

fun main(args: Array<String>) {
    runApplication<EnterpriseCrmApplication>(*args)
}