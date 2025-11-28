package com.mintportal.repository;

import com.mintportal.entity.BuildStageResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildStageResultRepository extends JpaRepository<BuildStageResult, String> {

    // 빌드별 스테이지 결과 조회 (순서대로)
    List<BuildStageResult> findByBuildIdOrderByStageOrder(String buildId);

    // 빌드 및 스테이지 이름으로 조회
    Optional<BuildStageResult> findByBuildIdAndStageName(String buildId, String stageName);

    // 특정 스테이지 이름으로 조회
    List<BuildStageResult> findByStageNameOrderByReceivedAtDesc(String stageName);

    // 상태별 스테이지 결과 조회
    List<BuildStageResult> findByStatusOrderByReceivedAtDesc(String status);

    // 빌드의 완료되지 않은 스테이지 확인
    @Query("SELECT COUNT(s) FROM BuildStageResult s " +
           "WHERE s.build.id = :buildId " +
           "AND s.status IN ('pending', 'running')")
    long countIncompleteStages(@Param("buildId") String buildId);

    // 빌드의 실패한 스테이지 확인
    @Query("SELECT COUNT(s) FROM BuildStageResult s " +
           "WHERE s.build.id = :buildId " +
           "AND s.status = 'failed'")
    long countFailedStages(@Param("buildId") String buildId);

    // 빌드의 모든 스테이지가 완료되었는지 확인
    @Query("SELECT CASE WHEN COUNT(s) = 0 THEN true ELSE false END " +
           "FROM BuildStageResult s " +
           "WHERE s.build.id = :buildId " +
           "AND s.status IN ('pending', 'running')")
    boolean areAllStagesComplete(@Param("buildId") String buildId);

    // 빌드의 최종 상태 계산
    @Query("SELECT CASE " +
           "WHEN COUNT(CASE WHEN s.status = 'failed' THEN 1 END) > 0 THEN 'failed' " +
           "WHEN COUNT(CASE WHEN s.status IN ('pending', 'running') THEN 1 END) > 0 THEN 'running' " +
           "ELSE 'success' END " +
           "FROM BuildStageResult s " +
           "WHERE s.build.id = :buildId")
    String calculateBuildStatus(@Param("buildId") String buildId);

    // 스테이지별 평균 실행 시간
    @Query("SELECT s.stageName, AVG(s.durationSeconds) " +
           "FROM BuildStageResult s " +
           "WHERE s.status = 'success' " +
           "AND s.durationSeconds IS NOT NULL " +
           "GROUP BY s.stageName")
    List<Object[]> getAverageDurationByStage();

    // 스테이지별 에러/경고 합계
    @Query("SELECT s.stageName, SUM(s.errorCount), SUM(s.warningCount) " +
           "FROM BuildStageResult s " +
           "WHERE s.build.id = :buildId " +
           "GROUP BY s.stageName " +
           "ORDER BY s.stageName")
    List<Object[]> getErrorWarningCountsByBuild(@Param("buildId") String buildId);
}
