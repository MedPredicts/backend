package kz.medpredict.prediction.controller;

import java.util.Map;
import kz.medpredict.prediction.model.dto.PredictionModelStatusResponse;
import kz.medpredict.prediction.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ModelHealthController {
    private final PredictionService service;

    @GetMapping("/health/live")
    public Map<String, String> live() {
        return Map.of("status", "UP");
    }

    @GetMapping({"/model/status", "/health/ready"})
    public ResponseEntity<PredictionModelStatusResponse> ready() {
        var status = service.modelStatus();
        return ResponseEntity.status(status.isReady() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(status);
    }
}
