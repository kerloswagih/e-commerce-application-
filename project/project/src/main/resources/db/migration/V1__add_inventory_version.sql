
-- Flyway migration: add version column to inventory_levels for optimistic locking
ALTER TABLE inventory_levels ADD COLUMN IF NOT EXISTS version INT DEFAULT 0;

-- set NOT NULL if you prefer (optional):
-- ALTER TABLE inventory_levels ALTER COLUMN version SET NOT NULL;
