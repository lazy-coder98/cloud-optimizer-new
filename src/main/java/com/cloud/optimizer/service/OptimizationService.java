package com.cloud.optimizer.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.cloud.optimizer.model.OptimizationRequest;
import com.cloud.optimizer.model.OptimizationSuggestion;
import com.cloud.optimizer.model.UsageRecord;
import com.cloud.optimizer.repository.UsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OptimizationService {

    private static final Logger logger = LoggerFactory.getLogger(OptimizationService.class);

    @Autowired
    private UsageRepository usageRepository;

    public Page<UsageRecord> getHistory(String username, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "_id")
        );

        return usageRepository.findByUsername(username, pageable);
    }

    public OptimizationSuggestion analyzeResources(String username, OptimizationRequest request) {
        OptimizationSuggestion suggestion = new OptimizationSuggestion();

        double cpu = request.getCpuUsage() != null ? request.getCpuUsage() : 0;
        double memory = request.getMemoryUsage() != null ? request.getMemoryUsage() : 0;
        double storage = request.getStorageUsage() != null ? request.getStorageUsage() : 0;
        double monthlyCost = request.getMonthlyCost() != null ? request.getMonthlyCost() : 0;
        String provider = normalize(request.getProvider(), "AWS");
        String workloadType = normalize(request.getWorkloadType(), "Web app");

        List<String> recommendations = new ArrayList<>();
        List<String> rationale = new ArrayList<>();
        double estimatedCostSaving = 0;
        boolean needsScaling = false;
        boolean hasOptimizationOpportunity = false;

        if (cpu >= 90) {
            recommendations.add("Scale up compute instance");
            rationale.add("CPU is critically high, so performance risk is higher than savings.");
            estimatedCostSaving -= 25;
            needsScaling = true;
        } else if (cpu >= 80) {
            recommendations.add("Monitor CPU pressure and consider a larger instance");
            rationale.add("CPU is near saturation.");
            estimatedCostSaving -= 12;
            needsScaling = true;
        } else if (cpu >= 55) {
            recommendations.add("CPU usage is healthy");
            estimatedCostSaving += 4;
        } else if (cpu >= 30) {
            recommendations.add("Rightsize compute instance");
            rationale.add("CPU has consistent headroom.");
            estimatedCostSaving += 14;
            hasOptimizationOpportunity = true;
        } else {
            recommendations.add("Downsize underused compute instance");
            rationale.add("CPU is heavily underused.");
            estimatedCostSaving += 28;
            hasOptimizationOpportunity = true;
        }

        if (memory >= 92) {
            recommendations.add("Increase RAM allocation");
            rationale.add("Memory is critically high.");
            estimatedCostSaving -= 14;
            needsScaling = true;
        } else if (memory >= 80) {
            recommendations.add("Tune memory usage before scaling RAM");
            estimatedCostSaving -= 5;
            needsScaling = true;
        } else if (memory >= 50) {
            recommendations.add("Memory allocation looks balanced");
            estimatedCostSaving += 3;
        } else if (memory >= 25) {
            recommendations.add("Reduce memory allocation");
            estimatedCostSaving += 10;
            hasOptimizationOpportunity = true;
        } else {
            recommendations.add("Downsize heavily underused memory");
            rationale.add("Memory allocation is much higher than usage.");
            estimatedCostSaving += 18;
            hasOptimizationOpportunity = true;
        }

        if (storage >= 92) {
            recommendations.add("Clean unused storage and expand capacity soon");
            rationale.add("Storage is close to full.");
            estimatedCostSaving += 6;
            needsScaling = true;
        } else if (storage >= 75) {
            recommendations.add(providerStorageRecommendation(provider));
            estimatedCostSaving += 8;
            hasOptimizationOpportunity = true;
        } else if (storage >= 40) {
            recommendations.add("Storage usage is stable");
            estimatedCostSaving += 2;
        } else {
            recommendations.add("Remove or shrink unused storage volumes");
            estimatedCostSaving += 12;
            hasOptimizationOpportunity = true;
        }

        estimatedCostSaving += workloadAdjustment(workloadType, recommendations, rationale);
        estimatedCostSaving += providerAdjustment(provider, recommendations);

        double savingPercent = roundToOneDecimal(clamp(estimatedCostSaving, -35, 65));
        double savingAmount = monthlyCost > 0 ? roundToTwoDecimals(monthlyCost * savingPercent / 100.0) : 0;

        suggestion.setRecommendation(String.join(" + ", recommendations));
        suggestion.setSeverity(resolveSeverity(needsScaling, hasOptimizationOpportunity, savingPercent, monthlyCost));
        suggestion.setEstimatedCostSaving(savingPercent);
        suggestion.setEstimatedMonthlySavingAmount(savingAmount);
        suggestion.setRationale(String.join(" ", rationale));

        UsageRecord record = new UsageRecord();

        record.setUsername(username);
        record.setCpuUsage(cpu);
        record.setMemoryUsage(memory);
        record.setStorageUsage(storage);
        record.setProvider(provider);
        record.setWorkloadType(workloadType);
        record.setMonthlyCost(monthlyCost);
        record.setRecommendation(suggestion.getRecommendation());
        record.setSeverity(suggestion.getSeverity());
        record.setEstimatedCostSaving(suggestion.getEstimatedCostSaving());
        record.setEstimatedMonthlySavingAmount(suggestion.getEstimatedMonthlySavingAmount());
        record.setRationale(suggestion.getRationale());
        record.setCreatedAt(Instant.now());

        try {
            usageRepository.save(record);
        } catch (Exception ex) {
            logger.warn("Optimization generated but history could not be saved to MongoDB.", ex);
        }

        return suggestion;
    }

    private String providerStorageRecommendation(String provider) {
        String normalizedProvider = provider.toLowerCase(Locale.ROOT);

        if (normalizedProvider.contains("azure")) {
            return "Move cold data to Azure Blob cool or archive tier";
        }

        if (normalizedProvider.contains("gcp") || normalizedProvider.contains("google")) {
            return "Move cold data to Cloud Storage Nearline or Archive";
        }

        return "Archive cold data with S3 lifecycle rules";
    }

    private double providerAdjustment(String provider, List<String> recommendations) {
        String normalizedProvider = provider.toLowerCase(Locale.ROOT);

        if (normalizedProvider.contains("aws")) {
            recommendations.add("Review AWS Savings Plans for steady workloads");
            return 4;
        }

        if (normalizedProvider.contains("azure")) {
            recommendations.add("Review Azure Reserved VM Instances for predictable usage");
            return 4;
        }

        if (normalizedProvider.contains("gcp") || normalizedProvider.contains("google")) {
            recommendations.add("Review committed use discounts for predictable usage");
            return 4;
        }

        return 0;
    }

    private double workloadAdjustment(String workloadType, List<String> recommendations, List<String> rationale) {
        String normalizedWorkload = workloadType.toLowerCase(Locale.ROOT);

        if (normalizedWorkload.contains("production")) {
            recommendations.add("Use cautious rightsizing because this is a production workload");
            rationale.add("Production workloads should prioritize stability.");
            return -4;
        }

        if (normalizedWorkload.contains("batch")) {
            recommendations.add("Schedule batch jobs on lower-cost or off-peak capacity");
            return 6;
        }

        if (normalizedWorkload.contains("dev") || normalizedWorkload.contains("test")) {
            recommendations.add("Stop non-production resources outside working hours");
            return 10;
        }

        if (normalizedWorkload.contains("database")) {
            recommendations.add("Review backup retention, indexes, and storage tiering");
            return 2;
        }

        return 0;
    }

    private String resolveSeverity(
            boolean needsScaling,
            boolean hasOptimizationOpportunity,
            double estimatedCostSaving,
            double monthlyCost) {

        if (needsScaling && estimatedCostSaving < 0) {
            return "HIGH";
        }

        if (monthlyCost >= 1000 && estimatedCostSaving >= 15) {
            return "HIGH";
        }

        if (needsScaling || hasOptimizationOpportunity || estimatedCostSaving >= 15) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private String normalize(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
