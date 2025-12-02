BEGIN;

-- Drop the transactions table since we're now using expenses
DROP TABLE IF EXISTS transactions CASCADE;

-- Add indexes to expenses table for better query performance
CREATE INDEX idx_expenses_user_id ON expenses(user_id);
CREATE INDEX idx_expenses_category_id ON expenses(category_id);
CREATE INDEX idx_expenses_date ON expenses(date);
CREATE INDEX idx_expenses_user_date ON expenses(user_id, date);

COMMIT;
