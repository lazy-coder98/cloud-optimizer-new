package com.cloud.optimizer.controller;

import com.cloud.optimizer.model.AiAdviceRequest;
import com.cloud.optimizer.model.AiAdviceResponse;
import com.cloud.optimizer.service.GeminiAdvisorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiAdvisorController {

    @Autowired
    private GeminiAdvisorService geminiAdvisorService;

    @PostMapping("/advice")
    public AiAdviceResponse getAdvice(@RequestBody AiAdviceRequest request) {
        return new AiAdviceResponse(geminiAdvisorService.generateAdvice(request));
    }
}
