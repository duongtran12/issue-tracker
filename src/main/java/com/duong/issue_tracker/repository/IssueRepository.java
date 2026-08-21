package com.duong.issue_tracker.repository;

import com.duong.issue_tracker.entity.Issue;
import com.duong.issue_tracker.enums.IssuePriority;
import com.duong.issue_tracker.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findAllByProjectIdOrderByCreatedAtDesc(Long projectId);

        @Query(value = """
                        select i
                        from Issue i
                        left join i.assignee a
                        where i.project.id = :projectId
                            and (:status is null or i.status = :status)
                            and (:priority is null or i.priority = :priority)
                            and (:assigneeUsername is null or a.username = :assigneeUsername)
                            and (
                                    :keyword is null
                                    or lower(i.title) like lower(concat('%', :keyword, '%'))
                                    or lower(coalesce(i.description, '')) like lower(concat('%', :keyword, '%'))
                            )
                        """,
                        countQuery = """
                                        select count(i)
                                        from Issue i
                                        left join i.assignee a
                                        where i.project.id = :projectId
                                            and (:status is null or i.status = :status)
                                            and (:priority is null or i.priority = :priority)
                                            and (:assigneeUsername is null or a.username = :assigneeUsername)
                                            and (
                                                    :keyword is null
                                                    or lower(i.title) like lower(concat('%', :keyword, '%'))
                                                    or lower(coalesce(i.description, '')) like lower(concat('%', :keyword, '%'))
                                            )
                                        """)
        Page<Issue> search(
                        @Param("projectId") Long projectId,
                        @Param("status") IssueStatus status,
                        @Param("priority") IssuePriority priority,
                        @Param("assigneeUsername") String assigneeUsername,
                        @Param("keyword") String keyword,
                        Pageable pageable);

    Optional<Issue> findByIdAndProjectId(Long id, Long projectId);
}
