CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_name      VARCHAR(100) NOT NULL,
    mobile_number   VARCHAR(15)  NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_mobile_number UNIQUE (mobile_number)
);

CREATE TABLE businesses (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    business_name   VARCHAR(150) NOT NULL,
    business_type   VARCHAR(30)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_businesses_user_id UNIQUE (user_id),
    CONSTRAINT fk_businesses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE expense_categories (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id     BIGINT       NOT NULL,
    name            VARCHAR(100) NOT NULL,
    is_default      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_category_business_name UNIQUE (business_id, name),
    CONSTRAINT fk_category_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE
);

CREATE TABLE transactions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id         BIGINT        NOT NULL,
    type                VARCHAR(10)   NOT NULL,
    amount              DECIMAL(12,2) NOT NULL,
    category_id         BIGINT        NULL,
    note                VARCHAR(255)  NULL,
    payment_method      VARCHAR(20)   NULL,
    transaction_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_transactions_type CHECK (type IN ('SALE', 'EXPENSE')),
    CONSTRAINT fk_transactions_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES expense_categories(id) ON DELETE SET NULL
);

CREATE INDEX idx_transactions_business_date ON transactions (business_id, transaction_date);
CREATE INDEX idx_transactions_business_type_date ON transactions (business_id, type, transaction_date);
