# Apache Causeway — implementation notes

The company uses Apache Causeway, which generates a UI (and optionally a REST
API) from annotated Java domain objects. This shapes how the operational app is
built and is why the assignment says "you don't need to focus on the UI."

## What Causeway gives you automatically

One annotated domain class is read into Causeway's metamodel; from that single
source it provides:

- **UI** — the Wicket-based viewer renders domain objects as screens. Annotate a
  class, get a UI. No HTML/React written by hand. The review screen for a
  flagged turnover is generated, not coded — the mock-up just shows what it
  would look like.
- **REST API** — the Restful Objects viewer exposes the same domain objects and
  their actions as a REST API automatically. No controllers written. This is how
  the operational system can be consumed programmatically by other apps later.

## What you still wire up (persistence is not magic)

Causeway integrates with an ORM rather than inventing persistence:

- Choose **JPA (EclipseLink)** or **JDO (DataNucleus)**.
- Annotate entities for **two audiences**: the ORM (`@Entity`, `@Table`,
  `@Column`, relationships = how to persist) *and* Causeway (`@DomainObject`,
  `@Property`, `@Action` = how to render and expose).
- Configure a datasource in `application.properties`; let the ORM generate the
  schema or manage it with migrations.

## Corrections are domain actions, not POST controllers

The Asset Manager corrects a flagged turnover through a **business action method**
on the domain object — not a hand-written `POST` endpoint.

```java
public Turnover correctAmount(BigDecimal newAmount, String reason) {
    this.amount = newAmount;
    this.status = Status.CORRECTED;
    // resolve the linked ValidationIssue; record who/when/why
    return this;
}
```

From this one method Causeway:

- renders a **button** on the turnover's generated screen (the UI),
- exposes it as a **REST endpoint** (Restful Objects),
- runs it in a **managed transaction**,
- enforces **business rules** and reflects them in the UI (e.g. greys out the
  button when the rule isn't met).

Sibling action `accept()` → status `ACCEPTED` ("figure was right after all").
These two actions are the buttons on the review-screen mock-up.

Business rules live in the action, e.g.:
- only a `FLAGGED` turnover can be corrected,
- correcting requires a reason,
- corrected amount must be positive.

## Audit trail (why reports become trustworthy)

The old process produced untrusted reports. Each correction records the original
value, new value, who changed it, when, and why — mapped onto the existing
`ValidationIssue` fields (`resolution`, `resolvedBy`, `resolvedAt`). This audit
trail is what lets the Board trust the numbers, and only `ACCEPTED` / `CORRECTED`
data flows to the analytics views.

## Gradle (sketch — not a running setup)

- Causeway **BOM** for version alignment
- Starter modules: core, persistence (JPA *or* JDO), Wicket viewer, Restful
  Objects viewer
- Database driver (e.g. PostgreSQL)
