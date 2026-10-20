# MedPredict backend

Java 17+ / Spring Boot 3.5 / Maven wrapper. Follow the feature-based packages and immutable DTO conventions in `SKILL.md`.

- `sh mvnw test`, `sh mvnw package` — checks; Windows uses `mvnw.cmd`.
- `sh mvnw spring-boot:run` — unavailable model by default; `-Dspring-boot.run.profiles=demo` — explicitly fixed demo.
- `MEDPREDICT_MODEL_DIRECTORY=/path/to/ml/artifacts sh mvnw spring-boot:run -Dspring-boot.run.profiles=onnx` — local ONNX CPU inference. `MEDPREDICT_MODEL_VERSION` optionally pins the exact version. `PORT` overrides 8080.
- `sh mvnw test -Dmedpredict.model.test-directory=/path/to/ml/artifacts` — optional real bundle parity/API tests using `golden.json`.
- `prediction/infrastructure/model/impl/OnnxPredictionModel` loads and hashes immutable ONNX bytes once; `ModelArtifactMetadata` validates the matching schema. Preprocessing is inside ONNX.
- `prediction/validation/` owns schema-driven field checks; preserve flat GET/POST contracts, localized field errors, and no-store responses.
- `/api/v1/health/live` checks HTTP liveness; `/api/v1/health/ready` and `/api/v1/model/status` expose safe model readiness.
- Never log, persist, or echo patient input. Never replace failed real inference with demo. Artifact field bounds describe the training domain, not medical normal ranges.
- This model estimates a current measurement; do not describe it as a forecast, diagnosis, clinical validation, or basis for changing medication.
