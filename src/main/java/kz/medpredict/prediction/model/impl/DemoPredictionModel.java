package kz.medpredict.prediction.model.impl;

import java.util.List;
import java.util.Map;
import kz.medpredict.prediction.model.PredictionModel;
import kz.medpredict.prediction.model.dto.Field;
import kz.medpredict.prediction.model.dto.ModelInfo;
import kz.medpredict.prediction.model.dto.Option;
import kz.medpredict.prediction.model.dto.Prediction;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
public class DemoPredictionModel implements PredictionModel {
    private static final String VERSION = "demo-example";
    private static final String UNIT = "мм рт. ст.";
    private static final ModelInfo INFO = new ModelInfo(
            "Систолическое давление — фиксированный DEMO-пример", UNIT, 1, VERSION,
            List.of(number("age", "Возраст", "лет"),
                    new Field("sex", "Пол", "select", true, null, null, null, null,
                            List.of(new Option("male", "Мужской"), new Option("female", "Женский"))),
                    number("bmi", "Индекс массы тела", "кг/м²"),
                    number("totalCholesterol", "Общий холестерин", "ммоль/л"),
                    number("glucose", "Глюкоза", "ммоль/л")));

    @Override
    public ModelInfo info() {
        return INFO;
    }

    @Override
    public Prediction predict(Map<String, Object> input) {
        return new Prediction(134.2, UNIT, VERSION,
                "DEMO: фиксированный пример, не рассчитан по введённым данным. Не является диагнозом.");
    }

    private static Field number(String key, String label, String unit) {
        // Medical constraints must come from the agreed model schema.
        return new Field(key, label, "number", true, unit, null, null, null, null);
    }
}
