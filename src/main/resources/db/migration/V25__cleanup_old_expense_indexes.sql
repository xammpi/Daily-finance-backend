-- V25: Clean up old "expenses" indexes that remained after V18 rename
-- These indexes are duplicates of the new "transactions" indexes created in V22
-- Total savings: ~614 MB

BEGIN;

-- ============================================================================
-- REMOVE OLD EXPENSE INDEXES (from before V18 rename)
-- ============================================================================

-- Remove duplicate category index (idx_expenses_category_id is same as idx_transactions_category)
DROP INDEX IF EXISTS idx_expenses_category_id;

-- Remove duplicate user_date index (idx_expenses_user_date is same as idx_transactions_user_date)
DROP INDEX IF EXISTS idx_expenses_user_date;

-- Remove redundant single-column date index (superseded by composite indexes)
DROP INDEX IF EXISTS idx_expenses_date;

-- Remove redundant single-column user_id index (superseded by composite indexes)
DROP INDEX IF EXISTS idx_expenses_user_id;

COMMIT;
