package com.cloud.optimizer.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OptimizationRequest {

    @NotNull(message = "CPU usage is required")
    @Min(value = 0, message = "CPU must be >= 0")
    @Max(value = 100, message = "CPU must be <= 100")
    private Double cpuUsage;

    @NotNull(message = "Memory usage is required")
    @Min(value = 0, message = "Memory must be >= 0")
    @Max(value = 100, message = "Memory must be <= 100")
    private Double memoryUsage;

    @NotNull(message = "Storage usage is required")
    @Min(value = 0, message = "Storage must be >= 0")
    @Max(value = 100, message = "Storage must be <= 100")
    private Double storageUsage;

    // ✅ REQUIRED empty constructor
    public OptimizationRequest() {}

    // ===== GETTERS =====
    public Double getCpuUsage() {
        return cpuUsage;
    }

    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public Double getStorageUsage() {
        return storageUsage;
    }

    // ===== SETTERS =====
    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public void setStorageUsage(Double storageUsage) {
        this.storageUsage = storageUsage;
    }
}