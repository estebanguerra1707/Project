ALTER TABLE venta
ADD COLUMN IF NOT EXISTS total_paid NUMERIC(19,2);

ALTER TABLE venta
ADD COLUMN IF NOT EXISTS pending_balance NUMERIC(19,2);

ALTER TABLE venta
ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20);

UPDATE venta
SET total_paid = total_amount,
    pending_balance = 0,
    payment_status = 'PAGADA'
WHERE total_paid IS NULL
   OR pending_balance IS NULL
   OR payment_status IS NULL;

ALTER TABLE venta
ALTER COLUMN total_paid SET NOT NULL;

ALTER TABLE venta
ALTER COLUMN pending_balance SET NOT NULL;

ALTER TABLE venta
ALTER COLUMN payment_status SET NOT NULL;

CREATE TABLE IF NOT EXISTS venta_pago (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    payment_method_id BIGINT NOT NULL,
    usuario_id BIGINT NULL,
    payment_date TIMESTAMP NOT NULL,
    note VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_venta_pago_venta
        FOREIGN KEY (venta_id)
        REFERENCES venta(id),

    CONSTRAINT fk_venta_pago_metodo_pago
        FOREIGN KEY (payment_method_id)
        REFERENCES metodo_pago(id),

    CONSTRAINT fk_venta_pago_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
);

CREATE INDEX IF NOT EXISTS idx_venta_payment_status
ON venta (payment_status);

CREATE INDEX IF NOT EXISTS idx_venta_pago_venta_id
ON venta_pago (venta_id);

CREATE INDEX IF NOT EXISTS idx_venta_pago_payment_date
ON venta_pago (payment_date);