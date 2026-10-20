package kz.medpredict.prediction.model.dto;

public record Prediction(double predictedValue, String unit, String modelVersion, String disclaimer) {}
