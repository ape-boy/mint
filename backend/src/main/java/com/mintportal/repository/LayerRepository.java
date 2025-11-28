package com.mintportal.repository;

import com.mintportal.entity.Layer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LayerRepository extends JpaRepository<Layer, String> {

    List<Layer> findByProjectId(String projectId);

    List<Layer> findByProjectIdAndType(String projectId, String type);

    List<Layer> findByType(String type);
}
