DO $$
BEGIN
    IF current_database() <> 'product_management_test' THEN
        RAISE EXCEPTION 'Refusing PostgreSQL test schema setup outside product_management_test';
    END IF;
END
$$;@@

CREATE SEQUENCE IF NOT EXISTS product_sequence
    START WITH 1
    INCREMENT BY 50;@@

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_sequences
        WHERE schemaname = current_schema()
          AND sequencename = 'product_sequence'
          AND increment_by = 50
    ) THEN
        RAISE EXCEPTION 'product_sequence must increment by 50';
    END IF;
END
$$;@@

CREATE TABLE IF NOT EXISTS products (
    active BOOLEAN NOT NULL,
    price NUMERIC(38,2) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    id BIGINT NOT NULL PRIMARY KEY,
    brand VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    name VARCHAR(255) NOT NULL
);@@
