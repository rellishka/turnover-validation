# External API: Tenant App (assumed contract)

The Tenant App is an external system outside our control. Its API is not
specified in the assignment, so the contract below is an explicit assumption.

## Authentication

API key issued by the Tenant App administrator, sent as a bearer token.
The key is kept in secret storage (environment variable / vault), never in
code or the database.

```
Authorization: Bearer <api-key>
```

## Endpoint

```
GET /api/v1/turnovers?period=2026-05&page=0&size=100
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `period`  | yes      | Reporting month (`YYYY-MM`). Fetching by period makes re-imports idempotent: any month can be re-fetched safely. |
| `page`, `size` | no | Pagination (defaults `0` / `100`). |

## Response

```json
{
  "page": 0,
  "totalPages": 3,
  "items": [
    {
      "tenantId": "T-1042",
      "propertyId": "P-007",
      "period": "2026-05",
      "turnover": 125000.00,
      "currency": "EUR",
      "submittedAt": "2026-06-03T14:21:00Z"
    }
  ]
}
```

`tenantId` and `propertyId` are the Tenant App's identifiers; they are matched
against `Tenant.externalId` and `Property.externalId` in our domain model.

## Error handling and durability

- Non-2xx responses and timeouts: retry with exponential backoff (3 attempts),
  then mark the `ImportRun` as `FAILED` with the error message. A manual
  re-trigger is available from the application.
- The raw response body of every fetch is stored with the `ImportRun`, so any
  mapping bug can be replayed without calling the API again.
- Re-running an import for the same period updates existing `Turnover` rows
  (matched by lease + period) instead of duplicating them.

## Assumptions

- Amounts are reported with an explicit currency; no conversion is done by the
  Tenant App.
- `submittedAt` is when the tenant entered the figure.
- Tenant App is the system of record for tenant master data. We cache only the
  tenant name (required for reports and the review UI), refreshed on import and
  never edited locally. Contact details are deliberately **not** stored: they
  are personal data and not required by any report or view. Asset Managers look
  up tenant contacts in the Tenant App when needed.
- Tenants and properties are referenced by external ID only. If the Tenant App
  also exposes `GET /tenants` and `GET /properties`, master data is synced from
  there; otherwise it is maintained manually in our application.
