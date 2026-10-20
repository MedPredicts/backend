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

Response DTO names describe their role: `PredictionModelInfoResponse`, `PredictionFieldResponse`, `PredictionFieldOptionResponse`, and `PredictionResultResponse`. Shared error DTOs are `ApiErrorResponse`, `ApiValidationErrorResponse`, and `FieldValidationErrorResponse`. POST input remains dynamic JSON because its fields come from model metadata.
- `prediction/infrastructure/model/` and its `impl/` package: inference contract and profile-specific providers. `prediction/model/` is reserved for DTOs and future entities.
- `prediction/validation/`: model-specific input checks.
- `core/configuration/`: type-safe CORS properties, web configuration, and API cache policy.
- `core/exception/`, `core/exception/dto/`, `core/exception/handler/`: exceptions, error responses, and global HTTP exception mapping.
- `MedPredictApplication`: application entry point.

This follows the feature-based structure of `restaurant-service`, with service interfaces and implementations and shared infrastructure under `core`. Tests mirror the corresponding production packages. Persistence entities, repositories, and entity mappers can be introduced when there is a persistence requirement; this backend currently has none.

`SKILL.md` contains reusable Spring Boot conventions for this and other backend projects. This README records MedPredict-specific behavior. Constructor injection and immutable dependencies are used throughout. Validation remains schema-driven rather than hardcoding demo fields into Bean Validation DTOs: real models may require different fields. API DTOs are immutable classes using Lombok for getters, equality, and ordinary constructors. Collection constructors retain defensive copying; DTOs do not generate payload-bearing toString methods. spring-boot-starter-validation supplies Jakarta constraints; the service validates model metadata and prediction responses, including nested DTOs. Database transactions and authentication remain outside the current scope. `SKILL (1).md` concerns the Copilot SDK and does not apply to this service.

M1/M2/M3 selection and time-series requests are not part of this initial frontend contract. No authentication or history endpoints are added.

Contract source: [frontend API contract](https://github.com/bsanat/MedPredict/blob/main/docs/api-contract.md).

