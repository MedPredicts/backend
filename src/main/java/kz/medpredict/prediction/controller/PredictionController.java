package kz.medpredict.prediction.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.medpredict.core.exception.dto.ApiErrorResponse;
import kz.medpredict.core.exception.dto.ApiValidationErrorResponse;
import kz.medpredict.prediction.model.dto.PredictionCreateRequest;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import kz.medpredict.prediction.model.dto.PredictionResultResponse;
import kz.medpredict.prediction.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Predictions", description = "Model metadata and explicit prediction requests")
@ApiResponses({
        @ApiResponse(responseCode = "503", description = "Model unavailable or incompatible metadata/output",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(value = "{\"error\":\"MODEL_UNAVAILABLE\",\"message\":\"No trained model is configured.\"}"))),
        @ApiResponse(responseCode = "500", description = "Unexpected failure; internal details are never returned",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class),
                        examples = @ExampleObject(value = "{\"error\":\"SYSTEM_ERROR\",\"message\":\"An unexpected server error occurred.\"}")))
})
public class PredictionController {
    private final PredictionService service;

    @GetMapping("/model/info")
    @Parameters({@Parameter(name = "Accept-Language", in = ParameterIn.HEADER,
            description = "Error language: ru, en or kk; regional tags and language priorities are supported. "
                    + "Missing/unsupported languages use the configured default.",
            schema = @Schema(type = "string", example = "en"))})
    @Operation(operationId = "getPredictionModelInfo", summary = "Get the active model input schema",
            description = "Returns ordered fields, units, constraints and output precision. The default provider returns 503.")
    @ApiResponse(responseCode = "200", description = "Validated model metadata",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PredictionModelInfoResponse.class),
                    examples = @ExampleObject(name = "Demo metadata only", value = """
                            {"indicatorName":"Систолическое давление — фиксированный DEMO-пример",
                             "unit":"мм рт. ст.","decimalPlaces":1,"modelVersion":"demo-example",
                             "fields":[
                               {"key":"age","label":"Возраст","type":"number","required":true,"unit":"лет"},
                               {"key":"sex","label":"Пол","type":"select","required":true,
                                "options":[{"value":"male","label":"Мужской"},{"value":"female","label":"Женский"}]},
                               {"key":"bmi","label":"Индекс массы тела","type":"number","required":true,"unit":"кг/м²"},
                               {"key":"totalCholesterol","label":"Общий холестерин","type":"number","required":true,"unit":"ммоль/л"},
                               {"key":"glucose","label":"Глюкоза","type":"number","required":true,"unit":"ммоль/л"}]}
                            """)))
    public PredictionModelInfoResponse info() {
        return service.modelInfo();
    }

    @PostMapping(value = "/predictions", consumes = "application/json")
    @Parameters({@Parameter(name = "Accept-Language", in = ParameterIn.HEADER,
            description = "Error language: ru, en or kk; regional tags and language priorities are supported. "
                    + "Missing/unsupported languages use the configured default.",
            schema = @Schema(type = "string", example = "en"))})
    @Operation(operationId = "createPrediction", summary = "Create a prediction from model-specific fields",
            description = "Validates against current model metadata. Demo returns 134.2 regardless of input and is explicitly labeled. "
                    + "No automatic retry or demo fallback occurs.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/PredictionCreateRequest"),
                            examples = @ExampleObject(name = "Demo input only", value = "{\"age\":52,\"sex\":\"male\",\"bmi\":27.4,\"totalCholesterol\":5.6,\"glucose\":5.9}"))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prediction with actual model version and disclaimer",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PredictionResultResponse.class),
                            examples = @ExampleObject(name = "Fixed demo example", value = "{\"predictedValue\":134.2,\"unit\":\"мм рт. ст.\",\"modelVersion\":\"demo-example\",\"disclaimer\":\"DEMO: фиксированный пример, не рассчитан по введённым данным. Не является диагнозом.\"}"))),
            @ApiResponse(responseCode = "400", description = "Known field failures use VALIDATION_ERROR; malformed JSON, wrong root shape or unknown keys use INVALID_REQUEST",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(oneOf = {ApiValidationErrorResponse.class, ApiErrorResponse.class}),
                            examples = {
                                    @ExampleObject(name = "English (en)", value = "{\"error\":\"VALIDATION_ERROR\",\"fields\":[{\"field\":\"age\",\"message\":\"This field is required.\"}]}"),
                                    @ExampleObject(name = "Russian (ru)", value = "{\"error\":\"VALIDATION_ERROR\",\"fields\":[{\"field\":\"age\",\"message\":\"Обязательное поле.\"}]}"),
                                    @ExampleObject(name = "Kazakh (kk)", value = "{\"error\":\"VALIDATION_ERROR\",\"fields\":[{\"field\":\"age\",\"message\":\"Бұл өріс міндетті.\"}]}"),
                                    @ExampleObject(name = "Invalid request (en)", value = "{\"error\":\"INVALID_REQUEST\",\"message\":\"Expected a JSON object with supported fields and valid values.\"}")
                            })),
            @ApiResponse(responseCode = "415", description = "Content-Type must be application/json",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public PredictionResultResponse predict(@Valid @RequestBody PredictionCreateRequest request) {
        return service.predict(request);
    }
}
