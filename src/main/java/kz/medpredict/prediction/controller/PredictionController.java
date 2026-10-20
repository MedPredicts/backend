package kz.medpredict.prediction.controller;

import com.fasterxml.jackson.databind.JsonNode;
import kz.medpredict.prediction.model.dto.ModelInfo;
import kz.medpredict.prediction.model.dto.Prediction;
import kz.medpredict.prediction.service.PredictionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PredictionController {
    private final PredictionService service;

    public PredictionController(PredictionService service) {
        this.service = service;
    }

    @GetMapping("/model/info")
    public ModelInfo info() {
        return service.modelInfo();
    }

    @PostMapping(value = "/predictions", consumes = "application/json")
    public Prediction predict(@RequestBody JsonNode body) {
        return service.predict(body);
    }
}
