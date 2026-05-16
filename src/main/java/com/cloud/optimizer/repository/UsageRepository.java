package com.cloud.optimizer.repository;

import com.cloud.optimizer.model.UsageRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsageRepository
        extends MongoRepository<UsageRecord, String> {

    Page<UsageRecord> findByUsername(String username, Pageable pageable);
}
