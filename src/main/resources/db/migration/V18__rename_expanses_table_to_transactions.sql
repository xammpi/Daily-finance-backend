BEGIN;

ALTER TABLE expenses RENAME TO transactions;

COMMIT;