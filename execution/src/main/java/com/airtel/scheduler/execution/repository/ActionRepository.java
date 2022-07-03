package com.airtel.scheduler.execution.repository;

import com.airtel.scheduler.execution.model.Action;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Arun Singh
 */

@Repository
public interface ActionRepository extends MongoRepository<Action, String> {
}
