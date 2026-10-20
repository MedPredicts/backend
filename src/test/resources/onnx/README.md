# Synthetic ONNX test fixtures

These four tiny mathematical graphs are not trained medical models and are never used by the application profile. They test actual native ONNX Runtime behavior without requiring Python during `mvnw test`.

- `synthetic-affine.onnx`: `ReduceSum(features * [1,2,3,4,5], axes=[1], keepdims=1) + 100`. Distinct mathematical coefficients make feature-order errors visible.
- `wrong-input-shape.onnx`: same graph with input width 4 and coefficients `[1,2,3,4]` instead of width 5.
- `wrong-output-shape.onnx`: same graph with `keepdims=0`, so the output is rank 1.
- `non-finite-output.onnx`: `ReduceSum(features * [1,2,3,4,5], axes=[1], keepdims=1) / 0`.

Generated locally with ONNX 1.19.1 `helper.make_graph`, `helper.make_model` and `checker.check_model`: IR version 9, standard opset 17, float32 input `features`, float32 output `predictedValue`, symbolic batch dimension. They contain only the named operators and scalar/axes initializers, with no observations, weights from training, or patient information. The test computes the fixture SHA-256 when assembling each temporary bundle.
