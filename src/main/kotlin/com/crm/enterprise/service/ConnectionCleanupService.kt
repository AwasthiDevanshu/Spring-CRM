package com.crm.enterprise.service

import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException

@Service
class ConnectionCleanupService {

    private val logger = LoggerFactory.getLogger(ConnectionCleanupService::class.java)

    @Autowired
    private lateinit var dataSource: DataSource

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    fun cleanupIdleConnections() {
        try {
            if (dataSource is HikariDataSource) {
                val hikariDataSource = dataSource as HikariDataSource
                val poolStats = hikariDataSource.hikariPoolMXBean
                
                logger.debug("Cleaning up idle connections. Current stats: Active=${poolStats.activeConnections}, Idle=${poolStats.idleConnections}")
                
                // Only evict if we have idle connections
                if (poolStats.idleConnections > 0) {
                    try {
                        hikariDataSource.evictConnection(null)
                    } catch (e: NullPointerException) {
                        logger.debug("No connections to evict (null connection)")
                    }
                }
                
                logger.debug("Idle connection cleanup completed")
            }
        } catch (e: Exception) {
            logger.error("Error during idle connection cleanup", e)
        }
    }

    @Scheduled(fixedRate = 1800000) // Every 30 minutes
    fun validateAndCleanupConnections() {
        try {
            if (dataSource is HikariDataSource) {
                val hikariDataSource = dataSource as HikariDataSource
                val poolStats = hikariDataSource.hikariPoolMXBean
                
                logger.info("Performing connection validation and cleanup")
                logger.info("Pool stats before cleanup: Active=${poolStats.activeConnections}, Idle=${poolStats.idleConnections}, Total=${poolStats.totalConnections}")
                
                // Test a few connections to ensure they're still valid
                testConnections(hikariDataSource)
                
                // Soft evict connections that might be stale
                if (poolStats.idleConnections > 0) {
                    try {
                        hikariDataSource.evictConnection(null)
                    } catch (e: NullPointerException) {
                        logger.debug("No connections to evict during validation")
                    }
                }
                
                logger.info("Pool stats after cleanup: Active=${poolStats.activeConnections}, Idle=${poolStats.idleConnections}, Total=${poolStats.totalConnections}")
            }
        } catch (e: Exception) {
            logger.error("Error during connection validation and cleanup", e)
        }
    }

    @Async
    fun handleConnectionError(connection: Connection?, error: SQLException) {
        try {
            logger.warn("Handling connection error: ${error.message}")
            
            if (connection != null && !connection.isClosed) {
                try {
                    // Try to rollback any pending transaction
                    if (!connection.autoCommit) {
                        connection.rollback()
                    }
                } catch (rollbackException: SQLException) {
                    logger.warn("Failed to rollback connection: ${rollbackException.message}")
                }
                
                try {
                    // Close the problematic connection
                    connection.close()
                    logger.info("Successfully closed problematic connection")
                } catch (closeException: SQLException) {
                    logger.warn("Failed to close connection: ${closeException.message}")
                }
            }
            
            // Log the error for monitoring
            logConnectionError(error)
            
        } catch (e: Exception) {
            logger.error("Error in connection error handler", e)
        }
    }

    private fun testConnections(hikariDataSource: HikariDataSource) {
        try {
            // Test a few connections to ensure they're working
            repeat(3) { index ->
                try {
                    val connection = hikariDataSource.connection
                    val isValid = connection.isValid(5) // 5 second timeout
                    
                    if (isValid) {
                        logger.debug("Connection test $index: Valid")
                    } else {
                        logger.warn("Connection test $index: Invalid - will be evicted")
                    }
                    
                    connection.close()
                } catch (e: SQLException) {
                    logger.warn("Connection test $index failed: ${e.message}")
                }
            }
        } catch (e: Exception) {
            logger.error("Error during connection testing", e)
        }
    }

    private fun logConnectionError(error: SQLException) {
        val errorInfo = mapOf(
            "sqlState" to (error.sqlState ?: "Unknown"),
            "errorCode" to error.errorCode.toString(),
            "message" to (error.message ?: "Unknown error"),
            "nextException" to (error.nextException?.message ?: "None")
        )
        
        logger.error("SQL Connection Error Details: $errorInfo")
        
        // You could also send this to a monitoring system
        // monitoringService.recordConnectionError(errorInfo)
    }

    fun getConnectionPoolStatus(): Map<String, Any> {
        return try {
            if (dataSource is HikariDataSource) {
                val hikariDataSource = dataSource as HikariDataSource
                val poolStats = hikariDataSource.hikariPoolMXBean
                
                mapOf(
                    "poolName" to "CRM-HikariCP",
                    "activeConnections" to poolStats.activeConnections,
                    "idleConnections" to poolStats.idleConnections,
                    "totalConnections" to poolStats.totalConnections,
                    "threadsAwaitingConnection" to poolStats.threadsAwaitingConnection,
                    "maximumPoolSize" to hikariDataSource.maximumPoolSize,
                    "minimumIdle" to hikariDataSource.minimumIdle,
                    "connectionTimeout" to hikariDataSource.connectionTimeout,
                    "idleTimeout" to hikariDataSource.idleTimeout,
                    "maxLifetime" to hikariDataSource.maxLifetime,
                    "leakDetectionThreshold" to hikariDataSource.leakDetectionThreshold
                )
            } else {
                mapOf("error" to "DataSource is not HikariCP")
            }
        } catch (e: Exception) {
            mapOf("error" to (e.message ?: "Unknown error"))
        }
    }
}
