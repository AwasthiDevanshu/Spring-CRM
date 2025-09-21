package com.crm.enterprise.service

import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import javax.sql.DataSource

@Service
class DatabaseShutdownService {

    private val logger = LoggerFactory.getLogger(DatabaseShutdownService::class.java)

    @Autowired
    private lateinit var dataSource: DataSource

    @EventListener(ContextClosedEvent::class)
    fun handleContextClosed(event: ContextClosedEvent) {
        logger.info("=== DATABASE SHUTDOWN SERVICE ===")
        logger.info("Application context is closing. Initiating database cleanup...")
        
        try {
            if (dataSource is HikariDataSource) {
                val hikariDataSource = dataSource as HikariDataSource
                
                // Log current pool status
                val poolStats = hikariDataSource.hikariPoolMXBean
                logger.info("""
                    |Current Pool Status:
                    |  Active Connections: ${poolStats.activeConnections}
                    |  Idle Connections: ${poolStats.idleConnections}
                    |  Total Connections: ${poolStats.totalConnections}
                    |  Threads Awaiting: ${poolStats.threadsAwaitingConnection}
                    |  Pool Name: ${hikariDataSource.poolName}
                """.trimMargin())
                
                // Soft shutdown - wait for active connections to complete
                logger.info("Initiating soft shutdown of connection pool...")
                hikariDataSource.close()
                
                // Wait a moment for graceful shutdown
                Thread.sleep(2000)
                
                // Force close if still open
                if (!hikariDataSource.isClosed) {
                    logger.warn("Pool still open after soft shutdown. Force closing...")
                    hikariDataSource.close()
                }
                
                logger.info("Database connection pool closed successfully")
                
            } else {
                logger.warn("DataSource is not HikariDataSource, cannot perform detailed cleanup")
            }
            
        } catch (e: Exception) {
            logger.error("Error during database shutdown cleanup", e)
        }
        
        logger.info("=== DATABASE SHUTDOWN COMPLETE ===")
    }

    /**
     * Manual cleanup method that can be called programmatically
     */
    fun forceCleanup() {
        logger.info("Manual database cleanup requested...")
        
        try {
            if (dataSource is HikariDataSource) {
                val hikariDataSource = dataSource as HikariDataSource
                
                if (!hikariDataSource.isClosed) {
                    logger.info("Force closing database connections...")
                    hikariDataSource.close()
                    logger.info("Database connections force closed")
                } else {
                    logger.info("Database connections already closed")
                }
            }
        } catch (e: Exception) {
            logger.error("Error during manual database cleanup", e)
        }
    }

    /**
     * Get current connection pool status
     */
    fun getConnectionPoolStatus(): Map<String, Any> {
        return try {
            if (dataSource is HikariDataSource) {
                val hikariDataSource = dataSource as HikariDataSource
                val poolStats = hikariDataSource.hikariPoolMXBean
                
                mapOf(
                    "isClosed" to hikariDataSource.isClosed,
                    "activeConnections" to poolStats.activeConnections,
                    "idleConnections" to poolStats.idleConnections,
                    "totalConnections" to poolStats.totalConnections,
                    "threadsAwaitingConnection" to poolStats.threadsAwaitingConnection,
                    "poolName" to hikariDataSource.poolName
                )
            } else {
                mapOf("error" to "DataSource is not HikariDataSource")
            }
        } catch (e: Exception) {
            mapOf("error" to (e.message ?: "Unknown error"))
        }
    }
}
