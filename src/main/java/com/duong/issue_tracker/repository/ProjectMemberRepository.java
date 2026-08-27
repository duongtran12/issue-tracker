package com.duong.issue_tracker.repository;

import com.duong.issue_tracker.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    boolean existsByProjectIdAndUserUsername(Long projectId, String username);

    List<ProjectMember> findAllByProjectIdOrderByIdAsc(Long projectId);

    Optional<ProjectMember> findByProjectIdAndUserUsername(Long projectId, String username);
}
