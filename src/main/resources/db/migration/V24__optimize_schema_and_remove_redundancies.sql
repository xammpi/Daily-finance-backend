-- V24: Schema Optimization - Remove redundancies and add missing constraints
-- This migration cleans up redundant indexes and adds proper constraints

BEGIN;

-- ============================================================================
-- 1. REMOVE REDUNDANT INDEXES
-- ============================================================================
-- PostgreSQL automatically creates indexes for UNIQUE constraints
-- These indexes are redundant and waste space/performance

-- Drop redundant indexes on users table (UNIQUE constraints already indexed)
DROP INDEX IF EXISTS idx_users_email;
DROP INDEX IF EXISTS idx_users_username;

-- Drop redundant single-column indexes superseded by composite indexes
-- idx_categories_user_id is redundant with idx_categories_user_type
DROP INDEX IF EXISTS idx_categories_user_id;

-- idx_categories_type is redundant with idx_categories_user_type
DROP INDEX IF EXISTS idx_categories_type;

-- idx_transactions_date is redundant with idx_transactions_user_date
DROP INDEX IF EXISTS idx_transactions_date;

-- Drop redundant index on parent_id (if it still exists from V3)
DROP INDEX IF EXISTS idx_categories_parent_id;


-- ============================================================================
-- 2. ADD MISSING CHECK CONSTRAINTS
-- ============================================================================

-- Add CHECK constraint for category type (ensure only valid values)
ALTER TABLE categories
ADD CONSTRAINT chk_category_type
CHECK (type IN ('INCOME', 'EXPENSE'));

-- Add CHECK constraint for transaction amounts (must be positive)
ALTER TABLE transactions
ADD CONSTRAINT chk_transaction_amount_positive
CHECK (amount > 0);

-- Add CHECK constraint for wallet amounts (can be zero or positive)
ALTER TABLE wallets
ADD CONSTRAINT chk_wallet_amount_non_negative
CHECK (amount >= 0);


-- ============================================================================
-- 3. ADD MISSING FOREIGN KEY INDEXES
-- ============================================================================
-- Foreign keys should have indexes for JOIN performance

-- Transactions table foreign keys
-- idx_transactions_user_date already covers user_id
-- idx_transactions_category already covers category_id

-- Wallets table foreign keys
-- idx_wallets_currency already covers currency_id
-- wallet.user_id has UNIQUE constraint (automatically indexed)


-- ============================================================================
-- 4. ADD MISSING CONSTRAINTS FOR DATA INTEGRITY
-- ============================================================================

-- Ensure currency code is always uppercase (data consistency)
ALTER TABLE currencies
ADD CONSTRAINT chk_currency_code_format
CHECK (code = UPPER(code) AND LENGTH(code) = 3);

-- Ensure currency code only contains letters
ALTER TABLE currencies
ADD CONSTRAINT chk_currency_code_alpha
CHECK (code ~ '^[A-Z]{3}$');


-- ============================================================================
-- 5. OPTIMIZE EXISTING INDEXES
-- ============================================================================

-- Create partial index for enabled users only (most queries filter on enabled=true)
CREATE INDEX IF NOT EXISTS idx_users_enabled
ON users(id) WHERE enabled = TRUE;

-- Create covering index for common transaction queries (avoid table lookups)
-- This index includes all columns typically selected in transaction lists
CREATE INDEX IF NOT EXISTS idx_transactions_list_covering
ON transactions(user_id, date DESC)
INCLUDE (id, amount, category_id, description);


-- ============================================================================
-- 6. ADD COMMENTS FOR DOCUMENTATION
-- ============================================================================

COMMENT ON TABLE users IS 'Application users with authentication credentials';
COMMENT ON TABLE wallets IS 'User wallets with balance and currency (1:1 with users)';
COMMENT ON TABLE currencies IS 'Static currency reference data (21 currencies)';
COMMENT ON TABLE categories IS 'User-defined transaction categories (INCOME or EXPENSE)';
COMMENT ON TABLE transactions IS 'User transactions (income and expenses)';

COMMENT ON COLUMN transactions.amount IS 'Transaction amount (must be positive, type determined by category)';
COMMENT ON COLUMN categories.type IS 'Category type: INCOME or EXPENSE';
COMMENT ON COLUMN wallets.amount IS 'Current wallet balance (automatically updated by transactions)';

COMMIT;
