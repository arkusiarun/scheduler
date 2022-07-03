package com.airtel.scheduler.execution.repository;

import com.airtel.scheduler.execution.model.EventConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventConfigRepository extends MongoRepository<EventConfig, String> {
}