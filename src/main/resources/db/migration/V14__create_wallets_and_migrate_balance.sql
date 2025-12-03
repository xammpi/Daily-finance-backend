BEGIN;

-- Create wallets table
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    user_id BIGINT NOT NULL UNIQUE,
    currency_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_wallets_currency FOREIGN KEY (currency_id) REFERENCES currencies(id)
);

-- Create indexes for better query performance
CREATE INDEX idx_wallets_user_id ON wallets(user_id);
CREATE INDEX idx_wallets_currency_id ON wallets(currency_id);

-- Migrate existing user balances to wallets
-- Only if users table has balance and currency columns
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'users'
        AND column_name = 'balance'
    ) THEN
        -- Insert wallet for each existing user
        INSERT INTO wallets (user_id, amount, currency_id, created_at, updated_at)
        SELECT
            u.id,
            COALESCE(u.balance, 0.00),
            c.id,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        FROM users u
        LEFT JOIN currencies c ON c.code = COALESCE(u.currency, 'USD');

        -- Drop balance and currency columns from users table
        ALTER TABLE users DROP COLUMN IF EXISTS balance;
        ALTER TABLE users DROP COLUMN IF EXISTS currency;
    END IF;
END $$;

COMMIT;
