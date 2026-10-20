package kz.medpredict.prediction.infrastructure.model.impl;

import java.util.List;
import java.util.Map;
import kz.medpredict.prediction.infrastructure.model.PredictionModel;
import kz.medpredict.prediction.model.dto.PredictionFieldResponse;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import kz.medpredict.prediction.model.dto.PredictionFieldOptionResponse;
import kz.medpredict.prediction.model.dto.PredictionResultResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
public class DemoPredictionModel implements PredictionModel {
    private static final String VERSION = "demo-example";
    private static final String UNIT = "мм рт. ст.";
    private static final PredictionModelInfoResponse INFO = new PredictionModelInfoResponse(
            "Систолическое давление — фиксированный DEMO-пример", UNIT, 1, VERSION,
            List.of(number("age", "Возраст", "лет"),
                    new PredictionFieldResponse("sex", "Пол", "select", true, null, null, null, null,
                            List.of(new PredictionFieldOptionResponse("male", "Мужской"), new PredictionFieldOptionResponse("female", "Женский"))),
                    number("bmi", "Индекс массы тела", "кг/м²"),
                    number("totalCholesterol", "Общий холестерин", "ммоль/л"),
                    number("glucose", "Глюкоза", "ммоль/л")));

    @Override
    public PredictionModelInfoResponse info() {
        return INFO;
    }

    @Override
    public PredictionResultResponse predict(Map<String, Object> input) {
        return new PredictionResultResponse(134.2, UNIT, VERSION,
                "DEMO: фиксированный пример, не рассчитан по введённым данным. Не является диагнозом.");
    }

    private static PredictionFieldResponse number(String key, String label, String unit) {
        // Medical constraints must come from the agreed model schema.
        return new PredictionFieldResponse(key, label, "number", true, unit, null, null, null, null);
    }
}

