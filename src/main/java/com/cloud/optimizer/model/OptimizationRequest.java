package com.cloud.optimizer.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

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

    private String provider;
    private String workloadType;

    @PositiveOrZero(message = "Monthly cost must be >= 0")
    private Double monthlyCost;

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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getWorkloadType() {
        return workloadType;
    }

    public void setWorkloadType(String workloadType) {
        this.workloadType = workloadType;
    }

    public Double getMonthlyCost() {
        return monthlyCost;
    }

    public void setMonthlyCost(Double monthlyCost) {
        this.monthlyCost = monthlyCost;
    }
}
