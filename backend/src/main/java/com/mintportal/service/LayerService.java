package com.mintportal.service;

import com.mintportal.entity.Layer;
import com.mintportal.entity.Project;
import com.mintportal.repository.LayerRepository;
import com.mintportal.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LayerService {

    private final LayerRepository layerRepository;
    private final ProjectRepository projectRepository;

    public List<Layer> findAll() {
        return layerRepository.findAll();
    }

    public Layer findById(String id) {
        return layerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Layer not found: " + id));
    }

    public List<Layer> findByProjectId(String projectId) {
        return layerRepository.findByProjectId(projectId);
    }

    public List<Layer> findByProjectIdAndType(String projectId, String type) {
        return layerRepository.findByProjectIdAndType(projectId, type);
    }

    @Transactional
    public Layer create(Layer layer, String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));

        if (layer.getId() == null || layer.getId().isEmpty()) {
            layer.setId(UUID.randomUUID().toString());
        }
        layer.setProject(project);
        return layerRepository.save(layer);
    }

    @Transactional
    public Layer update(String id, Layer layer) {
        Layer existing = findById(id);

        // Update fields that exist in the V3 schema
        if (layer.getName() != null) {
            existing.setName(layer.getName());
        }
        if (layer.getType() != null) {
            existing.setType(layer.getType());
        }
        if (layer.getLayerPath() != null) {
            existing.setLayerPath(layer.getLayerPath());
        }
        if (layer.getBuildEnabled() != null) {
            existing.setBuildEnabled(layer.getBuildEnabled());
        }
        if (layer.getSamEnabled() != null) {
            existing.setSamEnabled(layer.getSamEnabled());
        }
        if (layer.getCoverityEnabled() != null) {
            existing.setCoverityEnabled(layer.getCoverityEnabled());
        }
        if (layer.getLayerConfig() != null) {
            existing.setLayerConfig(layer.getLayerConfig());
        }

        return layerRepository.save(existing);
    }

    @Transactional
    public void delete(String id) {
        layerRepository.deleteById(id);
    }
}
