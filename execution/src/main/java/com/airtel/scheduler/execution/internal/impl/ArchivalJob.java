package com.airtel.scheduler.execution.internal.impl;

import com.airtel.scheduler.execution.enums.InternalJobsEnum;
import com.airtel.scheduler.execution.enums.Status;
import com.airtel.scheduler.execution.internal.InternalJobService;
import com.airtel.scheduler.execution.model.ArchivedTask;
import com.airtel.scheduler.execution.model.Task;
import com.airtel.scheduler.execution.properties.InternalJobConfigurations;
import com.airtel.scheduler.execution.repository.ArchivedTaskRepository;
import com.airtel.scheduler.execution.repository.CustomRepository;
import com.airtel.scheduler.execution.repository.TaskRepository;
import com.airtel.scheduler.execution.utils.CommonUtils;
import com.airtel.scheduler.execution.utils.SchedulerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ArchivalJob implements InternalJobService {

    private final Logger logger = LoggerFactory.getLogger(ArchivalJob.class);

    private TaskRepository taskRepository;
    private CustomRepository customRepository;
    private ArchivedTaskRepository archivedTaskRepository;
    private InternalJobConfigurations jobConfiguration;

    @Autowired
    public ArchivalJob(TaskRepository taskRepository,
                          CustomRepository customRepository,
                          ArchivedTaskRepository archivedTaskRepository,
                       InternalJobConfigurations jobConfiguration) {
        this.taskRepository = taskRepository;
        this.customRepository = customRepository;
        this.archivedTaskRepository = archivedTaskRepository;
        this.jobConfiguration = jobConfiguration;
    }

    @Override
    public InternalJobsEnum getInternalJob() {
        return InternalJobsEnum.ARCHIVAL;
    }

    @Override
    public void execute(Map<String, Object> meta) {
        logger.info("Archival Job Started at Time : {}", LocalDateTime.now());
        List<ArchivedTask> archivedTaskList = new ArrayList<>();
        List<Task> tasks = new ArrayList<>();
        List<Status> exclusionStatus = (List<Status>) meta.get(this.jobConfiguration.getExcludeStatusKey());
        Integer limit = (Integer) meta.get(this.jobConfiguration.getArchivalLimitKey());
        Optional.of(this.customRepository.fetchTasksForArchival(exclusionStatus, limit))
                .ifPresent(taskList -> taskList.forEach(task -> {
                    try {
                        archivedTaskList.add(SchedulerUtils.convertTaskToArchived(task));
                        tasks.add(task);
                    } catch (Exception e) {
                        logger.error("Not able Creat Archival Task For TaskId: {}", task.getId());
                    }
                }));
        if (!CommonUtils.isEmptyList(archivedTaskList) && !CommonUtils.isEmptyList(tasks)) {
            logger.info("Adding : {}, to Archival Collection", archivedTaskList.size());
            this.archivedTaskRepository.saveAll(archivedTaskList);
            logger.info("Removing : {}, From Tasks Collection", tasks.size());
            this.taskRepository.deleteAll(tasks);
        } else {
            logger.info("No Task Found For Archival");
        }
    }
}