package com.cloud.optimizer.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MongoUriValidator {

    @Bean
    public static BeanFactoryPostProcessor validateMongoUri(Environment environment) {
        return beanFactory -> {
            String uri = environment.getProperty("spring.data.mongodb.uri", "").trim();

            if (!uri.startsWith("mongodb://") && !uri.startsWith("mongodb+srv://")) {
                throw new IllegalStateException(
                        "SPRING_DATA_MONGODB_URI must start with mongodb:// or mongodb+srv://. " +
                                "Check the Render environment variable value."
                );
            }
        };
    }
}
