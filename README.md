# MedPredict backend

Java 17+ / Spring Boot / Maven. No database, request-body logging, or prediction persistence.

## Run

Set `JAVA_HOME` to a JDK 17 or newer, then use the Maven wrapper:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\ms-17.0.17"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The API listens on port 8080 (`PORT` overrides it). With no profile, both endpoints return HTTP 503 `MODEL_UNAVAILABLE`: there is no trained model yet, and the frontend correctly keeps the form unavailable.

For explicit, synthetic integration testing:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

The demo provider returns the fixed value 134.2 regardless of inputs, with `modelVersion: demo-example` and an explicit DEMO disclaimer. This is not trained inference or a medical calculation. The field schema follows the frontend's provisional example; no unapproved medical ranges are imposed.

Frontend `.env.local`:

```dotenv
VITE_API_MODE=real
VITE_API_BASE_URL=http://localhost:8080
VITE_API_TIMEOUT_MS=15000
```

Here `real` means the frontend uses HTTP. If the server runs the demo profile, its output remains a fixed demo example.

## Contract

- `GET /api/v1/model/info`: ordered field metadata, output unit, precision, and model version.
- `POST /api/v1/predictions`: JSON object with `age`, `sex` (`male`/`female`), `bmi`, `totalCholesterol`, and `glucose` for the demo provider. Numeric strings, nulls, non-finite numbers, invalid categories, and missing required fields are rejected.
- `200`: `predictedValue`, `unit`, `modelVersion`, `disclaimer`.
- `400` field errors: `{"error":"VALIDATION_ERROR","fields":[{"field":"age","message":"Обязательное поле"}]}`.
- Malformed objects, duplicate keys, trailing JSON, or unknown keys: `400` `INVALID_REQUEST`, without echoing submitted values. Unknown field names cannot be returned in `fields` because the frontend rejects those responses.
- Unavailable or invalid inference: `503` `MODEL_UNAVAILABLE`. No automatic fallback.

API responses use `Cache-Control: no-store`. Defaults allow CORS only from `http://localhost:5173` and `http://127.0.0.1:5173`, with no credentials. Override the comma-separated allowlist via `CORS_ALLOWED_ORIGINS`. Configure HTTPS at your deployment boundary. Do not enable request/response-body tracing for medical inputs.

## Connect a trained model

Replace `prediction/infrastructure/model/impl/UnavailablePredictionModel` with a `PredictionModel` implementation. Its `info()` must describe the exact preprocessing inputs and approved constraints; its `predict()` must apply the same preprocessing as training and return the version actually used. `PredictionServiceImpl` checks output units and finite values. Keep the demo profile separate. Agree feature schema, artifact, checksum, category encoding, units, preprocessing and numerical parity examples with Ali before enabling real inference.

## Structure and conventions

- `prediction/controller/`: HTTP endpoints delegating to the service interface.
- `prediction/service/` and `prediction/service/impl/`: service contract and stateless implementation.
- `prediction/model/dto/`: standalone classes for model metadata, fields, options, and predictions.

Response DTO names describe their role: `PredictionModelInfoResponse`, `PredictionFieldResponse`, `PredictionFieldOptionResponse`, and `PredictionResultResponse`. Shared error DTOs are `ApiErrorResponse`, `ApiValidationErrorResponse`, and `FieldValidationErrorResponse`. POST input binds to `PredictionCreateRequest` while retaining the flat JSON object. `@Valid` runs `@ValidPredictionInput` against active model metadata before the controller delegates to the service. Field violations retain their original names in the 400 response; unknown keys remain INVALID_REQUEST and unavailable models remain 503. Service-boundary checks also protect non-HTTP callers.
- `prediction/infrastructure/model/` and its `impl/` package: inference contract and profile-specific providers. `prediction/model/` is reserved for DTOs and future entities.
- `prediction/validation/`: model-specific input checks.
- `core/configuration/`: type-safe CORS properties, web configuration, and API cache policy.
- `core/exception/`, `core/exception/dto/`, `core/exception/handler/`: exceptions, error responses, and global HTTP exception mapping.
- `MedPredictApplication`: application entry point.

This follows the feature-based structure of `restaurant-service`, with service interfaces and implementations and shared infrastructure under `core`. Tests mirror the corresponding production packages. Persistence entities, repositories, and entity mappers can be introduced when there is a persistence requirement; this backend currently has none.

`SKILL.md` contains reusable Spring Boot conventions for this and other backend projects. This README records MedPredict-specific behavior. Constructor injection and immutable dependencies are used throughout. Request validation uses a schema-driven Bean Validation constraint rather than hardcoded demo properties: real models may require different fields. API DTOs are immutable classes using Lombok for getters, equality, and ordinary constructors. Collection constructors retain defensive copying; DTOs do not generate payload-bearing toString methods. spring-boot-starter-validation supplies Jakarta constraints; the service validates model metadata and prediction responses, including nested DTOs. Database transactions and authentication remain outside the current scope. `SKILL (1).md` concerns the Copilot SDK and does not apply to this service.

M1/M2/M3 selection and time-series requests are not part of this initial frontend contract. No authentication or history endpoints are added.

Contract source: [frontend API contract](https://github.com/bsanat/MedPredict/blob/main/docs/api-contract.md).


## Localized errors

Send `Accept-Language: ru`, `en`, or `kk` to translate API error messages. Regional variants such as `en-US` resolve to their supported language. Missing or unsupported languages use `application.default-locale`, configured through `DEFAULT_LOCALE` (default `ru`). Machine-readable codes, field names, and JSON shapes stay unchanged.

`core.i18n.MessageService` delegates to Spring MessageSource; `core.model.AppSettings` selects the request language or default. Translations live in UTF-8 `messages*.properties`; the base bundle enables Spring Boot message-source auto-configuration. Missing keys fall back to the key. Validators produce message codes; the global exception handler translates them. Add each new error key to all supported bundles. This change localizes application-owned error responses; model metadata labels and successful prediction disclaimers remain provider-owned.

## Service errors

`core.exception.ServiceValidationException` carries a `core.util.ErrorCode`, a message key, and optional formatting arguments. `GlobalExceptionHandler` translates the message and uses the enum's HTTP status. `INVALID_REQUEST` and `VALIDATION_ERROR` map to 400; `MODEL_UNAVAILABLE` maps to 503. Specific request/model exceptions extend the shared base, so existing callers retain meaningful types. Field-validation exceptions also extend the shared base with VALIDATION_ERROR and a localizable message key, while retaining their dedicated list response. Formatting arguments are copied defensively; never pass sensitive input as message arguments.

Unexpected failures return a localized SYSTEM_ERROR with HTTP 500, without exception details. Spring MVC failures keep their HTTP status and headers with the localized error envelope. Provider failures wrapped by Bean Validation retain their application error code and translation. Model metadata is validated for unique safe keys, units, finite bounds, and valid options before request validation or inference. Startup configuration failures remain developer-facing rather than API service exceptions.

## Swagger / OpenAPI

With the server running, open [Swagger UI](http://localhost:8080/swagger-ui/index.html) or [OpenAPI JSON](http://localhost:8080/v3/api-docs). The UI is also reachable through `/swagger-ui.html`. Documentation works with the default unavailable provider; prediction requests still return 503 until a real model is connected or the demo profile is explicitly enabled.

The API document includes model metadata and response DTOs, a flat dynamic `PredictionCreateRequest`, the `Accept-Language` header, localized validation examples, and 400/415/503/500 error responses. Demo inputs and outputs are labeled as fixed examples. Fetch model metadata for actual field requirements. Set `API_DOCS_ENABLED=false` and `SWAGGER_UI_ENABLED=false` to disable the specification and UI respectively.

Uses the same springdoc Web MVC UI starter version as restaurant-service (2.8.13), compatible with Spring Boot 3.5 per the [springdoc compatibility matrix](https://springdoc.org/v2/#what-is-the-compatibility-matrix-of-springdoc-openapi-with-spring-boot).
