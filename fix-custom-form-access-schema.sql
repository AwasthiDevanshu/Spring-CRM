-- Fix custom_form_access table to add missing columns
ALTER TABLE custom_form_access 
ADD COLUMN company_id BIGINT NOT NULL DEFAULT 1,
ADD COLUMN created_by BIGINT NOT NULL DEFAULT 1,
ADD COLUMN last_accessed_at TIMESTAMP NULL;

-- Add foreign key constraints
ALTER TABLE custom_form_access 
ADD CONSTRAINT fk_custom_form_access_company_id 
FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE custom_form_access 
ADD CONSTRAINT fk_custom_form_access_created_by 
FOREIGN KEY (created_by) REFERENCES users(id);

-- Add indexes for the new columns
CREATE INDEX idx_custom_form_access_company_id ON custom_form_access(company_id);
CREATE INDEX idx_custom_form_access_created_by ON custom_form_access(created_by);
CREATE INDEX idx_custom_form_access_last_accessed ON custom_form_access(last_accessed_at);
