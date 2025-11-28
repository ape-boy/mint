package com.mintportal.repository;

import com.mintportal.entity.Build;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BuildRepository extends JpaRepository<Build, String> {

    List<Build> findByProjectId(String projectId);

    List<Build> findByLayerId(String layerId);

    List<Build> findByProjectIdAndLayerId(String projectId, String layerId);

    List<Build> findByStatus(String status);

    // Bamboo 빌드 키로 조회
    Optional<Build> findByBambooBuildKey(String bambooBuildKey);

    @Query("SELECT b FROM Build b WHERE b.status IN ('pending', 'running') ORDER BY b.startedAt ASC")
    List<Build> findActiveBuilds();

    @Query("SELECT b FROM Build b WHERE " +
           "(:projectId IS NULL OR b.project.id = :projectId) AND " +
           "(:layerId IS NULL OR b.layer.id = :layerId) AND " +
           "(:status IS NULL OR b.status = :status) " +
           "ORDER BY b.startedAt DESC NULLS LAST")
    List<Build> findByFilters(
            @Param("projectId") String projectId,
            @Param("layerId") String layerId,
            @Param("status") String status);

    @Query("SELECT b FROM Build b WHERE b.project.id = :projectId ORDER BY b.startedAt DESC NULLS LAST")
    Page<Build> findRecentByProjectId(@Param("projectId") String projectId, Pageable pageable);

    @Query("SELECT b FROM Build b WHERE b.layer.id = :layerId ORDER BY b.startedAt DESC NULLS LAST")
    List<Build> findRecentByLayerId(@Param("layerId") String layerId, Pageable pageable);

    @Query("SELECT MAX(b.round) FROM Build b WHERE b.layer.id = :layerId")
    Integer findMaxRoundByLayerId(@Param("layerId") String layerId);

    @Query("SELECT MAX(b.buildNumber) FROM Build b WHERE b.project.id = :projectId")
    Integer findMaxBuildNumberByProjectId(@Param("projectId") String projectId);

    long countByStatus(String status);

    @Query("SELECT COUNT(b) FROM Build b WHERE b.status = 'success'")
    long countSuccessBuilds();

    @Query("SELECT COUNT(b) FROM Build b")
    long countTotalBuilds();

    @Query("SELECT COUNT(b) FROM Build b WHERE b.status = 'running'")
    long countRunningBuilds();

    @Query("SELECT COUNT(b) FROM Build b WHERE b.status = 'failed'")
    long countFailedBuilds();

    // 성공률 계산
    @Query("SELECT CASE WHEN COUNT(b) = 0 THEN 0.0 " +
           "ELSE CAST(COUNT(CASE WHEN b.status = 'success' THEN 1 END) AS double) * 100 / COUNT(b) END " +
           "FROM Build b")
    Double calculateSuccessRate();

    // 특정 기간 내 빌드 조회
    @Query("SELECT b FROM Build b WHERE b.startedAt BETWEEN :startDate AND :endDate ORDER BY b.startedAt DESC")
    List<Build> findByDateRange(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);

    // 릴리즈 상태별 빌드 조회 (Release Layer only)
    List<Build> findByReleaseStatusOrderByStartedAtDesc(String releaseStatus);

    // 최근 빌드 조회 (전체)
    @Query("SELECT b FROM Build b ORDER BY b.startedAt DESC NULLS LAST")
    List<Build> findRecentBuilds(Pageable pageable);

    // 트리거 타입별 빌드 조회
    List<Build> findByTriggerTypeOrderByStartedAtDesc(String triggerType);
}
