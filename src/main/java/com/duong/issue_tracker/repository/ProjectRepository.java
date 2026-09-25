package com.duong.issue_tracker.repository;

import com.duong.issue_tracker.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByKey(String key);

    List<Project> findAllByOwnerUsernameOrderByCreatedAtDesc(String username);

    @Query("""
            select p from Project p
            where exists (
                select pm.id from ProjectMember pm
                where pm.project = p and pm.user.username = :username
            )
            order by p.createdAt desc
            """)
    List<Project> findAllAccessibleByUsername(@Param("username") String username);

    Optional<Project> findByIdAndOwnerUsername(Long id, String username);
}
