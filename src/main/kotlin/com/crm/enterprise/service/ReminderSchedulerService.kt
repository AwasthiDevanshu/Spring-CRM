package com.crm.enterprise.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ReminderSchedulerService(
    private val reminderService: ReminderService
) {
    
    private val logger = LoggerFactory.getLogger(ReminderSchedulerService::class.java)
    
    // Run every minute to check for due reminders
    @Scheduled(fixedRate = 60000) // 60 seconds
    fun processDueReminders() {
        try {
            // Process reminders for all companies
            // In a real implementation, you would iterate through all companies
            reminderService.processDueReminders()
        } catch (e: Exception) {
            // Log error but don't let it crash the scheduler
            logger.error("Error processing due reminders: {}", e.message, e)
        }
    }
    
    // Run every hour to clean up old sent reminders
    @Scheduled(fixedRate = 3600000) // 1 hour
    fun cleanupOldReminders() {
        try {
            // Clean up reminders that were sent more than 30 days ago
            val cutoffDate = LocalDateTime.now().minusDays(30)
            // Implementation would depend on your cleanup strategy
            logger.info("Cleaning up old reminders before: {}", cutoffDate)
        } catch (e: Exception) {
            logger.error("Error cleaning up old reminders: {}", e.message, e)
        }
    }
}
