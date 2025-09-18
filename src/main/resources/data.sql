-- Seed data for CRM system

-- Insert companies
INSERT INTO companies (name, description, website, phone, email, address, city, state, country, postal_code, is_active) VALUES
('Acme Corporation', 'Leading technology solutions provider', 'https://acme.com', '+1-555-0101', 'info@acme.com', '123 Tech Street', 'San Francisco', 'CA', 'USA', '94105', true),
('Global Enterprises', 'International business consulting', 'https://globalent.com', '+1-555-0102', 'contact@globalent.com', '456 Business Ave', 'New York', 'NY', 'USA', '10001', true);

-- Insert roles
INSERT INTO roles (name, description, permissions, is_active) VALUES
('ADMIN', 'System Administrator', '["ALL"]', true),
('MANAGER', 'Team Manager', '["READ", "WRITE", "MANAGE_TEAM"]', true),
('SALES_REP', 'Sales Representative', '["READ", "WRITE"]', true),
('USER', 'Regular User', '["READ"]', true);

-- Insert users
INSERT INTO users (email, username, hashed_password, first_name, last_name, phone, is_active, is_superuser, is_company_admin, company_id) VALUES
('admin@acme.com', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'John', 'Admin', '+1-555-1001', true, true, true, 1),
('manager@acme.com', 'manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Jane', 'Manager', '+1-555-1002', true, false, true, 1),
('sales@acme.com', 'sales', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Bob', 'Sales', '+1-555-1003', true, false, false, 1),
('admin@globalent.com', 'admin_global', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Alice', 'Global', '+1-555-2001', true, false, true, 2);

-- Insert pipelines
INSERT INTO pipelines (name, description, is_default, is_active, company_id) VALUES
('Sales Pipeline', 'Main sales process pipeline', true, true, 1),
('Marketing Pipeline', 'Marketing qualified leads pipeline', false, true, 1),
('Sales Pipeline Global', 'Global sales process', true, true, 2);

-- Insert pipeline stages
INSERT INTO pipeline_stages (name, description, position, probability, is_active, pipeline_id) VALUES
('Lead', 'New lead identified', 1, 10, true, 1),
('Qualified', 'Lead qualified', 2, 25, true, 1),
('Proposal', 'Proposal sent', 3, 50, true, 1),
('Negotiation', 'In negotiation', 4, 75, true, 1),
('Closed Won', 'Deal closed successfully', 5, 100, true, 1),
('Closed Lost', 'Deal lost', 6, 0, true, 1);

-- Insert sample leads
INSERT INTO leads (first_name, last_name, email, phone, company, job_title, status, source, score, notes, assigned_user_id, company_id) VALUES
('John', 'Doe', 'john.doe@example.com', '+1-555-3001', 'Example Corp', 'CTO', 'NEW', 'WEBSITE', 85, 'High priority lead from website', 3, 1),
('Jane', 'Smith', 'jane.smith@test.com', '+1-555-3002', 'Test Inc', 'CEO', 'CONTACTED', 'PHONE', 92, 'Very interested in our solution', 3, 1),
('Mike', 'Johnson', 'mike.j@demo.com', '+1-555-3003', 'Demo Ltd', 'VP Sales', 'QUALIFIED', 'REFERRAL', 78, 'Referred by existing customer', 2, 1);

-- Insert sample contacts
INSERT INTO contacts (first_name, last_name, email, phone, job_title, department, company_id, lead_id, is_active, notes, assigned_user_id) VALUES
('Sarah', 'Wilson', 'sarah.wilson@acme.com', '+1-555-4001', 'Marketing Director', 'Marketing', 1, NULL, true, 'Internal contact', 2, 1),
('Tom', 'Brown', 'tom.brown@client.com', '+1-555-4002', 'IT Director', 'IT', 1, 1, true, 'Converted from lead', 3, 1);

-- Insert sample deals
INSERT INTO deals (name, description, value, currency, status, probability, expected_close_date, contact_id, pipeline_id, stage_id, assigned_user_id, company_id) VALUES
('Enterprise Software License', 'Annual software license for 500 users', 50000.00, 'USD', 'OPEN', 75, '2024-03-31 00:00:00', 2, 1, 4, 3, 1),
('Consulting Services', '6-month implementation project', 25000.00, 'USD', 'OPEN', 60, '2024-02-28 00:00:00', 1, 1, 3, 2, 1);

-- Insert sample activities
INSERT INTO activities (type, subject, description, status, due_date, assigned_user_id, related_entity_type, related_entity_id, company_id) VALUES
('CALL', 'Follow up with John Doe', 'Call to discuss pricing and timeline', 'PENDING', '2024-01-15 14:00:00', 3, 'lead', 1, 1),
('EMAIL', 'Send proposal to Jane Smith', 'Send detailed proposal document', 'PENDING', '2024-01-16 10:00:00', 3, 'lead', 2, 1),
('MEETING', 'Demo presentation', 'Product demonstration for Mike Johnson', 'COMPLETED', '2024-01-10 15:00:00', 2, 'lead', 3, 1);

-- Insert custom fields for LEAD entity
INSERT INTO custom_fields (entity_type, field_name, field_label, field_type, is_required, default_value, options, validation_rules, display_order, is_active, company_id, created_by) VALUES
('LEAD', 'custom_industry', 'Industry', 'SELECT', false, 'Technology', '["Technology", "Healthcare", "Finance", "Manufacturing", "Retail", "Education", "Other"]', '{"required": false}', 1, true, 1, 1),
('LEAD', 'custom_company_size', 'Company Size', 'SELECT', false, 'Small', '["Startup (1-10)", "Small (11-50)", "Medium (51-200)", "Large (201-1000)", "Enterprise (1000+)"]', '{"required": false}', 2, true, 1, 1),
('LEAD', 'custom_budget_range', 'Budget Range', 'SELECT', false, 'Not Specified', '["Under $10K", "$10K - $50K", "$50K - $100K", "$100K - $500K", "$500K+", "Not Specified"]', '{"required": false}', 3, true, 1, 1),
('LEAD', 'custom_lead_source_detail', 'Lead Source Detail', 'TEXT', false, null, null, '{"maxLength": 100}', 4, true, 1, 1),
('LEAD', 'custom_notes', 'Additional Notes', 'TEXTAREA', false, null, null, '{"maxLength": 1000}', 5, true, 1, 1),
('LEAD', 'custom_priority', 'Priority Level', 'SELECT', false, 'Medium', '["Low", "Medium", "High", "Urgent"]', '{"required": false}', 6, true, 1, 1),
('LEAD', 'custom_referral_source', 'Referral Source', 'TEXT', false, null, null, '{"maxLength": 100}', 7, true, 1, 1),
('LEAD', 'custom_decision_maker', 'Decision Maker', 'TEXT', false, null, null, '{"maxLength": 100}', 8, true, 1, 1),
('LEAD', 'custom_timeline', 'Expected Timeline', 'SELECT', false, 'Not Specified', '["Immediate", "1-3 months", "3-6 months", "6-12 months", "12+ months", "Not Specified"]', '{"required": false}', 9, true, 1, 1),
('LEAD', 'custom_competitors', 'Competitors Considered', 'TEXTAREA', false, null, null, '{"maxLength": 500}', 10, true, 1, 1);

-- Insert custom fields for CONTACT entity
INSERT INTO custom_fields (entity_type, field_name, field_label, field_type, is_required, default_value, options, validation_rules, display_order, is_active, company_id, created_by) VALUES
('CONTACT', 'custom_department', 'Department', 'SELECT', false, 'Not Specified', '["Sales", "Marketing", "IT", "HR", "Finance", "Operations", "Executive", "Other", "Not Specified"]', '{"required": false}', 1, true, 1, 1),
('CONTACT', 'custom_job_level', 'Job Level', 'SELECT', false, 'Not Specified', '["C-Level", "VP", "Director", "Manager", "Senior", "Mid-Level", "Junior", "Not Specified"]', '{"required": false}', 2, true, 1, 1),
('CONTACT', 'custom_communication_preference', 'Communication Preference', 'SELECT', false, 'Email', '["Email", "Phone", "SMS", "WhatsApp", "LinkedIn", "Other"]', '{"required": false}', 3, true, 1, 1),
('CONTACT', 'custom_timezone', 'Timezone', 'SELECT', false, 'UTC', '["UTC", "EST", "PST", "CST", "MST", "GMT", "Other"]', '{"required": false}', 4, true, 1, 1),
('CONTACT', 'custom_notes', 'Contact Notes', 'TEXTAREA', false, null, null, '{"maxLength": 1000}', 5, true, 1, 1);

-- Insert custom fields for DEAL entity
INSERT INTO custom_fields (entity_type, field_name, field_label, field_type, is_required, default_value, options, validation_rules, display_order, is_active, company_id, created_by) VALUES
('DEAL', 'custom_deal_type', 'Deal Type', 'SELECT', false, 'New Business', '["New Business", "Renewal", "Upsell", "Cross-sell", "Migration", "Other"]', '{"required": false}', 1, true, 1, 1),
('DEAL', 'custom_contract_length', 'Contract Length', 'SELECT', false, 'Not Specified', '["Monthly", "Quarterly", "Annual", "2 Years", "3+ Years", "One-time", "Not Specified"]', '{"required": false}', 2, true, 1, 1),
('DEAL', 'custom_payment_terms', 'Payment Terms', 'SELECT', false, 'Net 30', '["Net 15", "Net 30", "Net 45", "Net 60", "Upfront", "Milestone-based", "Other"]', '{"required": false}', 3, true, 1, 1),
('DEAL', 'custom_competition', 'Competition', 'TEXTAREA', false, null, null, '{"maxLength": 500}', 4, true, 1, 1),
('DEAL', 'custom_risk_factors', 'Risk Factors', 'TEXTAREA', false, null, null, '{"maxLength": 500}', 5, true, 1, 1);