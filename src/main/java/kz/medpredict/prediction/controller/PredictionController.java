package kz.medpredict.prediction.controller;

import com.fasterxml.jackson.databind.JsonNode;
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
public class PredictionController {
    private final PredictionService service;

    @GetMapping("/model/info")
    public PredictionModelInfoResponse info() {
        return service.modelInfo();
    }

    @PostMapping(value = "/predictions", consumes = "application/json")
    public PredictionResultResponse predict(@RequestBody JsonNode body) {
        return service.predict(body);
    }
}
