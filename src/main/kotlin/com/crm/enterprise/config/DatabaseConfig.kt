package com.crm.enterprise.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import javax.sql.DataSource

@Configuration
@EnableScheduling
class DatabaseConfig(
    private val dataSource: DataSource
) {

    private val logger = LoggerFactory.getLogger(DatabaseConfig::class.java)

    @Bean
    fun jdbcTemplate(dataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(dataSource)
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    fun logConnectionPoolStats() {
        try {
            val hikariDataSource = dataSource as HikariDataSource
            val poolStats = hikariDataSource.hikariPoolMXBean
            
                logger.info("""
                |=== HikariCP Pool Statistics ===
                |Active Connections: ${poolStats.activeConnections}
                |Idle Connections: ${poolStats.idleConnections}
                |Total Connections: ${poolStats.totalConnections}
                |Threads Awaiting Connection: ${poolStats.threadsAwaitingConnection}
                |Pool Name: CRM-HikariCP
                |===============================
            """.trimMargin())
            
            // Check for potential connection leaks
            if (poolStats.threadsAwaitingConnection > 5) {
                logger.warn("High number of threads awaiting connection: ${poolStats.threadsAwaitingConnection}")
            }
            
            if (poolStats.activeConnections >= hikariDataSource.maximumPoolSize * 0.9) {
                logger.warn("Connection pool is near capacity: ${poolStats.activeConnections}/${hikariDataSource.maximumPoolSize}")
            }
            
        } catch (e: Exception) {
            logger.error("Error retrieving connection pool statistics", e)
        }
    }

    @Scheduled(fixedRate = 600000) // Every 10 minutes
    fun validateConnections() {
        try {
            val hikariDataSource = dataSource as HikariDataSource
            val jdbcTemplate = JdbcTemplate(hikariDataSource)
            
            // Test connection by running a simple query
            val result = jdbcTemplate.queryForObject("SELECT 1", Int::class.java)
            if (result == 1) {
                logger.debug("Database connection validation successful")
            } else {
                logger.warn("Database connection validation returned unexpected result: $result")
            }
            
        } catch (e: Exception) {
            logger.error("Database connection validation failed", e)
        }
    }

    @EventListener(ContextClosedEvent::class)
    fun onApplicationShutdown(event: ContextClosedEvent) {
        logger.info("Application is shutting down. Cleaning up database connections...")
        
        try {
            val hikariDataSource = dataSource as HikariDataSource
            
            // Log final connection pool statistics
            val poolStats = hikariDataSource.hikariPoolMXBean
            logger.info("""
                |=== Final HikariCP Pool Statistics ===
                |Active Connections: ${poolStats.activeConnections}
                |Idle Connections: ${poolStats.idleConnections}
                |Total Connections: ${poolStats.totalConnections}
                |Threads Awaiting Connection: ${poolStats.threadsAwaitingConnection}
                |Pool Name: CRM-HikariCP
                |=====================================
            """.trimMargin())
            
            // Force close all connections
            if (!hikariDataSource.isClosed) {
                logger.info("Closing HikariCP DataSource...")
                hikariDataSource.close()
                logger.info("HikariCP DataSource closed successfully")
            } else {
                logger.info("HikariCP DataSource was already closed")
            }
            
        } catch (e: Exception) {
            logger.error("Error during database connection cleanup", e)
        }
        
        logger.info("Database connection cleanup completed")
    }
}
