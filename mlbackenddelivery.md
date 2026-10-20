# Интеграция модели MedPredict в backend

Основа: публичный `MedPredicts/backend`, ветка `main`, коммит `3588c363e3d34946d86ce75be39a5695a8073176`. Изменения подготовлены в отдельном checkout. Коммиты, push и развёртывание не выполнялись; существующий demo-сервер на 8080 сохранён.

## Что реализовано

- Провайдер `OnnxPredictionModel` в существующем интерфейсе `PredictionModel`, без отдельного Python-сервера.
- Явный профиль `onnx`; без профиля остаётся 503, `demo` сохраняет отдельный фиксированный пример. Одновременный `onnx,demo` не запускается.
- Загрузка одного ONNX-сеанса при старте, проверка схемы metadata версии 1, SHA-256 фактически загруженных байтов и необязательной ожидаемой версии через `MEDPREDICT_MODEL_VERSION`.
- Точный порядок `age, sex, bmi, totalCholesterol, glucose`, `male=0, female=1`; вход `features: float32[batch,5]`, выход `predictedValue: float32[batch,1]`. Предобработка и возврат к мм рт. ст. находятся внутри графа ONNX.
- Проверка единиц и конечных границ числового домена из артефакта, защита известных полей и локализованные ошибки. Границы формы отражают обучающий домен, а не медицинскую норму.
- Обратная совместимость `GET /api/v1/model/info` и `POST /api/v1/predictions`: прежние ключи сохранены; добавлены необязательные `predictionType` и `demo`. Серверное demo явно отмечено `demo:true`.
- Liveness `/api/v1/health/live`; readiness `/api/v1/health/ready` и `/api/v1/model/status`. Статусы не содержат путей, исключений и введённых данных.
- Отсутствующий или некорректный артефакт и ошибки inference не дают фиктивных чисел, автоматического перехода в demo нет. Входные показатели не сохраняются и не логируются.

## Команды

Стандартная среда — JDK 17+ и Maven wrapper:

```sh
sh mvnw test
MEDPREDICT_MODEL_DIRECTORY=/absolute/path/to/frontend/ml/artifacts \
  sh mvnw spring-boot:run -Dspring-boot.run.profiles=onnx
sh mvnw test -Dmedpredict.model.test-directory=/absolute/path/to/frontend/ml/artifacts
```

Для параллельного сохранения старого demo-сервера используется `PORT=8081`. В рабочей среде macOS проверены JDK 21.0.8 arm64, Maven 3.9.16 и ONNX Runtime Java CPU 1.24.3. Нативный probe реально загрузил runtime и сообщил доступный CPU provider.

Настройки данной машины для повторения:

```sh
export JAVA_HOME=/Users/admin/Library/Java/JavaVirtualMachines/ms-21.0.8/Contents/Home
export MAVEN_USER_HOME=/private/tmp/medpredict-backend-maven-20261021
sh mvnw -Dmaven.repo.local=/private/tmp/medpredict-backend-maven-20261021/repository test
```

Mockito в ограниченной песочнице не может подключить Java-agent к JVM; проверка в разрешённом окружении выполняется успешно. Это ограничение окружения тестов, а не ошибка приложения.

## Выполненные проверки

Итоговые проверки ниже выполнены заново с исправленным артефактом **v2**. Предварительный v1 отозван после обнаружения ошибки декодирования нулевых весов SAS XPT в pandas; сервер с ним остановлен, его метрики не используются как итоговые. Текущий bundle построен после повторного чтения данных и обучения; model schema осталась совместимой.

- Полный `mvnw test` с настоящим bundle: **48 тестов, 0 ошибок, 0 падений, 0 пропусков**.
- Java ONNX Runtime 1.24.3 и Python: 4 синтетических эталона, максимальное абсолютное расхождение **0.00000762939453125 мм рт. ст.** при допустимом 0.01.
- Версия модели `MedPredict-SBP-mlp-v2`; SHA-256 `3a4be6a1746f4230a5dba7346e3948e56cabc400540c91b0fbcd7882299dbe2a`.
- Подтверждён фактический запуск HTTP-сервера на 8081 и `GET /api/v1/health/ready` → `READY`, `demo:false`.
- Проверены отсутствие bundle, неверные метаданные, версия/единицы/категории, неверная контрольная сумма, повреждённый ONNX с правильной суммой, ошибочные входная/выходная формы тензоров и бесконечный результат inference. Ошибки не возвращают фиктивного прогноза.
- 4 отдельные математические ONNX-fixture выполняются через настоящий native runtime. Разные коэффициенты в корректном графе выявляют ошибки порядка признаков; пример не является обученной медицинской моделью.

Агрегированный отчёт Java: `target/model-parity-report.json`, полный вывод проверки: `/private/tmp/medpredict-backend-ml-tests-v2.log`. Golden/API suite не использует наблюдения пациентов и не выводит введённые данные в логи. Дополнительно `mvnw -DskipTests package` собрал runnable Spring Boot JAR с ONNX Runtime; тестовые ONNX-fixture в него не входят.

## Проверка реального артефакта

`OnnxBundleIntegrationTest` запускается только с явным `-Dmedpredict.model.test-directory=...`. Он требует `metadata.json`, `model.onnx` и `golden.json`, проверяет успешный запуск настоящего провайдера, полный HTTP-сценарий, конечные разные результаты для нескольких синтетических входов, версию/единицы/семантику и отклонение ввода вне домена. Прогноз Java должен совпасть с Python в заявленном допуске не более 0.01 мм рт. ст.

`golden.json` создаётся экспортом автоматически и содержит `absoluteTolerance`, `featureOrder` и массив `cases`. Каждый случай включает синтетический объект `input` со строковым `sex` (`male`/`female`) и вычисленное исходной Python-моделью число `expected`. Минимум два эталона должны давать различные результаты.

Исследовательские ограничения: оценка текущего систолического измерения, без прогноза будущего давления и без диагностики. Нельзя заменять измерение тонометром или менять лекарства по результату. NHANES не устанавливает применимость к населению Казахстана или клиническую валидацию.

Источники: [официальный ONNX Runtime Java](https://onnxruntime.ai/docs/get-started/with-java.html), [артефакт CPU 1.24.3 в Maven Central](https://repo.maven.apache.org/maven2/com/microsoft/onnxruntime/onnxruntime/1.24.3/).
