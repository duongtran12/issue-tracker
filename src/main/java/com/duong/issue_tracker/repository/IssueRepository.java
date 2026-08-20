package com.duong.issue_tracker.repository;

import com.duong.issue_tracker.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findAllByProjectIdOrderByCreatedAtDesc(Long projectId);

    Optional<Issue> findByIdAndProjectId(Long id, Long projectId);
}
