package com.cloud.optimizer.controller;

import jakarta.validation.Valid;

import com.cloud.optimizer.model.OptimizationRequest;
import com.cloud.optimizer.model.OptimizationSuggestion;
import com.cloud.optimizer.model.UsageRecord;
import com.cloud.optimizer.service.OptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/optimize")
public class OptimizationController {

    @Autowired
    private OptimizationService optimizationService;

    @PostMapping
    public OptimizationSuggestion optimize(
            @Valid @RequestBody OptimizationRequest request,
            Authentication authentication) {

        return optimizationService.analyzeResources(authentication.getName(), request);
    }

    @GetMapping("/history")
    public Page<UsageRecord> getHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return optimizationService.getHistory(authentication.getName(), page, size);
    }
}
