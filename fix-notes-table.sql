-- Fix notes table structure to match Note entity
-- This script will:
-- 1. Add entity_type and entity_id columns
-- 2. Rename user_id to created_by
-- 3. Remove old lead_id, contact_id, deal_id columns

USE crm;

-- First, let's backup any existing data
CREATE TABLE IF NOT EXISTS notes_backup AS SELECT * FROM notes;

-- Add new columns
ALTER TABLE notes ADD COLUMN entity_type VARCHAR(50) AFTER content;
ALTER TABLE notes ADD COLUMN entity_id BIGINT AFTER entity_type;

-- Rename user_id to created_by
ALTER TABLE notes CHANGE COLUMN user_id created_by BIGINT NOT NULL;

-- Remove old columns
ALTER TABLE notes DROP COLUMN lead_id;
ALTER TABLE notes DROP COLUMN contact_id;
ALTER TABLE notes DROP COLUMN deal_id;
ALTER TABLE notes DROP COLUMN title;
ALTER TABLE notes DROP COLUMN is_private;

-- Add foreign key constraint for created_by
ALTER TABLE notes ADD CONSTRAINT fk_notes_created_by FOREIGN KEY (created_by) REFERENCES users(id);

-- Make entity_type and entity_id NOT NULL
ALTER TABLE notes MODIFY COLUMN entity_type VARCHAR(50) NOT NULL;
ALTER TABLE notes MODIFY COLUMN entity_id BIGINT NOT NULL;

-- Add index for better performance
CREATE INDEX idx_notes_entity ON notes(entity_type, entity_id);
CREATE INDEX idx_notes_company ON notes(company_id);
