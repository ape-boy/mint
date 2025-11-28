package com.mintportal.repository;

import com.mintportal.entity.BuildQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildQueueRepository extends JpaRepository<BuildQueue, String> {

    // 대기 중인 빌드 조회 (우선순위 높은 순, 오래된 순)
    @Query("SELECT q FROM BuildQueue q " +
           "WHERE q.queueStatus = 'waiting' " +
           "ORDER BY q.priority DESC, q.queuedAt ASC")
    List<BuildQueue> findWaitingBuilds();

    // 다음 처리할 빌드 조회
    @Query("SELECT q FROM BuildQueue q " +
           "WHERE q.queueStatus = 'waiting' " +
           "ORDER BY q.priority DESC, q.queuedAt ASC " +
           "LIMIT 1")
    Optional<BuildQueue> findNextBuild();

    // 프로젝트별 대기 중인 빌드 조회
    List<BuildQueue> findByProjectIdAndQueueStatusOrderByPriorityDescQueuedAtAsc(
            String projectId, String queueStatus);

    // 레이어별 대기 중인 빌드 조회
    List<BuildQueue> findByLayerIdAndQueueStatusOrderByPriorityDescQueuedAtAsc(
            String layerId, String queueStatus);

    // 처리 중인 빌드 개수
    @Query("SELECT COUNT(q) FROM BuildQueue q WHERE q.queueStatus = 'processing'")
    long countProcessingBuilds();

    // 대기 중인 빌드 개수
    @Query("SELECT COUNT(q) FROM BuildQueue q WHERE q.queueStatus = 'waiting'")
    long countWaitingBuilds();

    // 상태별 빌드 큐 조회
    List<BuildQueue> findByQueueStatusOrderByPriorityDescQueuedAtAsc(String queueStatus);

    // 사용자별 대기 중인 빌드 조회
    List<BuildQueue> findByRequesterIdAndQueueStatusIn(String requesterId, List<String> statuses);

    // 재시도 가능한 실패 빌드 조회
    @Query("SELECT q FROM BuildQueue q " +
           "WHERE q.queueStatus = 'waiting' " +
           "AND q.retryCount > 0 " +
           "AND q.retryCount < q.maxRetries " +
           "ORDER BY q.priority DESC, q.queuedAt ASC")
    List<BuildQueue> findRetryableBuilds();
}
