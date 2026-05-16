package com.cloud.optimizer.model;

public class OptimizationSuggestion {

    private String recommendation;
    private String severity;
    private double estimatedCostSaving;

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public double getEstimatedCostSaving() {
        return estimatedCostSaving;
    }

    public void setEstimatedCostSaving(double estimatedCostSaving) {
        this.estimatedCostSaving = estimatedCostSaving;
    }
}