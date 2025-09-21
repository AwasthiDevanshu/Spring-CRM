-- Simple seed data for CRM system

-- Insert companies
INSERT INTO companies (name, description, website, phone, email, address, city, state, country, postal_code, is_active) VALUES
('Acme Corporation', 'Leading technology solutions provider', 'https://acme.com', '+1-555-0101', 'info@acme.com', '123 Tech Street', 'San Francisco', 'CA', 'USA', '94105', true);

-- Insert users
INSERT INTO users (email, username, hashed_password, first_name, last_name, phone, is_active, is_superuser, is_company_admin, company_id) VALUES
('admin@company.com', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Admin', 'User', '+1-555-1001', true, true, true, 1);

-- Insert pipelines
INSERT INTO pipelines (name, description, is_default, is_active, company_id, created_by_id) VALUES
('Sales Pipeline', 'Main sales process pipeline', true, true, 1, 1);

-- Insert pipeline stages
INSERT INTO pipeline_stages (name, description, stage_order, is_active, pipeline_id, company_id) VALUES
('Lead', 'Initial lead stage', 1, true, 1, 1),
('Qualified', 'Qualified lead stage', 2, true, 1, 1),
('Proposal', 'Proposal stage', 3, true, 1, 1),
('Negotiation', 'Negotiation stage', 4, true, 1, 1),
('Closed Won', 'Closed won stage', 5, true, 1, 1),
('Closed Lost', 'Closed lost stage', 6, true, 1, 1);
