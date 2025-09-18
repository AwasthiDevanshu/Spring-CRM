-- Fix database schema issues
-- Add missing column if it doesn't exist
ALTER TABLE leads ADD COLUMN IF NOT EXISTS assigned_user_id BIGINT;

-- Update existing data to use the new column name
UPDATE leads SET assigned_user_id = assigned_to_id WHERE assigned_to_id IS NOT NULL;

-- Drop the old column if it exists
ALTER TABLE leads DROP COLUMN IF EXISTS assigned_to_id;

-- Add foreign key constraint if it doesn't exist
ALTER TABLE leads ADD CONSTRAINT IF NOT EXISTS fk_leads_assigned_user 
    FOREIGN KEY (assigned_user_id) REFERENCES users(id);

-- Add some sample leads data
INSERT IGNORE INTO leads (first_name, last_name, email, phone, company, job_title, source, status, score, notes, assigned_user_id, company_id, created_at, updated_at) VALUES
('John', 'Doe', 'john.doe@example.com', '+1-555-0101', 'Acme Corp', 'CEO', 'WEBSITE', 'NEW', 85, 'Interested in our premium package', 1, 1, NOW(), NOW()),
('Jane', 'Smith', 'jane.smith@techcorp.com', '+1-555-0102', 'TechCorp Inc', 'CTO', 'REFERRAL', 'CONTACTED', 92, 'Very interested, follow up next week', 1, 1, NOW(), NOW()),
('Mike', 'Johnson', 'mike.j@startup.io', '+1-555-0103', 'StartupIO', 'Founder', 'LINKEDIN', 'QUALIFIED', 78, 'Looking for enterprise solution', 1, 1, NOW(), NOW()),
('Sarah', 'Wilson', 'sarah.w@bigcorp.com', '+1-555-0104', 'BigCorp Ltd', 'VP Sales', 'EMAIL', 'NEW', 65, 'Initial inquiry about pricing', 1, 1, NOW(), NOW()),
('David', 'Brown', 'david.b@innovate.com', '+1-555-0105', 'InnovateTech', 'Director', 'TRADE_SHOW', 'CONTACTED', 88, 'High priority lead, very interested', 1, 1, NOW(), NOW());
