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

import java.util.List;
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
    void create_shouldTrimProjectNameAndKeyBeforePersist() {
        User owner = user("duong");
        ProjectRequest request = new ProjectRequest("  Issue Tracker  ", "  ISSUE  ", "  Project management  ");

        when(projectRepository.existsByKey("ISSUE")).thenReturn(false);
        when(userRepository.findByUsername("duong")).thenReturn(Optional.of(owner));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(1L);
            return project;
        });

        ProjectResponse response = projectService.create(request, "duong");

        assertThat(response.name()).isEqualTo("Issue Tracker");
        assertThat(response.key()).isEqualTo("ISSUE");
        assertThat(response.description()).isEqualTo("Project management");
    }

    @Test
    void findById_shouldRejectProjectOwnedByAnotherUser() {
        Project project = new Project();
        project.setOwner(user("alice"));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserUsername(1L, "duong")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.findById(1L, "duong"));
    }

    @Test
    void findById_shouldAllowProjectMember() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Shared");
        project.setKey("SHARED");
        project.setOwner(user("alice"));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserUsername(1L, "duong")).thenReturn(true);

        assertThat(projectService.findById(1L, "duong").id()).isEqualTo(1L);
    }

    @Test
    void findMine_shouldReturnProjectsSharedWithMember() {
        User owner = user("alice");
        Project sharedProject = new Project();
        sharedProject.setId(3L);
        sharedProject.setName("Shared project");
        sharedProject.setKey("SHARED");
        sharedProject.setOwner(owner);
        when(projectRepository.findAllAccessibleByUsername("duong"))
                .thenReturn(List.of(sharedProject));

        List<ProjectResponse> response = projectService.findMine(" Duong ");

        assertThat(response).extracting(ProjectResponse::id).containsExactly(3L);
    }

        @Test
        void addMember_shouldNormalizeUsernameBeforeLookup() {
        User owner = user("duong");
        User member = user("alice");
        Project project = new Project();
        project.setId(1L);
        project.setOwner(owner);

        when(projectRepository.findByIdAndOwnerUsername(1L, "duong")).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserUsername(1L, "alice")).thenReturn(false);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(member));
        when(projectMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = projectService.addMember(1L, "  alice  ", "duong");

        assertThat(response.username()).isEqualTo("alice");
        verify(userRepository).findByUsername("alice");
        }

    private User user(String username) {
        User user = new User();
        user.setId(10L);
        user.setUsername(username);
        return user;
    }
}
