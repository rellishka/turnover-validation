# CLAUDE.md

## Purpose
This repository is a Java/Spring Boot application that supports the tenant turnover
validation business process, built with ports and adapters (hexagonal architecture).

The application:
- ingests monthly turnover data from an external Tenant App (REST)
- supports Asset Managers in validating flagged turnover figures
- exposes validated data to analytics consumers (e.g. Power BI) via SQL views

The main architectural goals are:
- keep business validation in the domain or domain-oriented application flow
- keep HTTP concerns in the inbound adapter
- keep persistence concerns in the outbound adapter

Prefer clarity over framework cleverness.

## Architecture

### Package responsibilities
- `com.turnover.validation.adapter.in`: controllers, HTTP DTOs, inbound mappers
- `com.turnover.validation.adapter.in.exceptionhandler`: HTTP exception translation and error responses
- `com.turnover.validation.adapter.out`: persistence entities, Spring Data repositories, persistence mappers, outbound adapters
- `com.turnover.validation.application.port.in`: inbound use-case interfaces
- `com.turnover.validation.application.port.out`: outbound port interfaces
- `com.turnover.validation.application.domain`: domain models, validation, domain services/managers
- `com.turnover.validation.application.service`: application/use-case orchestration
- `com.turnover.validation.exception`: shared base exceptions

## Rules

### Domain
- Do not put Spring MVC or JPA annotations in the domain.
- Do not use controller DTOs in the domain or application service layer.
- Keep validation logic in domain code or domain-oriented application flow, not in controllers or persistence adapters.

### Inbound adapter
- Controllers should only handle HTTP concerns.
- Request DTOs stay in `adapter.in`.
- Response DTOs stay in `adapter.in`.
- Exception handlers for HTTP responses stay in `adapter.in.exceptionhandler`.

### Outbound adapter
- JPA entities must stay out of the domain.
- Spring Data repositories must stay in `adapter.out`.
- Outbound adapters translate between domain objects and persistence entities.

### Application service
- Application services orchestrate use cases.
- Prefer delegating domain-specific coordination to manager classes rather than reaching directly from service classes to persistence ports.
- They may throw specific application exceptions such as `InvalidTurnoverException`.
- They must depend on ports, not on Spring Data repositories directly.

## Style
- Prefer explicit, simple Java over unnecessary abstractions.
- Use Lombok only when it removes boilerplate without hiding important domain semantics.
- For small domain classes, avoid annotations that hide important semantics.
- Keep names concrete and easy to explain.

## Tests
- When writing tests, use an existing test helper class if one already exists for that area.
- If repeated test values or object creation patterns appear and no helper exists yet, create a dedicated test helper class and use it.
- When a test repeats the same literal value in setup and assertions, extract it into a local variable.
- When changing a class or adding tests, prefer running only the affected test class or targeted test set instead of the full test suite.
- When test helper methods are reused in a test class, prefer static imports for those helper methods and constants to keep the test readable.
- When a test already has a fixture object, prefer reading values from that object in assertions instead of importing separate constants for the same data.
- For request/response payloads in web tests, prefer serializing DTOs with `ObjectMapper` or another mapper instead of hardcoding JSON strings.
- Prefer importing classes directly in tests instead of using fully qualified class names inline.
