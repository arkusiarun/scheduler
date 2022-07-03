package com.airtel.scheduler.execution.repository;

import com.airtel.scheduler.execution.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Arun Singh
 */

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
}