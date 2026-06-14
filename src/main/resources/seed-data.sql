-- Demo seed data. Loaded on every app run via spring.sql.init in the main
-- application.properties; the test config omits it, so tests stay unaffected.
-- Runs after Hibernate creates the tables and schema.sql creates the analytics view.
-- Illustrates the review workflow: ACCEPTED history, one FLAGGED (open issue),
-- and one CORRECTED (resolved issue). Only ACCEPTED/CORRECTED rows reach the view.

INSERT INTO property (id, external_id, name, country, city) VALUES
    (1, 'P-007', 'Kastanjelaan', 'Netherlands', 'Amsterdam'),
    (2, 'P-011', 'Rue Royale',   'Belgium',     'Brussels'),
    (3, 'P-021', 'Berliner Allee','Germany',    'Düsseldorf');

INSERT INTO tenant (id, external_id, name) VALUES
    (1, 'T-1042', 'Zalando'),
    (2, 'T-2055', 'MediaMarkt'),
    (3, 'T-3088', 'Decathlon');

INSERT INTO lease (id, property_id, tenant_id, start_date, end_date) VALUES
    (1, 1, 1, '2024-01-01', NULL),
    (2, 2, 3, '2023-06-01', NULL),
    (3, 3, 2, '2022-09-01', NULL);

INSERT INTO import_run (id, started_at, finished_at, status, period_fetched, entries_imported, error_message) VALUES
    (1, '2026-04-01 02:00:00', '2026-04-01 02:00:05', 'SUCCESS', '2026-03', 1, NULL),
    (2, '2026-05-01 02:00:00', '2026-05-01 02:00:06', 'SUCCESS', '2026-04', 3, NULL),
    (3, '2026-06-01 02:00:00', '2026-06-01 02:00:07', 'SUCCESS', '2026-05', 4, NULL);

INSERT INTO turnover (id, lease_id, import_run_id, period, amount, currency, status, submitted_at) VALUES
    -- Zalando / Kastanjelaan: steady, then a flagged spike in May (stays out of the view)
    (1, 1, 1, '2026-03', 180000.00, 'EUR', 'ACCEPTED', '2026-04-03 10:00:00'),
    (2, 1, 2, '2026-04', 180000.00, 'EUR', 'ACCEPTED', '2026-05-03 10:00:00'),
    (3, 1, 3, '2026-05', 266000.00, 'EUR', 'FLAGGED',  '2026-06-03 10:00:00'),
    -- Decathlon / Rue Royale: May figure flagged then corrected down by an Asset Manager
    (4, 2, 2, '2026-04', 100000.00, 'EUR', 'ACCEPTED',  '2026-05-03 09:15:00'),
    (5, 2, 3, '2026-05', 105000.00, 'EUR', 'CORRECTED', '2026-06-03 09:15:00'),
    -- MediaMarkt / Berliner Allee: within threshold, accepted automatically
    (6, 3, 2, '2026-04', 200000.00, 'EUR', 'ACCEPTED', '2026-05-03 11:30:00'),
    (7, 3, 3, '2026-05', 210000.00, 'EUR', 'ACCEPTED', '2026-06-03 11:30:00');

INSERT INTO validation_issue (id, turnover_id, rule, description, status, resolution, resolved_by, resolved_at) VALUES
    (1, 3, 'MONTH_OVER_MONTH_DEVIATION',
        'Turnover 266000 for 2026-05 deviates from previous month (180000) beyond the 0.30 threshold',
        'OPEN', NULL, NULL, NULL),
    (2, 5, 'MONTH_OVER_MONTH_DEVIATION',
        'Turnover 165000 for 2026-05 deviates from previous month (100000) beyond the 0.30 threshold',
        'RESOLVED', 'Tenant double-counted a refund; corrected to 105000', 'a.manager@property.example',
        '2026-06-05 14:30:00');
