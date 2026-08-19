package com.duong.issue_tracker.repository;

import com.duong.issue_tracker.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByKey(String key);

    List<Project> findAllByOwnerUsernameOrderByCreatedAtDesc(String username);

    Optional<Project> findByIdAndOwnerUsername(Long id, String username);
}
