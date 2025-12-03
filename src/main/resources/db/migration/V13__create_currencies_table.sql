BEGIN;

-- Create currencies table
CREATE TABLE currencies (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(5) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on code for faster lookups
CREATE INDEX idx_currencies_code ON currencies(code);

-- Insert popular currencies
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
    ('MXN', 'Mexican Peso', 'MX$'),
    ('SEK', 'Swedish Krona', 'kr'),
    ('NOK', 'Norwegian Krone', 'kr'),
    ('DKK', 'Danish Krone', 'kr'),
    ('TRY', 'Turkish Lira', '₺'),
    ('ZAR', 'South African Rand', 'R');

COMMIT;
