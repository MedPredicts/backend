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
│   ├── i18n/
│   │   └── impl/
│   ├── model/         # shared application settings/data when needed
│   ├── util/          # shared error codes or other focused cross-feature types
│   └── exception/
│       ├── dto/
│       └── handler/
└── <feature>/
    ├── controller/
    ├── service/
    │   └── impl/
    ├── model/
    │   ├── dto/
    │   └── entity/       # when persistence is needed
    ├── infrastructure/   # when integrations are needed
    │   └── <integration>/
    │       └── impl/
    ├── validation/       # when custom validation is needed
    ├── repository/       # when persistence is needed
    └── mapper/           # when nontrivial mapping is needed
```

This is a placement guide, not a requirement to generate empty packages. Use actual project class names for the entry point. Create new feature packages for new business domains. Keep tests under matching package paths in `src/test/java`. Retain existing subdomain boundaries such as `admin` and `view` when they have a purpose; do not introduce them without a requirement.

## Responsibilities and dependencies

- Controllers own HTTP routing, request binding, status codes, and delegation. Keep business logic out of controllers.
- Put service interfaces in `<feature>.service` and stateless `@Service` implementations in `<feature>.service.impl`, named `<Name>ServiceImpl`. Inject interfaces into consumers.
- Reserve `<feature>.model` for data representations such as DTOs and entities. Put integration contracts and providers in `<feature>.infrastructure.<integration>` and implementations in its `impl` package. For example, inference uses `prediction.infrastructure.model` and `prediction.infrastructure.model.impl`. Keep integration details out of controllers.
- Keep standalone request/response DTOs as classes in `<feature>.model.dto`. Use Lombok `@Getter`, `@EqualsAndHashCode`, and `@AllArgsConstructor` for boilerplate where appropriate. Prefer immutable fields. Retain explicit constructors only when they perform real work such as defensive collection copying or normalization; preserve absent versus empty values. Avoid `@Data` or `@Value` when their generated setters or `toString` would expose sensitive state or violate immutability.
- Name DTOs by domain, purpose, and direction: `<Domain><Operation>Request` for inputs and `<Domain><Purpose>Response` for outputs, for example `MenuCategoryCreateRequest`, `MenuCategoryUpdateRequest`, and `MenuCategoryViewResponse`. Give nested response DTOs equally descriptive names. Avoid generic names such as `Field`, `Option`, or `Info` and ambiguous names that resemble entities. Use shared error names such as `ApiErrorResponse` where the scope is cross-feature. Do not invent CRUD operations or request wrappers just to satisfy a naming pattern; dynamic payloads can remain dynamic. Preserve JSON property names when renaming Java types.
- Use the Spring Boot-managed Lombok version, configure annotation processing for the project's compiler, and keep Lombok out of the packaged runtime. Preserve existing annotation processors when adding Lombok. Verify generated getters, constructors, equality, JSON serialization, and Bean Validation through compilation and relevant tests.
- Keep persistence entities in `<feature>.model.entity`, repositories in `<feature>.repository`, and substantial conversion logic in `<feature>.mapper`. Do not expose entities directly as API responses.
- Put shared configuration and configuration properties in `core.configuration`. Put shared error types, response DTOs, and global exception handlers under `core.exception`. Keep feature-only helpers and domain behavior in the owning feature; core must not depend on feature implementation classes.
- Use constructor injection with Lombok `@RequiredArgsConstructor` and `private final` dependencies in controllers, services, configuration classes, and other Spring components whose constructors only assign dependencies. Do not add redundant `@Autowired` to a single generated constructor or constructor annotations to components without required dependencies. Keep explicit constructors when they perform real initialization or need injection annotations such as qualifiers that are not copied by Lombok; verify injection behavior when replacing them.
- Use appropriate Spring stereotypes and `@Override` on implementations. Keep methods focused, names descriptive, imports explicit and unused imports removed. Avoid anonymous implementations for substantial providers and catch-all utility classes.

## API boundaries and error handling

- Preserve documented paths, JSON property names, optionality, status codes, units, and error schemas. Update callers, tests, and documentation together when a contract change is authorized.
- Use `spring-boot-starter-validation` and Jakarta constraints for DTO validation. Invoke validation explicitly at service/integration boundaries or use `@Valid` for bound fixed-shape requests. Keep metadata-driven checks for genuinely dynamic schemas; do not force them into hardcoded DTOs.
- Dynamic JSON APIs can use a clearly named request DTO with Jackson delegating binding and a class-level custom Bean Validation constraint. Apply `@Valid` at the controller, validate fields against the active schema, and map violations to the established error format. Preserve flat JSON when the contract requires it, reject invalid JSON shapes, and keep request data immutable. Never store per-request state in a shared constraint validator. Retain service-boundary checks for callers outside HTTP.
- Validate required values, types, supported categories, numerical constraints, and cross-field rules at the server boundary. Decide unknown-field and duplicate-key handling according to the contract. Avoid silently coercing input when the contract requires strict types.
- Validate external service outputs before exposing them as successful results. Handle non-finite numbers and inconsistent units or identifiers where applicable.
- Use specific domain/integration exceptions and typed error responses through `@RestControllerAdvice`. Map HTTP statuses deliberately, preferably using `HttpStatus`. Do not turn unrelated programming errors into misleading business errors.
- Represent application failures with a shared `ServiceValidationException` carrying an `ErrorCode`, a message key, and optional formatting arguments. Keep error state immutable and defensively copy argument arrays. Put the enum in `core.util` and define explicit status mappings appropriate to each error; do not force availability or missing-resource errors into HTTP 400. Specific exceptions may extend this base to preserve semantic types. Translate keys and arguments in the global exception handler while retaining the existing JSON contract. Keep structured field-validation errors on their dedicated response path. Add enum values only for implemented failures, and do not wrap programming exceptions as service validation failures.
- Return safe client messages without stack traces, secrets, or submitted sensitive values. Preserve framework handling for HTTP errors unless the contract requires customization.

## Configuration and integrations

- When OpenAPI is requested, use the springdoc starter compatible with the project's Spring Boot version. Place shared API metadata in `core.configuration`; keep feature-specific schema customization with that feature's infrastructure. Document operation IDs, DTO schemas, request headers, actual HTTP statuses, and all error-envelope alternatives. Show localized examples while keeping error codes stable. Describe dynamic payloads accurately without inventing a wrapper or hardcoding a demo schema. Use synthetic examples and mark demo behavior clearly. Verify the generated API document and Swagger UI resources through integration tests, including when external integrations are unavailable. Keep production documentation exposure configurable.

- For multilingual APIs, place `MessageService` and its implementation in `core.i18n` and `core.i18n.impl`. Use Spring `MessageSource`, UTF-8 bundles, an explicit supported-language list, and a configurable default locale. Resolve the request locale through Spring and translate safe message codes at the HTTP boundary; keep machine-readable error codes and field names stable. Test supported languages, missing/unsupported-language fallback, and message arguments. Keep business validation independent of translated wording.

- Respect the project's configured Java version, Spring Boot version, build tool, wrapper, and properties/YAML format. Do not upgrade or add dependencies merely to satisfy a convention.
- Bind related settings with immutable `@ConfigurationProperties` where practical; validate configuration at startup. Startup configuration errors are developer-facing failures, not localized API service exceptions. Externalize environment-specific settings and secrets.
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
