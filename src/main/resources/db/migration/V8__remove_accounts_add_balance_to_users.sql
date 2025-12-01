BEGIN;

-- Add balance and currency fields to users table
ALTER TABLE users
ADD COLUMN balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN currency VARCHAR(10) NOT NULL DEFAULT 'USD';

-- Update transactions to reference users instead of accounts
-- First, migrate existing transaction data to reference users
ALTER TABLE transactions
ADD COLUMN user_id BIGINT;

-- Populate user_id from the account relationship
UPDATE transactions t
SET user_id = a.user_id
FROM accounts a
WHERE t.account_id = a.id;

-- Make user_id NOT NULL after populating
ALTER TABLE transactions
    ALTER COLUMN user_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Create index for better query performance
CREATE INDEX idx_transactions_user_id ON transactions(user_id);

-- Drop the old account_id foreign key and column
ALTER TABLE transactions
DROP CONSTRAINT IF EXISTS fk_transactions_account,
DROP COLUMN account_id;

-- Drop the accounts table entirely
DROP TABLE IF EXISTS accounts CASCADE;

COMMIT;
