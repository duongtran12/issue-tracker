package com.duong.issue_tracker.repository;

import com.duong.issue_tracker.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {

    List<Label> findAllByProjectIdOrderByNameAsc(Long projectId);

    List<Label> findAllByIdInAndProjectId(Collection<Long> ids, Long projectId);

    Optional<Label> findByIdAndProjectId(Long id, Long projectId);

    boolean existsByProjectIdAndNameIgnoreCase(Long projectId, String name);
}
