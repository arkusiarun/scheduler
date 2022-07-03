package com.airtel.scheduler.execution.repository;

import com.airtel.scheduler.execution.model.ActivityDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Arun Singh
 */

@Repository
public interface ActivityDefinitionRepository extends MongoRepository<ActivityDefinition, String> {
}
