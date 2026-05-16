package com.cloud.optimizer.controller;

import jakarta.validation.Valid;

import com.cloud.optimizer.model.OptimizationRequest;
import com.cloud.optimizer.model.OptimizationSuggestion;
import com.cloud.optimizer.model.UsageRecord;
import com.cloud.optimizer.service.OptimizationService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/optimize")
public class OptimizationController {

    @Autowired
    private OptimizationService optimizationService;

    // ================= POST : ANALYZE =================
    @PostMapping
    public OptimizationSuggestion optimize(
            @Valid @RequestBody OptimizationRequest request) {

        try {
            System.out.println("CPU: " + request.getCpuUsage());

            return optimizationService.analyzeResources(request);

        } catch (Exception e) {
            e.printStackTrace();   // 🔥 THIS IS IMPORTANT
            throw e;
        }
    }

    // ================= GET : HISTORY =================
    @GetMapping("/history")
    public Page<UsageRecord> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return optimizationService.getHistory(page, size);
    }
}