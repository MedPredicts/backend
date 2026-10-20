# MedPredict backend

Java 17+ / Spring Boot / Maven. No database, request-body logging, or prediction persistence.

## Run

Set `JAVA_HOME` to a JDK 17 or newer, then use the Maven wrapper:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\ms-17.0.17"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The API listens on port 8080 (`PORT` overrides it). With no profile, prediction and metadata endpoints return HTTP 503 `MODEL_UNAVAILABLE`. A trained artifact is enabled explicitly with the `onnx` profile; demo is a separate profile. Selecting both profiles is a configuration error.

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
- Both successful endpoints also provide optional `predictionType: estimated_current_measurement` and `demo`. Trained inference returns `demo: false`; the explicit demo provider returns `demo: true`.
- `400` field errors: `{"error":"VALIDATION_ERROR","fields":[{"field":"age","message":"Обязательное поле"}]}`.
- Malformed objects, duplicate keys, trailing JSON, or unknown keys: `400` `INVALID_REQUEST`, without echoing submitted values. Unknown field names cannot be returned in `fields` because the frontend rejects those responses.
- Unavailable or invalid inference: `503` `MODEL_UNAVAILABLE`. No automatic fallback.

API responses use `Cache-Control: no-store`. Defaults allow CORS only from `http://localhost:5173` and `http://127.0.0.1:5173`, with no credentials. Override the comma-separated allowlist via `CORS_ALLOWED_ORIGINS`. Configure HTTPS at your deployment boundary. Do not enable request/response-body tracing for medical inputs.

## Run the trained ONNX model

Use the model bundle produced by the frontend repository's reproducible `ml/` workspace. The directory must contain `model.onnx` and `metadata.json` from the same export:

```sh
export MEDPREDICT_MODEL_DIRECTORY=/absolute/path/to/frontend/ml/artifacts
export MEDPREDICT_MODEL_VERSION=MedPredict-SBP-mlp-v2 # optional exact version pin; use the exported version
sh mvnw spring-boot:run -Dspring-boot.run.profiles=onnx
```

On Windows, use the same environment variables and `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=onnx"`. A JDK 17+ is required. ONNX Runtime Java CPU 1.24.3 is pinned in Maven; its native CPU library was tested on macOS arm64 with JDK 21. No external inference API or Python server is used.

`OnnxPredictionModel` loads one immutable session at startup. It checks metadata schema version 1, the optional pinned model version, SHA-256 of the exact loaded bytes, feature names/order, category encoding, units, finite domain bounds, and ONNX tensor names/types/shapes. Input is `features: float32[batch,5]` in the order `age`, `sex`, `bmi`, `totalCholesterol`, `glucose`; categories are `male=0`, `female=1`. Output is `predictedValue: float32[batch,1]` in mmHg. Scaling and output inverse transformation are part of ONNX, so Java performs no extra normalization or unit conversion. Cholesterol and fasting plasma glucose inputs are mmol/L.

Required metadata keys are `schemaVersion`, `modelVersion`, `sha256`, `inputName`, `outputName`, `featureOrder`, `sexEncoding`, `indicatorName`, `unit`, `decimalPlaces`, `fields`, `predictionType`, `demo`, and `disclaimer`. Additional experiment provenance may be included. Numeric form bounds come from the training artifact's supported domain, and are not clinical reference intervals. The frontend obtains them through `GET /api/v1/model/info`.

Missing, corrupt, incompatible or incorrectly hashed artifacts leave the model unavailable and return 503. Inference failures return 503 without fallback values. Model inputs are not stored or logged. Replace a bundle atomically and restart the process to load it.

- `GET /api/v1/health/live`: HTTP 200 with `{"status":"UP"}` when HTTP service is running.
- `GET /api/v1/health/ready` and `GET /api/v1/model/status`: HTTP 200 for `READY` or explicitly labeled `DEMO`, HTTP 503 for `UNAVAILABLE` or `INVALID_ARTIFACT`. Response includes `ready`, `status`, optional `modelVersion`, and `demo`, without paths or exception details.

Run Python–Java parity and real API integration against the exported bundle, in addition to the ordinary test suite:

```sh
sh mvnw test -Dmedpredict.model.test-directory=/absolute/path/to/frontend/ml/artifacts
```

The optional bundle tests require `golden.json` with synthetic inputs and expected Python outputs. They compare Java ONNX predictions at the declared tolerance (at most 0.01 mmHg). Ordinary tests separately cover synthetic ONNX fixtures and unavailable/corrupt artifacts.

The result is an experimental estimate of a current systolic measurement. It does not forecast future pressure, diagnose hypertension, replace a validated blood-pressure device, or support changing medication. Test performance on NHANES does not establish performance for Kazakhstan's population or clinical validation.

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
