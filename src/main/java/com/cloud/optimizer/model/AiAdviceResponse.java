package com.cloud.optimizer.model;

public class AiAdviceResponse {

    private String advice;

    public AiAdviceResponse(String advice) {
        this.advice = advice;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }
}
