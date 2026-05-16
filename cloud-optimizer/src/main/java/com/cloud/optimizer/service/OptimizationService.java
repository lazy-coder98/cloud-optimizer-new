package com.cloud.optimizer.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.cloud.optimizer.model.OptimizationRequest;
import com.cloud.optimizer.model.OptimizationSuggestion;
import com.cloud.optimizer.model.UsageRecord;
import com.cloud.optimizer.repository.UsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OptimizationService {

    // ✅ Dependency first (best practice)
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
        // ---- CPU ANALYSIS ----
        if (cpu > 80) {
            suggestion.setRecommendation("Scale UP compute instance");
            suggestion.setSeverity("HIGH");
            suggestion.setEstimatedCostSaving(-20);
        }
        else if (cpu < 20) {
            suggestion.setRecommendation("Downsize instance to save cost");
            suggestion.setSeverity("MEDIUM");
            suggestion.setEstimatedCostSaving(35);
        }
        else {
            suggestion.setRecommendation("CPU usage optimal");
            suggestion.setSeverity("LOW");
            suggestion.setEstimatedCostSaving(0);
        }

        // Memory logic
        if (memory > 85) {
            suggestion.setRecommendation(
                    suggestion.getRecommendation() + " + Increase RAM allocation"
            );
        }

        // Storage logic
        if (storage > 90) {
            suggestion.setRecommendation(
                    suggestion.getRecommendation() + " + Clean unused storage"
            );
        }

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

        //usageRepository.save(record);

        return suggestion;
    }
}