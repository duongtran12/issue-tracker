package com.duong.issue_tracker.repository;

import com.duong.issue_tracker.entity.IssueHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueHistoryRepository extends JpaRepository<IssueHistory, Long> {

    List<IssueHistory> findAllByIssueIdOrderByCreatedAtAsc(Long issueId);
}
