-- Add indexes for better query performance

-- Transactions table indexes
CREATE INDEX IF NOT EXISTS idx_transactions_user_date
    ON transactions(user_id, date DESC);

CREATE INDEX IF NOT EXISTS idx_transactions_category
    ON transactions(category_id);

CREATE INDEX IF NOT EXISTS idx_transactions_date
    ON transactions(date DESC);

-- Categories table indexes
CREATE INDEX IF NOT EXISTS idx_categories_user_type
    ON categories(user_id, type);

CREATE INDEX IF NOT EXISTS idx_categories_name
    ON categories(name);

-- Wallets table indexes
CREATE INDEX IF NOT EXISTS idx_wallets_currency
    ON wallets(currency_id);

-- Users table indexes (already have unique constraints, but add for faster lookups)
CREATE INDEX IF NOT EXISTS idx_users_email
    ON users(email);

CREATE INDEX IF NOT EXISTS idx_users_username
    ON users(username);

-- Composite index for common transaction queries
CREATE INDEX IF NOT EXISTS idx_transactions_user_date_category
    ON transactions(user_id, date DESC, category_id);
