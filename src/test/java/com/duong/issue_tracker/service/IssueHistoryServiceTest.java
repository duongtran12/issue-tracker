package com.duong.issue_tracker.service;

import com.duong.issue_tracker.entity.Issue;
import com.duong.issue_tracker.entity.IssueHistory;
import com.duong.issue_tracker.entity.Project;
import com.duong.issue_tracker.entity.User;
import com.duong.issue_tracker.enums.IssueHistoryEventType;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.IssueHistoryRepository;
import com.duong.issue_tracker.repository.IssueRepository;
import com.duong.issue_tracker.repository.ProjectMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class IssueHistoryServiceTest {

    @Mock
    private IssueHistoryRepository historyRepository;
    @Mock
    private IssueRepository issueRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @InjectMocks
    private IssueHistoryService historyService;

    @Test
    void record_shouldPersistHistoryEvent() {
        Issue issue = new Issue();
        User actor = user("duong", 10L);

        historyService.record(issue, actor, IssueHistoryEventType.STATUS_CHANGED,
                "status", "TODO", "DONE");

        verify(historyRepository).save(any(IssueHistory.class));
    }

    @Test
    void findAll_shouldMapHistoryForProjectOwner() {
        User owner = user("duong", 10L);
        Project project = new Project();
        project.setId(1L);
        project.setOwner(owner);
        Issue issue = new Issue();
        issue.setId(5L);
        issue.setProject(project);
        IssueHistory history = new IssueHistory();
        history.setId(7L);
        history.setIssue(issue);
        history.setActor(owner);
        history.setEventType(IssueHistoryEventType.CREATED);
        when(issueRepository.findByIdAndProjectId(5L, 1L)).thenReturn(Optional.of(issue));
        when(historyRepository.findAllByIssueIdOrderByCreatedAtAsc(5L))
                .thenReturn(List.of(history));

        var response = historyService.findAll(1L, 5L, "duong");

        assertThat(response).singleElement()
                .satisfies(item -> assertThat(item.eventType()).isEqualTo("CREATED"));
    }

    @Test
    void findAll_shouldRejectUserOutsideProject() {
        User owner = user("owner", 10L);
        Project project = new Project();
        project.setId(1L);
        project.setOwner(owner);
        Issue issue = new Issue();
        issue.setId(5L);
        issue.setProject(project);
        when(issueRepository.findByIdAndProjectId(5L, 1L)).thenReturn(Optional.of(issue));
        when(projectMemberRepository.existsByProjectIdAndUserUsername(1L, "intruder"))
                .thenReturn(false);

        assertThatThrownBy(() -> historyService.findAll(1L, 5L, "intruder"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_shouldRepresentMissingActorAsSystem() {
        User owner = user("duong", 10L);
        Project project = new Project();
        project.setId(1L);
        project.setOwner(owner);
        Issue issue = new Issue();
        issue.setId(5L);
        issue.setProject(project);
        IssueHistory history = new IssueHistory();
        history.setId(7L);
        history.setIssue(issue);
        history.setEventType(IssueHistoryEventType.UPDATED);
        when(issueRepository.findByIdAndProjectId(5L, 1L)).thenReturn(Optional.of(issue));
        when(historyRepository.findAllByIssueIdOrderByCreatedAtAsc(5L))
                .thenReturn(List.of(history));

        var response = historyService.findAll(1L, 5L, "duong");

        assertThat(response.getFirst().actorUsername()).isNull();
        assertThat(response.getFirst().actorId()).isNull();
    }

    private User user(String username, Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
