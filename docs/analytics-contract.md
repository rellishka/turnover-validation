# Analytics contract

See [powerbi-dashboard-mockup.html](powerbi-dashboard-mockup.html) for an
illustrative dashboard built on these views (open in a browser).

Versioned, read-only SQL views in an `analytics` schema. Power BI is the first
consumer; designed so other apps can reuse it.

## Base view

Grain: one row per validated turnover (one lease, one month). Only `ACCEPTED` /
`CORRECTED` data. Denormalized so consumers don't need operational joins.

```sql
CREATE VIEW analytics.v1_monthly_turnover AS
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
JOIN property p   ON l.property_id = p.id
JOIN tenant   ten ON l.tenant_id   = ten.id
WHERE t.status IN ('ACCEPTED', 'CORRECTED');
```

The 12-month "by tenant" / "by property" reports: Power BI windows `period` and
sums by `tenant_name` / `property_name`. Expose the grain, let the consumer
aggregate.

## Access

- Power BI uses a **read-only role** with `SELECT` on the `analytics` schema only.
- `RECEIVED` / `FLAGGED` rows never leave the view's `WHERE` filter.

## Maintenance (when the operational model changes)

The view is an abstraction layer between the operational schema and consumers.

- **Non-breaking** (operational column renamed, entity split): absorb in the
  view definition — the output column stays the same.
- **Breaking** (output column renamed/removed/retyped, grain change): publish
  `v2_` alongside `v1_`, migrate consumers, drop `v1_`. Consumers bind to a
  version, so a refactor never breaks them.

Views are versioned as migrations; a contract test guards the output columns.

## Assumptions

- Multi-currency: view exposes `amount` + `currency`, no conversion. The app
  does not handle currency conversion; cross-currency totals are out of scope
  (handled by the consumer if needed).
- 12-month window = trailing 12 months unless the business says calendar-year.
- One turnover row per lease per period (enforced operationally) — no double-counting.
