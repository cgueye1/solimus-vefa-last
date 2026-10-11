-- Manual migration for reservation history and optimistic locking.
-- This project has no Flyway/Liquibase. Execute after reviewing the live schema.

ALTER TABLE reservations
    ADD COLUMN rejected_at DATETIME NULL,
    ADD COLUMN cancelled_at DATETIME NULL,
    ADD COLUMN rejection_reason VARCHAR(500) NULL;

ALTER TABLE real_estate_properties
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- ReservationStatus is persisted as VARCHAR through EnumType.STRING.
-- Existing PROSPECT and RESERVED values remain valid; no data rewrite is required.
