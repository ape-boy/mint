package com.mintportal.service;

import com.mintportal.entity.TaskGroup;
import com.mintportal.repository.TaskGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskGroupService {

    private final TaskGroupRepository taskGroupRepository;

    public List<TaskGroup> findAll() {
        return taskGroupRepository.findAll();
    }

    public TaskGroup findById(String id) {
        return taskGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TaskGroup not found: " + id));
    }

    @Transactional
    public TaskGroup create(TaskGroup taskGroup) {
        if (taskGroup.getId() == null || taskGroup.getId().isEmpty()) {
            taskGroup.setId(UUID.randomUUID().toString());
        }
        return taskGroupRepository.save(taskGroup);
    }

    @Transactional
    public TaskGroup update(String id, TaskGroup taskGroup) {
        TaskGroup existing = findById(id);
        existing.setName(taskGroup.getName());
        existing.setProduct(taskGroup.getProduct());
        existing.setController(taskGroup.getController());
        return taskGroupRepository.save(existing);
    }

    @Transactional
    public void delete(String id) {
        taskGroupRepository.deleteById(id);
    }
}
