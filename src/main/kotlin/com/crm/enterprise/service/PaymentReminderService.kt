package com.crm.enterprise.service

import com.crm.enterprise.entity.*
import com.crm.enterprise.repository.DealRepository
import com.crm.enterprise.repository.PaymentReminderRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
@Transactional
class PaymentReminderService(
    private val dealRepository: DealRepository,
    private val paymentReminderRepository: PaymentReminderRepository
) {
    private val logger = LoggerFactory.getLogger(PaymentReminderService::class.java)

    /**
     * Create payment reminders for a deal
     */
    fun createPaymentReminders(deal: Deal) {
        when (deal.paymentType) {
            PaymentType.ONE_TIME -> {
                // Create single payment reminder
                createSinglePaymentReminder(deal)
            }
            PaymentType.EMI -> {
                // Create EMI payment reminders
                createEMIPaymentReminders(deal)
            }
            PaymentType.RECURRING -> {
                // Create recurring payment reminders
                createRecurringPaymentReminders(deal)
            }
        }
    }

    /**
     * Create a single payment reminder for one-time payments
     */
    private fun createSinglePaymentReminder(deal: Deal) {
        val reminder = PaymentReminder(
            dealId = deal.id!!,
            contactId = deal.contactId,
            type = com.crm.enterprise.entity.ReminderType.PAYMENT_DUE,
            subject = "Payment Due - ${deal.name}",
            message = "Payment of ${deal.value} ${deal.currency} is due for deal: ${deal.name}",
            amount = deal.value,
            dueDate = deal.expectedCloseDate ?: LocalDateTime.now().plusDays(7),
            priority = ReminderPriority.HIGH,
            companyId = deal.companyId,
            createdBy = deal.assignedUserId ?: 1L
        )
        paymentReminderRepository.save(reminder)
        logger.info("Created single payment reminder for deal ${deal.id}")
    }

    /**
     * Create EMI payment reminders
     */
    private fun createEMIPaymentReminders(deal: Deal) {
        val installmentCount = deal.installmentCount ?: 1
        val installmentAmount = deal.installmentAmount ?: deal.value
        val frequency = deal.installmentFrequency ?: "MONTHLY"
        
        for (i in 1..installmentCount) {
            val dueDate = calculateNextPaymentDate(deal.nextPaymentDate ?: LocalDateTime.now(), frequency, i - 1)
            
            val reminder = PaymentReminder(
                dealId = deal.id!!,
                contactId = deal.contactId,
                type = com.crm.enterprise.entity.ReminderType.PAYMENT_DUE,
                subject = "EMI Payment Due - ${deal.name} (${i}/${installmentCount})",
                message = "EMI payment of ${installmentAmount} ${deal.currency} is due for deal: ${deal.name}. Installment ${i} of ${installmentCount}",
                amount = installmentAmount,
                dueDate = dueDate,
                priority = if (i == 1) ReminderPriority.HIGH else ReminderPriority.MEDIUM,
                companyId = deal.companyId,
                createdBy = deal.assignedUserId ?: 1L
            )
            paymentReminderRepository.save(reminder)
        }
        logger.info("Created ${installmentCount} EMI payment reminders for deal ${deal.id}")
    }

    /**
     * Create recurring payment reminders
     */
    private fun createRecurringPaymentReminders(deal: Deal) {
        val frequency = deal.installmentFrequency ?: "MONTHLY"
        val installmentAmount = deal.installmentAmount ?: deal.value
        
        // Create reminders for next 12 months
        for (i in 1..12) {
            val dueDate = calculateNextPaymentDate(deal.nextPaymentDate ?: LocalDateTime.now(), frequency, i - 1)
            
            val reminder = PaymentReminder(
                dealId = deal.id!!,
                contactId = deal.contactId,
                type = com.crm.enterprise.entity.ReminderType.PAYMENT_DUE,
                subject = "Recurring Payment Due - ${deal.name} (Month ${i})",
                message = "Recurring payment of ${installmentAmount} ${deal.currency} is due for deal: ${deal.name}",
                amount = installmentAmount,
                dueDate = dueDate,
                priority = ReminderPriority.MEDIUM,
                companyId = deal.companyId,
                createdBy = deal.assignedUserId ?: 1L
            )
            paymentReminderRepository.save(reminder)
        }
        logger.info("Created 12 recurring payment reminders for deal ${deal.id}")
    }

    /**
     * Calculate next payment date based on frequency
     */
    private fun calculateNextPaymentDate(startDate: LocalDateTime, frequency: String, installmentNumber: Int): LocalDateTime {
        return when (frequency.uppercase()) {
            "WEEKLY" -> startDate.plusWeeks(installmentNumber.toLong())
            "MONTHLY" -> startDate.plusMonths(installmentNumber.toLong())
            "QUARTERLY" -> startDate.plusMonths(installmentNumber * 3L)
            "YEARLY" -> startDate.plusYears(installmentNumber.toLong())
            else -> startDate.plusMonths(installmentNumber.toLong())
        }
    }

    /**
     * Scheduled task to check for overdue payments and create reminders
     */
    @Scheduled(cron = "0 0 9 * * ?") // Run daily at 9 AM
    fun checkOverduePayments() {
        logger.info("Checking for overdue payments...")
        
        val overdueReminders = paymentReminderRepository.findOverdueReminders(LocalDateTime.now())
        
        overdueReminders.forEach { reminder ->
            // Update reminder status to overdue
            val updatedReminder = reminder.copy(
                status = ReminderStatus.PENDING,
                priority = ReminderPriority.URGENT,
                updatedAt = LocalDateTime.now()
            )
            paymentReminderRepository.save(updatedReminder)
            
            // Create follow-up reminder
            createFollowUpReminder(reminder)
            
            logger.info("Marked reminder ${reminder.id} as overdue")
        }
    }

    /**
     * Create follow-up reminder for overdue payments
     */
    private fun createFollowUpReminder(originalReminder: PaymentReminder) {
        val followUpReminder = PaymentReminder(
            dealId = originalReminder.dealId,
            contactId = originalReminder.contactId,
            type = ReminderType.FOLLOW_UP,
            subject = "Follow-up: Overdue Payment - ${originalReminder.subject}",
            message = "This is a follow-up reminder for the overdue payment. Please contact us immediately to resolve this matter.",
            amount = originalReminder.amount,
            dueDate = LocalDateTime.now().plusDays(3),
            status = ReminderStatus.PENDING,
            priority = ReminderPriority.URGENT,
            companyId = originalReminder.companyId,
            createdBy = originalReminder.createdBy
        )
        paymentReminderRepository.save(followUpReminder)
    }

    /**
     * Mark payment as received and update next payment date
     */
    fun markPaymentReceived(reminderId: Long, paidAmount: BigDecimal) {
        val reminder = paymentReminderRepository.findById(reminderId)
            .orElseThrow { RuntimeException("Reminder not found") }
        
        // Update reminder status
        val updatedReminder = reminder.copy(
            status = ReminderStatus.ACKNOWLEDGED,
            acknowledgedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        paymentReminderRepository.save(updatedReminder)
        
        // Update deal with last payment date
        val deal = dealRepository.findById(reminder.dealId)
            .orElseThrow { RuntimeException("Deal not found") }
        
        val updatedDeal = deal.copy(
            lastPaymentDate = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        dealRepository.save(updatedDeal)
        
        logger.info("Marked payment as received for reminder ${reminderId}")
    }

    /**
     * Create delivery confirmation reminder
     */
    fun createDeliveryConfirmationReminder(dealId: Long, deliveryDate: LocalDateTime) {
        val deal = dealRepository.findById(dealId)
            .orElseThrow { RuntimeException("Deal not found") }
        
        val reminder = PaymentReminder(
            dealId = dealId,
            contactId = deal.contactId,
            type = com.crm.enterprise.entity.ReminderType.DELIVERY_CONFIRMATION,
            subject = "Delivery Confirmation Required - ${deal.name}",
            message = "Please confirm that the delivery for deal ${deal.name} was completed successfully on ${deliveryDate.toLocalDate()}",
            dueDate = deliveryDate.plusDays(1),
            priority = ReminderPriority.MEDIUM,
            companyId = deal.companyId,
            createdBy = deal.assignedUserId ?: 1L
        )
        paymentReminderRepository.save(reminder)
        
        logger.info("Created delivery confirmation reminder for deal ${dealId}")
    }

    /**
     * Get all pending reminders for a company
     */
    fun getPendingReminders(companyId: Long): List<PaymentReminder> {
        return paymentReminderRepository.findByCompanyIdAndStatus(companyId, ReminderStatus.PENDING)
    }

    /**
     * Get overdue reminders for a company
     */
    fun getOverdueReminders(companyId: Long): List<PaymentReminder> {
        return paymentReminderRepository.findOverdueRemindersByCompany(companyId, LocalDateTime.now())
    }
}
