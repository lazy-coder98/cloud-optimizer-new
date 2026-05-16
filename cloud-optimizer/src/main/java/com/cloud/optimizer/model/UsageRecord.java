package com.cloud.optimizer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usage")
public class UsageRecord {

    @Id
    private String id;

    private Double cpuUsage;
    private Double memoryUsage;
    private Double storageUsage;

    private String recommendation;
    private String severity;
    private Double estimatedCostSaving;

    public UsageRecord() {
    }

    // ===== GETTERS & SETTERS =====

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public Double getStorageUsage() {
        return storageUsage;
    }

    public void setStorageUsage(Double storageUsage) {
        this.storageUsage = storageUsage;
    }

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

    public Double getEstimatedCostSaving() {
        return estimatedCostSaving;
    }

    public void setEstimatedCostSaving(Double estimatedCostSaving) {
        this.estimatedCostSaving = estimatedCostSaving;
    }
}