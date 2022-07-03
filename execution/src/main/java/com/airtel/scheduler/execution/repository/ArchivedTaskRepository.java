package com.airtel.scheduler.execution.repository;

import com.airtel.scheduler.execution.model.ArchivedTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivedTaskRepository extends MongoRepository<ArchivedTask, String> {
}