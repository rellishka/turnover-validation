-- Analytics views (see docs/analytics-contract.md).
-- Applied after Hibernate creates the operational tables
-- (spring.jpa.defer-datasource-initialization=true). In production these
-- would be Flyway/Liquibase migrations; here schema.sql demonstrates them on H2.

CREATE SCHEMA IF NOT EXISTS analytics;

-- v1: one row per validated turnover (one lease, one month), denormalized so
-- consumers need no operational joins. Only ACCEPTED / CORRECTED data leaves it.
CREATE OR REPLACE VIEW analytics.v1_monthly_turnover AS
SELECT
    ten.external_id AS tenant_external_id,
    ten.name        AS tenant_name,
    p.external_id   AS property_external_id,
    p.name          AS property_name,
    p.country       AS property_country,
    t.period        AS period,      -- 'YYYY-MM'
    t.amount        AS amount,
    t.currency      AS currency,
    t.status        AS status       -- ACCEPTED | CORRECTED
FROM turnover t
JOIN lease    l   ON t.lease_id    = l.id
JOIN property p   ON l.property_id  = p.id
JOIN tenant   ten ON l.tenant_id    = ten.id
WHERE t.status IN ('ACCEPTED', 'CORRECTED');
