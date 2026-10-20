---
name: java-springboot
description: Build, review, and refactor Java Spring Boot backends using feature-based packages, service interfaces and implementations, standalone DTOs, and shared core infrastructure. Use for backend API, service, validation, configuration, persistence, and testing work.
---

# Java Spring Boot Backend Conventions

Read the project's build file, README, local instructions, and affected code before editing. Apply these conventions within the requested scope. Preserve established API contracts and domain behavior unless changes are requested. Keep project-specific endpoints, profiles, business rules, and deployment instructions in project documentation rather than this reusable skill.

## Package structure

Use the project's existing base package and keep its application entry point above component packages. Organize by business feature, with shared infrastructure under `core`:

```text
<base-package>/
├── Application.java
├── core/
│   ├── configuration/
│   └── exception/
│       ├── dto/
│       └── handler/
└── <feature>/
    ├── controller/
    ├── service/
    │   └── impl/
    ├── model/
    │   ├── dto/
    │   ├── entity/       # when persistence is needed
    │   └── impl/         # when the feature has provider implementations
    ├── validation/       # when custom validation is needed
    ├── repository/       # when persistence is needed
    └── mapper/           # when nontrivial mapping is needed
```

This is a placement guide, not a requirement to generate empty packages. Use actual project class names for the entry point. Create new feature packages for new business domains. Keep tests under matching package paths in `src/test/java`. Retain existing subdomain boundaries such as `admin` and `view` when they have a purpose; do not introduce them without a requirement.

## Responsibilities and dependencies

- Controllers own HTTP routing, request binding, status codes, and delegation. Keep business logic out of controllers.
- Put service interfaces in `<feature>.service` and stateless `@Service` implementations in `<feature>.service.impl`, named `<Name>ServiceImpl`. Inject interfaces into consumers.
- Put business-specific provider contracts in the owning feature and implementations in an appropriate `impl` package. Keep integration details out of controllers.
- Keep standalone request/response DTOs in `<feature>.model.dto`. Prefer immutable records where the project's Java version and serialization requirements permit them. Defensively copy collection-valued state. Preserve intentional distinctions between absent and empty values.
- Keep persistence entities in `<feature>.model.entity`, repositories in `<feature>.repository`, and substantial conversion logic in `<feature>.mapper`. Do not expose entities directly as API responses.
- Put shared configuration and configuration properties in `core.configuration`. Put shared error types, response DTOs, and global exception handlers under `core.exception`. Keep feature-only helpers and domain behavior in the owning feature; core must not depend on feature implementation classes.
- Use constructor injection, `private final` dependencies, appropriate Spring stereotypes, and `@Override` on implementations. Keep methods focused, names descriptive, imports explicit and unused imports removed. Avoid anonymous implementations for substantial providers and catch-all utility classes.

## API boundaries and error handling

- Preserve documented paths, JSON property names, optionality, status codes, units, and error schemas. Update callers, tests, and documentation together when a contract change is authorized.
- For fixed-shape requests, use DTOs and Jakarta Bean Validation compatible with the project's Spring version. Use metadata-driven validation for genuinely dynamic schemas; do not force them into hardcoded DTOs.
- Validate required values, types, supported categories, numerical constraints, and cross-field rules at the server boundary. Decide unknown-field and duplicate-key handling according to the contract. Avoid silently coercing input when the contract requires strict types.
- Validate external service outputs before exposing them as successful results. Handle non-finite numbers and inconsistent units or identifiers where applicable.
- Use specific domain/integration exceptions and typed error responses through `@RestControllerAdvice`. Map HTTP statuses deliberately, preferably using `HttpStatus`. Do not turn unrelated programming errors into misleading business errors.
- Return safe client messages without stack traces, secrets, or submitted sensitive values. Preserve framework handling for HTTP errors unless the contract requires customization.

## Configuration and integrations

- Respect the project's configured Java version, Spring Boot version, build tool, wrapper, and properties/YAML format. Do not upgrade or add dependencies merely to satisfy a convention.
- Bind related settings with immutable `@ConfigurationProperties`; validate configuration at startup. Externalize environment-specific settings and secrets.
- Keep profile/provider selection explicit. Never silently replace a failed production integration with a demo or fixed response. Label test/demo behavior accurately.
- Put outbound integration code behind an appropriate interface, configure connection/read timeouts, and define safe failure behavior. Add retries only when justified by operation semantics and idempotency.
- Preserve integration versioning, data transformations, and reference-output checks where those are relevant to correctness.

## Persistence and security when applicable

- Introduce databases, repositories, transactions, authentication, and messaging only when required by the task or existing architecture.
- Put transactions around the service operations that need atomic persistence, use parameterized queries, and account for database constraints, pagination, and query behavior.
- For authentication work, use the project's security mechanism, enforce authorization server-side, and store passwords only with appropriate password hashing. Do not add authentication to unrelated public API work.
- Configure CORS as an explicit origin allowlist appropriate to the application. Enable credentials only when needed. Choose cache policies based on response sensitivity and API requirements; do not impose no-store universally.
- Keep sensitive data out of URLs, logs, exception messages, and analytics. Use SLF4J parameterized logging for necessary operational events, without payload dumping.

## Verification and completion

- Use the checked-in Maven or Gradle wrapper with a compatible JDK. Read the build configuration to find required checks. Typical commands are `./mvnw test`, `.\mvnw.cmd test`, `./gradlew test`, or `.\gradlew.bat test`.
- After package moves, use a clean build to detect stale classes and broken imports. Preserve existing contract tests and mirror moved production packages in tests.
- Add meaningful tests for changed behavior: service decisions, validation boundaries, error mappings, configuration invariants, and integration failures. Use controller slices or full application tests according to the boundary under test. Use Testcontainers when real infrastructure behavior needs verification.
- Follow existing formatting/static-analysis tools when present; do not add a new toolchain for a small cleanup. Do not broaden testing without a concrete concern.
- Update affected documentation and setup references. Summarize actual changes, checks executed, and remaining limitations. Do not claim integration or deployment success from mocked tests alone.
