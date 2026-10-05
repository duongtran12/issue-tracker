package com.duong.issue_tracker.repository;

import com.duong.issue_tracker.entity.IssueTimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IssueTimeEntryRepository extends JpaRepository<IssueTimeEntry, Long> {
    List<IssueTimeEntry> findAllByIssueIdOrderByWorkDateDescIdDesc(Long issueId);
    Optional<IssueTimeEntry> findByIdAndIssueId(Long id, Long issueId);
}
