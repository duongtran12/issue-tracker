package com.duong.issue_tracker.repository;

import com.duong.issue_tracker.entity.IssueComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueCommentRepository extends JpaRepository<IssueComment, Long> {

    List<IssueComment> findAllByIssueIdOrderByCreatedAtAsc(Long issueId);
}
