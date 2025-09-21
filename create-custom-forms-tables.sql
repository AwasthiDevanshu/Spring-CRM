-- Create custom forms tables
CREATE TABLE IF NOT EXISTS custom_forms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'DRAFT', -- DRAFT, ACTIVE, INACTIVE, EXPIRED
    access_type VARCHAR(50) DEFAULT 'PUBLIC', -- PUBLIC, LEAD_SPECIFIC, CONTACT_SPECIFIC
    lead_id BIGINT NULL,
    contact_id BIGINT NULL,
    submission_expiry_days INT DEFAULT 30,
    max_submissions INT NULL,
    redirect_url VARCHAR(500),
    success_message TEXT,
    theme TEXT, -- JSON string for custom styling
    settings TEXT, -- JSON string for form settings
    company_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS custom_form_fields (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    form_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    label VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- TEXT, EMAIL, PHONE, NUMBER, TEXTAREA, SELECT, RADIO, CHECKBOX, DATE, FILE, URL, IMAGE
    is_required TINYINT(1) DEFAULT 0,
    placeholder VARCHAR(255),
    help_text TEXT,
    options TEXT, -- JSON string for select/radio/checkbox options
    validation TEXT, -- JSON string for validation rules
    `order` INT DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (form_id) REFERENCES custom_forms(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS custom_form_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    form_id BIGINT NOT NULL,
    lead_id BIGINT NULL,
    contact_id BIGINT NULL,
    access_token VARCHAR(255) NULL,
    submitted_by VARCHAR(255), -- Name of submitter
    submitted_by_email VARCHAR(255),
    submitted_by_phone VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent TEXT,
    referrer VARCHAR(500),
    submission_data TEXT NOT NULL, -- JSON string of form data
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, PROCESSED, REJECTED
    processed_at TIMESTAMP NULL,
    processed_by BIGINT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (form_id) REFERENCES custom_forms(id) ON DELETE CASCADE,
    FOREIGN KEY (processed_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS custom_form_access (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    form_id BIGINT NOT NULL,
    lead_id BIGINT NULL,
    contact_id BIGINT NULL,
    access_token VARCHAR(255) NOT NULL UNIQUE,
    is_active TINYINT(1) DEFAULT 1,
    submission_count INT DEFAULT 0,
    max_submissions INT NULL,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (form_id) REFERENCES custom_forms(id) ON DELETE CASCADE
);

-- Indexes for custom forms
CREATE INDEX idx_custom_forms_company_id ON custom_forms(company_id);
CREATE INDEX idx_custom_forms_created_by ON custom_forms(created_by);
CREATE INDEX idx_custom_forms_status ON custom_forms(status);
CREATE INDEX idx_custom_forms_access_type ON custom_forms(access_type);
CREATE INDEX idx_custom_forms_lead_id ON custom_forms(lead_id);
CREATE INDEX idx_custom_forms_contact_id ON custom_forms(contact_id);

CREATE INDEX idx_custom_form_fields_form_id ON custom_form_fields(form_id);
CREATE INDEX idx_custom_form_fields_type ON custom_form_fields(type);
CREATE INDEX idx_custom_form_fields_order ON custom_form_fields(`order`);

CREATE INDEX idx_custom_form_submissions_form_id ON custom_form_submissions(form_id);
CREATE INDEX idx_custom_form_submissions_lead_id ON custom_form_submissions(lead_id);
CREATE INDEX idx_custom_form_submissions_contact_id ON custom_form_submissions(contact_id);
CREATE INDEX idx_custom_form_submissions_status ON custom_form_submissions(status);
CREATE INDEX idx_custom_form_submissions_created_at ON custom_form_submissions(created_at);

CREATE INDEX idx_custom_form_access_form_id ON custom_form_access(form_id);
CREATE INDEX idx_custom_form_access_lead_id ON custom_form_access(lead_id);
CREATE INDEX idx_custom_form_access_contact_id ON custom_form_access(contact_id);
CREATE INDEX idx_custom_form_access_token ON custom_form_access(access_token);
CREATE INDEX idx_custom_form_access_active ON custom_form_access(is_active);
CREATE INDEX idx_custom_form_access_expires ON custom_form_access(expires_at);
