package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.IssueRequest;
import com.duong.issue_tracker.dto.response.IssueResponse;
import com.duong.issue_tracker.entity.Issue;
import com.duong.issue_tracker.entity.Project;
import com.duong.issue_tracker.entity.User;
import com.duong.issue_tracker.enums.IssuePriority;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.IssueRepository;
import com.duong.issue_tracker.repository.ProjectMemberRepository;
import com.duong.issue_tracker.repository.ProjectRepository;
import com.duong.issue_tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IssueService issueService;

    @Test
    void create_shouldUseDefaultStatusAndPriority() {
        User reporter = user("duong", 10L);
        Project project = project(1L, reporter);
        IssueRequest request = new IssueRequest("Fix login", "Handle expired token", null, null, null);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserUsername(1L, "duong")).thenReturn(false);
        when(userRepository.findByUsername("duong")).thenReturn(Optional.of(reporter));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(5L);
            return issue;
        });

        IssueResponse response = issueService.create(1L, request, "duong");

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.status()).isEqualTo("TODO");
        assertThat(response.priority()).isEqualTo("MEDIUM");
    }

    @Test
    void create_shouldRejectAssigneeOutsideProject() {
        User reporter = user("duong", 10L);
        Project project = project(1L, reporter);
        IssueRequest request = new IssueRequest("Fix login", null, null, IssuePriority.HIGH, "other");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserUsername(1L, "duong")).thenReturn(false);
        when(projectMemberRepository.existsByProjectIdAndUserUsername(1L, "other")).thenReturn(false);
        when(userRepository.findByUsername("duong")).thenReturn(Optional.of(reporter));

        assertThrows(ResourceNotFoundException.class,
                () -> issueService.create(1L, request, "duong"));
    }

    @Test
    void findAll_shouldRejectUserOutsideProject() {
        User owner = user("owner", 1L);
        Project project = project(1L, owner);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserUsername(1L, "intruder")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> issueService.findAll(1L, "intruder"));
    }

            @Test
            void search_shouldTrimTextFiltersAndMapPage() {
            User owner = user("duong", 10L);
            Project project = project(1L, owner);
            Issue issue = new Issue();
            issue.setId(5L);
            issue.setProject(project);
            issue.setReporter(owner);

            Pageable pageable = PageRequest.of(1, 10);
            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProjectIdAndUserUsername(1L, "duong")).thenReturn(false);
            when(issueRepository.search(1L, null, IssuePriority.HIGH, "assignee", "login", pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(issue), pageable, 11));

            var response = issueService.search(
                1L, "duong", null, IssuePriority.HIGH, " assignee ", " login ", pageable);

            assertThat(response.getTotalElements()).isEqualTo(11);
            assertThat(response.getContent()).hasSize(1);
            verify(issueRepository).search(1L, null, IssuePriority.HIGH, "assignee", "login", pageable);
            }

    private Project project(Long id, User owner) {
        Project project = new Project();
        project.setId(id);
        project.setOwner(owner);
        return project;
    }

    private User user(String username, Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setFullName(username);
        return user;
    }
}
