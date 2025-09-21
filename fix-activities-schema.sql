-- Fix activities table schema to match the Activity entity
-- First, let's check what columns exist and add missing ones

-- Add missing columns to activities table
ALTER TABLE activities 
ADD COLUMN IF NOT EXISTS assigned_to BIGINT NOT NULL DEFAULT 1,
ADD COLUMN IF NOT EXISTS assigned_by BIGINT NOT NULL DEFAULT 1,
ADD COLUMN IF NOT EXISTS entity_type VARCHAR(50) NOT NULL DEFAULT 'OTHER',
ADD COLUMN IF NOT EXISTS entity_id BIGINT NOT NULL DEFAULT 1,
ADD COLUMN IF NOT EXISTS company_id BIGINT NOT NULL DEFAULT 1,
ADD COLUMN IF NOT EXISTS activity_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS due_date TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
ADD COLUMN IF NOT EXISTS priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM';

-- Add foreign key constraints
ALTER TABLE activities 
ADD CONSTRAINT fk_activities_assigned_to 
FOREIGN KEY (assigned_to) REFERENCES users(id);

ALTER TABLE activities 
ADD CONSTRAINT fk_activities_assigned_by 
FOREIGN KEY (assigned_by) REFERENCES users(id);

ALTER TABLE activities 
ADD CONSTRAINT fk_activities_company_id 
FOREIGN KEY (company_id) REFERENCES companies(id);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_activities_company_id ON activities(company_id);
CREATE INDEX IF NOT EXISTS idx_activities_assigned_to ON activities(assigned_to);
CREATE INDEX IF NOT EXISTS idx_activities_assigned_by ON activities(assigned_by);
CREATE INDEX IF NOT EXISTS idx_activities_entity_type ON activities(entity_type);
CREATE INDEX IF NOT EXISTS idx_activities_entity_id ON activities(entity_id);
CREATE INDEX IF NOT EXISTS idx_activities_activity_date ON activities(activity_date);
CREATE INDEX IF NOT EXISTS idx_activities_status ON activities(status);
CREATE INDEX IF NOT EXISTS idx_activities_priority ON activities(priority);
