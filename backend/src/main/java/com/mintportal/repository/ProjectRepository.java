package com.mintportal.repository;

import com.mintportal.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    List<Project> findByTaskGroupId(String taskGroupId);

    List<Project> findByStatus(String status);

    List<Project> findByOem(String oem);

    // Plan ID로 조회
    Optional<Project> findByPlanId(String planId);

    @Query("SELECT p FROM Project p WHERE " +
           "(:taskGroupId IS NULL OR p.taskGroup.id = :taskGroupId) AND " +
           "(:oem IS NULL OR p.oem = :oem) AND " +
           "(:status IS NULL OR p.status = :status)")
    List<Project> findByFilters(
            @Param("taskGroupId") String taskGroupId,
            @Param("oem") String oem,
            @Param("status") String status);

    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.projectName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.projectCode) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Project> searchByNameOrCode(@Param("search") String search);

    // 활성 프로젝트 개수
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = 'active'")
    long countActiveProjects();

    // PL이 담당하는 프로젝트 조회
    List<Project> findByPlId(String plId);

    // TL이 담당하는 프로젝트 조회
    List<Project> findByTlId(String tlId);
}
