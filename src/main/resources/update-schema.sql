-- Update database schema to ensure proper company relationships

-- Add company_id to contacts table if it doesn't exist
ALTER TABLE contacts ADD COLUMN IF NOT EXISTS company_id BIGINT;

-- Add foreign key constraint for contacts.company_id
ALTER TABLE contacts ADD CONSTRAINT IF NOT EXISTS fk_contacts_company 
    FOREIGN KEY (company_id) REFERENCES companies(id);

-- Add index for better performance
CREATE INDEX IF NOT EXISTS idx_contacts_company_id ON contacts(company_id);

-- Update existing contacts to have a default company_id (assuming company with id=1 exists)
UPDATE contacts SET company_id = 1 WHERE company_id IS NULL;

-- Make company_id NOT NULL after setting default values
ALTER TABLE contacts MODIFY COLUMN company_id BIGINT NOT NULL;

-- Ensure all other tables have proper company_id relationships
-- Leads table already has company_id, so we're good there
-- Users table already has company_id, so we're good there
-- Deals table already has company_id, so we're good there
-- Activities table already has company_id, so we're good there

-- Add any missing indexes for better performance
CREATE INDEX IF NOT EXISTS idx_leads_company_id ON leads(company_id);
CREATE INDEX IF NOT EXISTS idx_users_company_id ON users(company_id);
CREATE INDEX IF NOT EXISTS idx_deals_company_id ON deals(company_id);
CREATE INDEX IF NOT EXISTS idx_activities_company_id ON activities(company_id);
CREATE INDEX IF NOT EXISTS idx_notes_company_id ON notes(company_id);
CREATE INDEX IF NOT EXISTS idx_custom_fields_company_id ON custom_fields(company_id);
CREATE INDEX IF NOT EXISTS idx_custom_field_values_company_id ON custom_field_values(company_id);

-- Ensure companies table has proper structure
ALTER TABLE companies MODIFY COLUMN name VARCHAR(255) NOT NULL;
ALTER TABLE companies MODIFY COLUMN is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE companies MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE companies MODIFY COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
