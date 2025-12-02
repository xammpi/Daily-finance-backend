BEGIN;

CREATE TABLE expenses (
                              id BIGSERIAL PRIMARY KEY,
                              amount NUMERIC(15, 2) NOT NULL CHECK (amount > 0),
                              date DATE NOT NULL,
                              description VARCHAR(255),
                              category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
                              user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMIT;