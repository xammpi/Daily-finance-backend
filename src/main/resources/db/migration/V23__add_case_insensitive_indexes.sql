-- Add functional indexes for case-insensitive category searches
-- PostgreSQL can use these indexes for LOWER(column) queries

-- Index for case-insensitive category name searches
CREATE INDEX IF NOT EXISTS idx_categories_name_lower
    ON categories(LOWER(name));

-- Composite index for case-insensitive user + name searches
-- Useful for existsByUserIdAndNameIgnoreCase queries
CREATE INDEX IF NOT EXISTS idx_categories_user_name_lower
    ON categories(user_id, LOWER(name));
