package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.ProjectRequest;
import com.duong.issue_tracker.dto.response.ProjectResponse;
import com.duong.issue_tracker.entity.Project;
import com.duong.issue_tracker.entity.User;
import com.duong.issue_tracker.exception.DuplicateResourceException;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.ProjectRepository;
import com.duong.issue_tracker.repository.ProjectMemberRepository;
import com.duong.issue_tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void create_shouldCreateProjectForAuthenticatedOwner() {
        User owner = user("duong");
        ProjectRequest request = new ProjectRequest("Issue Tracker", "ISSUE", "Project management");

        when(projectRepository.existsByKey("ISSUE")).thenReturn(false);
        when(userRepository.findByUsername("duong")).thenReturn(Optional.of(owner));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(1L);
            return project;
        });

        ProjectResponse response = projectService.create(request, "duong");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.key()).isEqualTo("ISSUE");
        assertThat(response.ownerUsername()).isEqualTo("duong");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void create_shouldRejectDuplicateProjectKey() {
        ProjectRequest request = new ProjectRequest("Issue Tracker", "ISSUE", null);
        when(projectRepository.existsByKey("ISSUE")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> projectService.create(request, "duong"));
    }

    @Test
    void findById_shouldRejectProjectOwnedByAnotherUser() {
        when(projectRepository.findByIdAndOwnerUsername(1L, "duong"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.findById(1L, "duong"));
    }

    private User user(String username) {
        User user = new User();
        user.setId(10L);
        user.setUsername(username);
        return user;
    }
}
