CREATE TABLE password_reset_audit (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT false
);
