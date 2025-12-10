-- BASELINE MIGRATION (Optional - For New Environments Only)
-- ============================================================================
-- This is a consolidated baseline migration that creates the entire schema
-- from scratch without the historical baggage of V1-V24.
--
-- USAGE:
-- For new environments, you can use this as a Flyway baseline by running:
--   ./mvnw flyway:baseline -Dflyway.baselineVersion=24
--   ./mvnw flyway:migrate
--
-- This will skip migrations V1-V24 and only run V25+
--
-- WARNING: Do NOT use this on existing databases! It will cause conflicts.
-- ============================================================================

BEGIN;

-- ============================================================================
-- 1. USERS TABLE
-- ============================================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Partial index for enabled users (most queries filter enabled=true)
CREATE INDEX idx_users_enabled ON users(id) WHERE enabled = TRUE;

COMMENT ON TABLE users IS 'Application users with authentication credentials';


-- ============================================================================
-- 2. CURRENCIES TABLE
-- ============================================================================
CREATE TABLE currencies (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(5) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_currency_code_format CHECK (code = UPPER(code) AND LENGTH(code) = 3),
    CONSTRAINT chk_currency_code_alpha CHECK (code ~ '^[A-Z]{3}$')
);

-- Insert all supported currencies
INSERT INTO currencies (code, name, symbol) VALUES
('USD', 'US Dollar', '$'),
('EUR', 'Euro', '€'),
('GBP', 'British Pound', '£'),
('JPY', 'Japanese Yen', '¥'),
('CNY', 'Chinese Yuan', '¥'),
('RUB', 'Russian Ruble', '₽'),
('UAH', 'Ukrainian Hryvnia', '₴'),
('PLN', 'Polish Zloty', 'zł'),
('CHF', 'Swiss Franc', 'CHF'),
('CAD', 'Canadian Dollar', 'C$'),
('AUD', 'Australian Dollar', 'A$'),
('BRL', 'Brazilian Real', 'R$'),
('INR', 'Indian Rupee', '₹'),
('KRW', 'South Korean Won', '₩'),
('MXN', 'Mexican Peso', '$'),
('SEK', 'Swedish Krona', 'kr'),
('NOK', 'Norwegian Krone', 'kr'),
('DKK', 'Danish Krone', 'kr'),
('TRY', 'Turkish Lira', '₺'),
('ZAR', 'South African Rand', 'R'),
('MDL', 'Moldovan Leu', 'L');

COMMENT ON TABLE currencies IS 'Static currency reference data (21 currencies)';


-- ============================================================================
-- 3. WALLETS TABLE
-- ============================================================================
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    currency_id BIGINT NOT NULL REFERENCES currencies(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_wallet_amount_non_negative CHECK (amount >= 0)
);

-- Index on foreign keys
CREATE INDEX idx_wallets_currency ON wallets(currency_id);

COMMENT ON TABLE wallets IS 'User wallets with balance and currency (1:1 with users)';
COMMENT ON COLUMN wallets.amount IS 'Current wallet balance (automatically updated by transactions)';


-- ============================================================================
-- 4. CATEGORIES TABLE
-- ============================================================================
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    type VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_category_type CHECK (type IN ('INCOME', 'EXPENSE'))
);

-- Composite indexes for query optimization
CREATE INDEX idx_categories_user_type ON categories(user_id, type);
CREATE INDEX idx_categories_name ON categories(name);
CREATE INDEX idx_categories_user_name_lower ON categories(user_id, LOWER(name));
CREATE INDEX idx_categories_name_lower ON categories(LOWER(name));

COMMENT ON TABLE categories IS 'User-defined transaction categories (INCOME or EXPENSE)';
COMMENT ON COLUMN categories.type IS 'Category type: INCOME or EXPENSE';


-- ============================================================================
-- 5. TRANSACTIONS TABLE
-- ============================================================================
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(19, 2) NOT NULL,
    date DATE NOT NULL,
    description VARCHAR(255),
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_transaction_amount_positive CHECK (amount > 0)
);

-- Performance indexes for common queries
CREATE INDEX idx_transactions_user_date ON transactions(user_id, date DESC);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_transactions_user_date_category ON transactions(user_id, date DESC, category_id);

-- Covering index for transaction list queries (includes all commonly selected columns)
CREATE INDEX idx_transactions_list_covering
ON transactions(user_id, date DESC)
INCLUDE (id, amount, category_id, description);

COMMENT ON TABLE transactions IS 'User transactions (income and expenses)';
COMMENT ON COLUMN transactions.amount IS 'Transaction amount (must be positive, type determined by category)';


-- ============================================================================
-- 6. DEPOSITS TABLE (Currently unused but kept for future use)
-- ============================================================================
CREATE TABLE deposits (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(19, 2) NOT NULL,
    date DATE NOT NULL,
    description VARCHAR(255),
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_deposits_user_date ON deposits(user_id, date DESC);

COMMENT ON TABLE deposits IS 'Deposit records (currently unused, reserved for future features)';


-- ============================================================================
-- 7. GRANT PERMISSIONS (Adjust as needed for your environment)
-- ============================================================================
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO your_app_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO your_app_user;


COMMIT;

-- ============================================================================
-- SCHEMA VERSION: BASELINE (Equivalent to V24)
-- ============================================================================
