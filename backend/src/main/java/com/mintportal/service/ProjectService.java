package com.mintportal.service;

import com.mintportal.entity.Project;
import com.mintportal.entity.TaskGroup;
import com.mintportal.repository.ProjectRepository;
import com.mintportal.repository.TaskGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskGroupRepository taskGroupRepository;

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Project findById(String id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found: " + id));
    }

    public List<Project> findByTaskGroupId(String taskGroupId) {
        return projectRepository.findByTaskGroupId(taskGroupId);
    }

    public List<Project> findByFilters(String taskGroupId, String oem, String status) {
        return projectRepository.findByFilters(taskGroupId, oem, status);
    }

    public List<Project> search(String query) {
        return projectRepository.searchByNameOrCode(query);
    }

    @Transactional
    public Project create(Project project, String taskGroupId) {
        TaskGroup taskGroup = taskGroupRepository.findById(taskGroupId)
                .orElseThrow(() -> new RuntimeException("TaskGroup not found: " + taskGroupId));

        if (project.getId() == null || project.getId().isEmpty()) {
            project.setId(UUID.randomUUID().toString());
        }
        project.setTaskGroup(taskGroup);
        return projectRepository.save(project);
    }

    @Transactional
    public Project update(String id, Project project) {
        Project existing = findById(id);

        // Update fields that exist in the V3 schema
        if (project.getProjectName() != null) {
            existing.setProjectName(project.getProjectName());
        }
        if (project.getProjectCode() != null) {
            existing.setProjectCode(project.getProjectCode());
        }
        if (project.getPlanId() != null) {
            existing.setPlanId(project.getPlanId());
        }
        if (project.getStatus() != null) {
            existing.setStatus(project.getStatus());
        }
        if (project.getOem() != null) {
            existing.setOem(project.getOem());
        }
        if (project.getPl() != null) {
            existing.setPl(project.getPl());
        }
        if (project.getTl() != null) {
            existing.setTl(project.getTl());
        }
        if (project.getScmConfig() != null) {
            existing.setScmConfig(project.getScmConfig());
        }
        if (project.getBuildConfig() != null) {
            existing.setBuildConfig(project.getBuildConfig());
        }
        if (project.getAnalysisConfig() != null) {
            existing.setAnalysisConfig(project.getAnalysisConfig());
        }
        if (project.getIsCertified() != null) {
            existing.setIsCertified(project.getIsCertified());
        }
        if (project.getLogPathTemplate() != null) {
            existing.setLogPathTemplate(project.getLogPathTemplate());
        }
        if (project.getCurrentMilestone() != null) {
            existing.setCurrentMilestone(project.getCurrentMilestone());
        }
        if (project.getKpiScore() != null) {
            existing.setKpiScore(project.getKpiScore());
        }

        return projectRepository.save(existing);
    }

    @Transactional
    public void delete(String id) {
        projectRepository.deleteById(id);
    }

    public long countActive() {
        return projectRepository.findByStatus("active").size();
    }
}
