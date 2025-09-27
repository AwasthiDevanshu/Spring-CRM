//package com.crm.enterprise.controller
//
//import com.crm.enterprise.entity.PaymentReminder
//import com.crm.enterprise.entity.ReminderStatus
////import com.crm.enterprise.service.PaymentReminderService
//import com.crm.enterprise.util.RequestUtils
//import org.slf4j.LoggerFactory
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.*
//import java.math.BigDecimal
//import jakarta.servlet.http.HttpServletRequest
//
//@RestController
//@RequestMapping("/api/payment-reminders")
//class PaymentReminderController(
////    private val paymentReminderService: PaymentReminderService,
//    private val requestUtils: RequestUtils
//) {
//    private val logger = LoggerFactory.getLogger(PaymentReminderController::class.java)
//
//    /**
//     * Get all pending reminders for the company
//     */
//    @GetMapping
//    fun getPendingReminders(request: HttpServletRequest): ResponseEntity<List<PaymentReminder>> {
//        try {
//            val companyId = requestUtils.extractCompanyIdFromToken(request)
//                ?: return ResponseEntity.badRequest().build()
//
//            val reminders = paymentReminderService.getPendingReminders(companyId)
//            return ResponseEntity.ok(reminders)
//        } catch (e: Exception) {
//            logger.error("Error fetching pending reminders", e)
//            return ResponseEntity.internalServerError().build()
//        }
//    }
//
//    /**
//     * Get overdue reminders for the company
//     */
//    @GetMapping("/overdue")
//    fun getOverdueReminders(request: HttpServletRequest): ResponseEntity<List<PaymentReminder>> {
//        try {
//            val companyId = requestUtils.extractCompanyIdFromToken(request)
//                ?: return ResponseEntity.badRequest().build()
//
//            val reminders = paymentReminderService.getOverdueReminders(companyId)
//            return ResponseEntity.ok(reminders)
//        } catch (e: Exception) {
//            logger.error("Error fetching overdue reminders", e)
//            return ResponseEntity.internalServerError().build()
//        }
//    }
//
//    /**
//     * Get reminders for a specific deal
//     */
//    @GetMapping("/deal/{dealId}")
//    fun getRemindersForDeal(
//        @PathVariable dealId: Long,
//        request: HttpServletRequest
//    ): ResponseEntity<List<PaymentReminder>> {
//        try {
//            val companyId = requestUtils.extractCompanyIdFromToken(request)
//                ?: return ResponseEntity.badRequest().build()
//
//            val reminders = paymentReminderService.getPendingReminders(companyId)
//                .filter { it.dealId == dealId }
//
//            return ResponseEntity.ok(reminders)
//        } catch (e: Exception) {
//            logger.error("Error fetching reminders for deal $dealId", e)
//            return ResponseEntity.internalServerError().build()
//        }
//    }
//
//    /**
//     * Mark a payment as received
//     */
//    @PostMapping("/{reminderId}/mark-paid")
//    fun markPaymentReceived(
//        @PathVariable reminderId: Long,
//        @RequestParam paidAmount: BigDecimal,
//        request: HttpServletRequest
//    ): ResponseEntity<String> {
//        try {
//            val companyId = requestUtils.extractCompanyIdFromToken(request)
//                ?: return ResponseEntity.badRequest().build()
//
//            paymentReminderService.markPaymentReceived(reminderId, paidAmount)
//            return ResponseEntity.ok("Payment marked as received")
//        } catch (e: Exception) {
//            logger.error("Error marking payment as received for reminder $reminderId", e)
//            return ResponseEntity.internalServerError().build()
//        }
//    }
//
//    /**
//     * Create delivery confirmation reminder
//     */
//    @PostMapping("/delivery-confirmation")
//    fun createDeliveryConfirmationReminder(
//        @RequestParam dealId: Long,
//        @RequestParam deliveryDate: String,
//        request: HttpServletRequest
//    ): ResponseEntity<String> {
//        try {
//            val companyId = requestUtils.extractCompanyIdFromToken(request)
//                ?: return ResponseEntity.badRequest().build()
//
//            val deliveryDateTime = java.time.LocalDateTime.parse(deliveryDate)
//            paymentReminderService.createDeliveryConfirmationReminder(dealId, deliveryDateTime)
//
//            return ResponseEntity.ok("Delivery confirmation reminder created")
//        } catch (e: Exception) {
//            logger.error("Error creating delivery confirmation reminder for deal $dealId", e)
//            return ResponseEntity.internalServerError().build()
//        }
//    }
//
//    /**
//     * Update reminder status
//     */
//    @PutMapping("/{reminderId}/status")
//    fun updateReminderStatus(
//        @PathVariable reminderId: Long,
//        @RequestParam status: ReminderStatus,
//        request: HttpServletRequest
//    ): ResponseEntity<String> {
//        try {
//            val companyId = requestUtils.extractCompanyIdFromToken(request)
//                ?: return ResponseEntity.badRequest().build()
//
//            // TODO: Implement status update logic
//            return ResponseEntity.ok("Reminder status updated")
//        } catch (e: Exception) {
//            logger.error("Error updating reminder status for reminder $reminderId", e)
//            return ResponseEntity.internalServerError().build()
//        }
//    }
//}
