BEGIN;

INSERT INTO wallets (user_id, amount, currency_id, created_at, updated_at)
SELECT
    u.id,
    0.00,
    (SELECT id FROM currencies WHERE code = 'USD' LIMIT 1),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM wallets w WHERE w.user_id = u.id
);

COMMIT;
