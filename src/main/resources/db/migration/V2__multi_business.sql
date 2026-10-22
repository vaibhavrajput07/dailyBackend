-- Multi-business: one user may now own several businesses. Existing
-- businesses, transactions and categories are untouched — every legacy
-- transaction already points at its original business via business_id.

-- archived hides a business from the selector and the consolidated totals
-- without deleting any of its history. client_id is the offline-created
-- record's local id, so a retried create returns the same row instead of
-- inserting a duplicate.
ALTER TABLE businesses
    ADD COLUMN archived  BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN client_id VARCHAR(36) NULL,
    ADD CONSTRAINT uq_businesses_user_client UNIQUE (user_id, client_id);

-- The composite index above (user_id first) now backs fk_businesses_user,
-- so the one-business-per-user constraint can be dropped safely.
ALTER TABLE businesses DROP INDEX uq_businesses_user_id;
