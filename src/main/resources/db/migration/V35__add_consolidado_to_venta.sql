ALTER TABLE venta
ADD COLUMN IF NOT EXISTS consolidated BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE venta
ADD COLUMN IF NOT EXISTS weekly_ticket_id BIGINT NULL;

ALTER TABLE venta
ADD COLUMN IF NOT EXISTS consolidated_at TIMESTAMP NULL;

CREATE INDEX IF NOT EXISTS idx_venta_consolidated
ON venta (consolidated);

CREATE INDEX IF NOT EXISTS idx_venta_weekly_ticket_id
ON venta (weekly_ticket_id);