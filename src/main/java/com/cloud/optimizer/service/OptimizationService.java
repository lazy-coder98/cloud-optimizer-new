package com.cloud.optimizer.service;

import java.util.ArrayList;
import java.time.Instant;
import java.util.List;
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

    // ================= HISTORY =================
    public Page<UsageRecord> getHistory(int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "_id") // latest first
        );

        return usageRepository.findAll(pageable);
    }

    // ================= ANALYSIS =================
    public OptimizationSuggestion analyzeResources(OptimizationRequest request) {

        OptimizationSuggestion suggestion = new OptimizationSuggestion();

        double cpu = request.getCpuUsage() != null ? request.getCpuUsage() : 0;
        double memory = request.getMemoryUsage() != null ? request.getMemoryUsage() : 0;
        double storage = request.getStorageUsage() != null ? request.getStorageUsage() : 0;

        List<String> recommendations = new ArrayList<>();
        double estimatedCostSaving = 0;
        boolean needsScaling = false;
        boolean hasOptimizationOpportunity = false;

        if (cpu >= 90) {
            recommendations.add("Scale up compute instance");
            estimatedCostSaving -= 25;
            needsScaling = true;
        } else if (cpu >= 80) {
            recommendations.add("Monitor CPU pressure and consider a larger instance");
            estimatedCostSaving -= 12;
            needsScaling = true;
        } else if (cpu >= 55) {
            recommendations.add("CPU usage is healthy");
            estimatedCostSaving += 4;
        } else if (cpu >= 30) {
            recommendations.add("Rightsize compute instance");
            estimatedCostSaving += 14;
            hasOptimizationOpportunity = true;
        } else {
            recommendations.add("Downsize underused compute instance");
            estimatedCostSaving += 28;
            hasOptimizationOpportunity = true;
        }

        if (memory >= 92) {
            recommendations.add("Increase RAM allocation");
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
            estimatedCostSaving += 18;
            hasOptimizationOpportunity = true;
        }

        if (storage >= 92) {
            recommendations.add("Clean unused storage and expand capacity soon");
            estimatedCostSaving += 6;
            needsScaling = true;
        } else if (storage >= 75) {
            recommendations.add("Archive cold data");
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

        suggestion.setRecommendation(String.join(" + ", recommendations));
        suggestion.setSeverity(resolveSeverity(needsScaling, hasOptimizationOpportunity, estimatedCostSaving));
        suggestion.setEstimatedCostSaving(roundToOneDecimal(clamp(estimatedCostSaving, -35, 55)));

        // ===== SAVE TO MONGODB =====
        UsageRecord record = new UsageRecord();

        record.setCpuUsage(cpu);
        record.setMemoryUsage(memory);
        record.setStorageUsage(storage);

        record.setRecommendation(suggestion.getRecommendation());
        record.setSeverity(suggestion.getSeverity());
        record.setEstimatedCostSaving(
                suggestion.getEstimatedCostSaving()
        );
        record.setCreatedAt(Instant.now());

        try {
            usageRepository.save(record);
        } catch (Exception ex) {
            logger.warn("Optimization generated but history could not be saved to MongoDB.", ex);
        }

        return suggestion;
    }

    private String resolveSeverity(boolean needsScaling, boolean hasOptimizationOpportunity, double estimatedCostSaving) {
        if (needsScaling && estimatedCostSaving < 0) {
            return "HIGH";
        }

        if (needsScaling || hasOptimizationOpportunity || estimatedCostSaving >= 15) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
