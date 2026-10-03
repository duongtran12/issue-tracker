package com.duong.issue_tracker.repository;

import com.duong.issue_tracker.entity.IssueChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IssueChecklistItemRepository extends JpaRepository<IssueChecklistItem, Long> {

    List<IssueChecklistItem> findAllByIssueIdOrderByPositionAscIdAsc(Long issueId);

    Optional<IssueChecklistItem> findByIdAndIssueId(Long id, Long issueId);

    long countByIssueId(Long issueId);
}
