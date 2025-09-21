package com.crm.enterprise.controller

import com.crm.enterprise.util.RequestUtils
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import jakarta.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/chatbot")
class ChatbotController(
    private val requestUtils: RequestUtils
) {
    private val logger = LoggerFactory.getLogger(ChatbotController::class.java)

    data class ChatbotRequest(
        val message: String,
        val context: Map<String, Any>? = null
    )

    data class ChatbotResponse(
        val answer: String,
        val suggestions: List<String>,
        val confidence: Double,
        val category: String? = null,
        val relatedQuestions: List<String>? = null
    )

    data class FAQ(
        val id: String,
        val question: String,
        val answer: String,
        val category: String,
        val keywords: List<String>,
        val page: String? = null
    )

    data class PageHelp(
        val suggestions: List<String>,
        val description: String
    )

    data class FeedbackRequest(
        val messageId: String,
        val feedback: String,
        val comment: String? = null
    )

    // FAQ Database
    private val faqDatabase = listOf(
        FAQ(
            id = "1",
            question = "How do I create a new lead?",
            answer = "To create a new lead, go to the Leads page and click the 'Add Lead' button. Fill in the required information like name, email, phone, and company details.",
            category = "Leads",
            keywords = listOf("create", "lead", "new", "add"),
            page = "/leads"
        ),
        FAQ(
            id = "2",
            question = "How do I convert a lead to a contact?",
            answer = "On the Leads page, click the three dots menu next to any lead and select 'Convert to Contact'. This will open a form to create a contact with the lead's information.",
            category = "Leads",
            keywords = listOf("convert", "contact", "lead", "change"),
            page = "/leads"
        ),
        FAQ(
            id = "3",
            question = "How do I create a deal?",
            answer = "You can create a deal from the Deals page or from a specific lead/contact. Click 'Add Deal' or use the 'Create Deal' option in the lead/contact dropdown menu.",
            category = "Deals",
            keywords = listOf("create", "deal", "new", "add", "sales"),
            page = "/deals"
        ),
        FAQ(
            id = "4",
            question = "How do I track activities?",
            answer = "Go to the Activities page to view all activities. You can create new activities and link them to leads, contacts, or deals. Activities help track your interactions and tasks.",
            category = "Activities",
            keywords = listOf("track", "activity", "task", "follow", "up"),
            page = "/activities"
        ),
        FAQ(
            id = "5",
            question = "How do I set up payment reminders?",
            answer = "Payment reminders are automatically created when you create deals with EMI or recurring payment types. You can view and manage them in the Payment Reminders section.",
            category = "Payments",
            keywords = listOf("payment", "reminder", "emi", "recurring", "billing"),
            page = "/deals"
        ),
        FAQ(
            id = "6",
            question = "How do I track delivery status?",
            answer = "When you mark a deal as delivered, the system automatically creates delivery confirmation reminders. You can track delivery status in the deal details or activities.",
            category = "Delivery",
            keywords = listOf("delivery", "track", "status", "shipped", "delivered"),
            page = "/deals"
        ),
        FAQ(
            id = "7",
            question = "How do I create a contact?",
            answer = "Go to the Contacts page and click 'Add Contact'. Fill in the contact information. You can also convert a lead to a contact from the Leads page.",
            category = "Contacts",
            keywords = listOf("create", "contact", "new", "add"),
            page = "/contacts"
        ),
        FAQ(
            id = "8",
            question = "How do I assign tasks to team members?",
            answer = "When creating activities, you can assign them to specific team members by entering their User ID. You can also assign deals and leads to team members.",
            category = "Team",
            keywords = listOf("assign", "task", "team", "member", "activity"),
            page = "/activities"
        ),
        FAQ(
            id = "9",
            question = "How do I export my data?",
            answer = "You can export your data from the Reports page. Select the data type (leads, contacts, deals) and choose your preferred format (CSV, Excel).",
            category = "Data",
            keywords = listOf("export", "download", "data", "csv", "excel", "reports"),
            page = "/reports"
        ),
        FAQ(
            id = "10",
            question = "What is the difference between a lead and a contact?",
            answer = "A lead is a potential customer who hasn't been qualified yet. A contact is a qualified person you have an ongoing relationship with. You convert leads to contacts when they're ready to do business.",
            category = "Process",
            keywords = listOf("lead", "contact", "difference", "qualify", "relationship"),
            page = "/leads"
        )
    )

    // Page-specific help
    private val pageHelp = mapOf(
        "/dashboard" to PageHelp(
            suggestions = listOf(
                "How do I view my sales performance?",
                "What are the quick actions available?",
                "How do I see recent activities?"
            ),
            description = "Your dashboard shows key metrics and quick actions"
        ),
        "/leads" to PageHelp(
            suggestions = listOf(
                "How do I create a new lead?",
                "How do I convert a lead to contact?",
                "How do I filter leads by status?"
            ),
            description = "Manage your potential customers and sales prospects"
        ),
        "/contacts" to PageHelp(
            suggestions = listOf(
                "How do I create a new contact?",
                "How do I create a deal from a contact?",
                "How do I track contact activities?"
            ),
            description = "Manage your qualified contacts and customer relationships"
        ),
        "/deals" to PageHelp(
            suggestions = listOf(
                "How do I create a new deal?",
                "How do I set up payment reminders?",
                "How do I track deal progress?"
            ),
            description = "Track your sales opportunities and revenue"
        ),
        "/activities" to PageHelp(
            suggestions = listOf(
                "How do I create a new activity?",
                "How do I link activities to leads/contacts?",
                "How do I track activity completion?"
            ),
            description = "Track your tasks, calls, meetings, and interactions"
        ),
        "/reports" to PageHelp(
            suggestions = listOf(
                "How do I generate sales reports?",
                "How do I export my data?",
                "What metrics are available?"
            ),
            description = "View insights and export your data"
        )
    )

    @PostMapping("/message")
    fun sendMessage(
        @RequestBody request: ChatbotRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ChatbotResponse> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(httpRequest)
                ?: return ResponseEntity.badRequest().build()

            val response = processMessage(request.message, request.context)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error processing chatbot message", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/faqs")
    fun getFAQs(@RequestParam(required = false) category: String?): ResponseEntity<List<FAQ>> {
        try {
            val faqs = if (category != null) {
                faqDatabase.filter { it.category.equals(category, ignoreCase = true) }
            } else {
                faqDatabase
            }
            return ResponseEntity.ok(faqs)
        } catch (e: Exception) {
            logger.error("Error fetching FAQs", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/page-help")
    fun getPageHelp(@RequestParam page: String): ResponseEntity<PageHelp> {
        try {
            val help = pageHelp[page] ?: PageHelp(
                suggestions = listOf(
                    "How do I create a new lead?",
                    "How do I track activities?",
                    "How do I set up payment reminders?"
                ),
                description = "General help for this page"
            )
            return ResponseEntity.ok(help)
        } catch (e: Exception) {
            logger.error("Error fetching page help for $page", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/search")
    fun searchKnowledgeBase(@RequestParam q: String): ResponseEntity<List<FAQ>> {
        try {
            val query = q.lowercase()
            val results = faqDatabase.filter { faq ->
                faq.question.lowercase().contains(query) ||
                faq.answer.lowercase().contains(query) ||
                faq.keywords.any { keyword -> keyword.lowercase().contains(query) }
            }.sortedByDescending { faq ->
                // Simple scoring based on keyword matches
                faq.keywords.count { keyword -> keyword.lowercase().contains(query) }
            }
            return ResponseEntity.ok(results)
        } catch (e: Exception) {
            logger.error("Error searching knowledge base for query: $q", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/feedback")
    fun submitFeedback(
        @RequestBody request: FeedbackRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<String> {
        try {
            val companyId = requestUtils.extractCompanyIdFromToken(httpRequest)
                ?: return ResponseEntity.badRequest().build()

            // TODO: Store feedback in database for analysis
            logger.info("Received feedback for message ${request.messageId}: ${request.feedback}")
            
            return ResponseEntity.ok("Feedback received")
        } catch (e: Exception) {
            logger.error("Error submitting feedback", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    private fun processMessage(message: String, context: Map<String, Any>?): ChatbotResponse {
        val query = message.lowercase()
        
        // Find best matching FAQ
        var bestMatch = faqDatabase[0]
        var bestScore = 0.0
        
        for (faq in faqDatabase) {
            var score = 0.0
            for (keyword in faq.keywords) {
                if (query.contains(keyword.lowercase())) {
                    score += 1.0
                }
            }
            if (faq.question.lowercase().contains(query)) {
                score += 2.0
            }
            if (faq.answer.lowercase().contains(query)) {
                score += 1.5
            }
            
            if (score > bestScore) {
                bestScore = score
                bestMatch = faq
            }
        }
        
        val confidence = if (bestScore > 0) {
            minOf(bestScore / 5.0, 1.0) // Normalize to 0-1
        } else {
            0.0
        }
        
        val answer = if (confidence > 0.3) {
            bestMatch.answer
        } else {
            "I'm not sure about that. Could you try rephrasing your question or ask about creating leads, contacts, deals, or activities?"
        }
        
        val suggestions = when {
            confidence > 0.7 -> listOf(
                "How do I create a new lead?",
                "How do I track activities?",
                "How do I set up payment reminders?"
            )
            else -> listOf(
                "How do I create a new lead?",
                "How do I convert a lead to contact?",
                "How do I create a deal?",
                "How do I track activities?"
            )
        }
        
        return ChatbotResponse(
            answer = answer,
            suggestions = suggestions,
            confidence = confidence,
            category = bestMatch.category,
            relatedQuestions = if (confidence > 0.5) {
                faqDatabase.filter { it.category == bestMatch.category && it.id != bestMatch.id }
                    .take(3)
                    .map { it.question }
            } else null
        )
    }
}
