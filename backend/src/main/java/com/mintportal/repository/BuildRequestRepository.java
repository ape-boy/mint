package com.mintportal.repository;

import com.mintportal.entity.BuildRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BuildRequestRepository extends JpaRepository<BuildRequest, String> {

    // Bamboo 요청 ID로 조회
    Optional<BuildRequest> findByBambooRequestId(String bambooRequestId);

    // 프로젝트별 요청 조회
    List<BuildRequest> findByProjectIdOrderBySentAtDesc(String projectId);

    // 레이어별 요청 조회
    List<BuildRequest> findByLayerIdOrderBySentAtDesc(String layerId);

    // 상태별 요청 조회
    List<BuildRequest> findByRequestStatusOrderBySentAtDesc(String requestStatus);

    // 특정 기간 내 요청 조회
    @Query("SELECT r FROM BuildRequest r " +
           "WHERE r.sentAt BETWEEN :startDate AND :endDate " +
           "ORDER BY r.sentAt DESC")
    List<BuildRequest> findByDateRange(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);

    // 타임아웃된 요청 조회 (응답 없이 특정 시간 경과)
    @Query("SELECT r FROM BuildRequest r " +
           "WHERE r.requestStatus = 'sent' " +
           "AND r.sentAt < :cutoffTime")
    List<BuildRequest> findTimedOutRequests(@Param("cutoffTime") OffsetDateTime cutoffTime);

    // 최근 요청 조회
    @Query("SELECT r FROM BuildRequest r ORDER BY r.sentAt DESC")
    List<BuildRequest> findRecentRequests();

    // 프로젝트 및 레이어별 최근 요청 조회
    Optional<BuildRequest> findFirstByProjectIdAndLayerIdOrderBySentAtDesc(
            String projectId, String layerId);

    // 큐 ID로 요청 조회
    Optional<BuildRequest> findByQueueId(String queueId);
}
