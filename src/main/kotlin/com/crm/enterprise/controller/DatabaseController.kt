package com.crm.enterprise.controller

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/database")
class DatabaseController(
    private val jdbcTemplate: JdbcTemplate
) {
    
    @PostMapping("/fix-schema")
    fun fixSchema(): Map<String, Any> {
        return try {
            // Check if assigned_user_id column already exists
            val columns = jdbcTemplate.queryForList("SHOW COLUMNS FROM leads LIKE 'assigned_user_id'")
            if (columns.isEmpty()) {
                // Add the new column
                jdbcTemplate.execute("ALTER TABLE leads ADD COLUMN assigned_user_id BIGINT")
                
                // Copy data from old column if it exists
                try {
                    jdbcTemplate.execute("UPDATE leads SET assigned_user_id = assigned_to_id WHERE assigned_to_id IS NOT NULL")
                } catch (e: Exception) {
                    // Old column doesn't exist, that's fine
                }
                
                // Drop old column if it exists
                try {
                    jdbcTemplate.execute("ALTER TABLE leads DROP COLUMN assigned_to_id")
                } catch (e: Exception) {
                    // Old column doesn't exist, that's fine
                }
                
                // Add foreign key constraint
                jdbcTemplate.execute("ALTER TABLE leads ADD CONSTRAINT fk_leads_assigned_user FOREIGN KEY (assigned_user_id) REFERENCES users(id)")
            }
            
            mapOf("success" to true, "message" to "Database schema fixed successfully")
        } catch (e: Exception) {
            mapOf("success" to false, "message" to "Error fixing schema: ${e.message}")
        }
    }
    
    @PostMapping("/seed-data")
    fun seedData(): Map<String, Any> {
        return try {
            // Add sample leads data
            val insertSql = """
                INSERT IGNORE INTO leads (first_name, last_name, email, phone, company, job_title, source, status, score, notes, assigned_user_id, company_id, created_at, updated_at) VALUES
                ('John', 'Doe', 'john.doe@example.com', '+1-555-0101', 'Acme Corp', 'CEO', 'WEBSITE', 'NEW', 85, 'Interested in our premium package', 1, 1, NOW(), NOW()),
                ('Jane', 'Smith', 'jane.smith@techcorp.com', '+1-555-0102', 'TechCorp Inc', 'CTO', 'REFERRAL', 'CONTACTED', 92, 'Very interested, follow up next week', 1, 1, NOW(), NOW()),
                ('Mike', 'Johnson', 'mike.j@startup.io', '+1-555-0103', 'StartupIO', 'Founder', 'LINKEDIN', 'QUALIFIED', 78, 'Looking for enterprise solution', 1, 1, NOW(), NOW()),
                ('Sarah', 'Wilson', 'sarah.w@bigcorp.com', '+1-555-0104', 'BigCorp Ltd', 'VP Sales', 'EMAIL', 'NEW', 65, 'Initial inquiry about pricing', 1, 1, NOW(), NOW()),
                ('David', 'Brown', 'david.b@innovate.com', '+1-555-0105', 'InnovateTech', 'Director', 'TRADE_SHOW', 'CONTACTED', 88, 'High priority lead, very interested', 1, 1, NOW(), NOW())
            """.trimIndent()
            
            jdbcTemplate.execute(insertSql)
            
            mapOf("success" to true, "message" to "Sample data seeded successfully")
        } catch (e: Exception) {
            mapOf("success" to false, "message" to "Error seeding data: ${e.message}")
        }
    }
}