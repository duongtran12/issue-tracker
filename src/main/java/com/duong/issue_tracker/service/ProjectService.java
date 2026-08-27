package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.ProjectRequest;
import com.duong.issue_tracker.dto.response.ProjectMemberResponse;
import com.duong.issue_tracker.dto.response.ProjectResponse;
import com.duong.issue_tracker.entity.Project;
import com.duong.issue_tracker.entity.ProjectMember;
import com.duong.issue_tracker.entity.User;
import com.duong.issue_tracker.enums.ProjectMemberRole;
import com.duong.issue_tracker.exception.DuplicateResourceException;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.ProjectRepository;
import com.duong.issue_tracker.repository.ProjectMemberRepository;
import com.duong.issue_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse create(ProjectRequest request, String ownerUsername) {
        if (projectRepository.existsByKey(request.key())) {
            throw new DuplicateResourceException("Project key already exists: " + request.key());
        }

        User owner = findUser(ownerUsername);
        Project project = new Project();
        project.setName(request.name());
        project.setKey(request.key());
        project.setDescription(request.description());
        project.setOwner(owner);
        Project savedProject = projectRepository.save(project);
        addMembership(savedProject, owner, ProjectMemberRole.OWNER);
        return toResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> findMine(String ownerUsername) {
        return projectRepository.findAllByOwnerUsernameOrderByCreatedAtDesc(ownerUsername)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(Long id, String ownerUsername) {
        return toResponse(findOwnedProject(id, ownerUsername));
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request, String ownerUsername) {
        Project project = findOwnedProject(id, ownerUsername);
        if (!project.getKey().equals(request.key()) && projectRepository.existsByKey(request.key())) {
            throw new DuplicateResourceException("Project key already exists: " + request.key());
        }

        project.setName(request.name());
        project.setKey(request.key());
        project.setDescription(request.description());
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void delete(Long id, String ownerUsername) {
        projectRepository.delete(findOwnedProject(id, ownerUsername));
    }

    @Transactional
    public ProjectMemberResponse addMember(Long projectId, String username, String ownerUsername) {
        Project project = findOwnedProject(projectId, ownerUsername);
        if (projectMemberRepository.existsByProjectIdAndUserUsername(projectId, username)) {
            throw new DuplicateResourceException("User is already a project member: " + username);
        }

        User user = findUser(username);
        return toMemberResponse(addMembership(project, user, ProjectMemberRole.MEMBER));
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> findMembers(Long projectId, String ownerUsername) {
        findOwnedProject(projectId, ownerUsername);
        return projectMemberRepository.findAllByProjectIdOrderByIdAsc(projectId)
                .stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public void removeMember(Long projectId, String username, String ownerUsername) {
        findOwnedProject(projectId, ownerUsername);
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserUsername(projectId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Project member not found: " + username));
        if (member.getRole() == ProjectMemberRole.OWNER) {
            throw new IllegalArgumentException("Project owner cannot be removed");
        }
        projectMemberRepository.delete(member);
    }

    private Project findOwnedProject(Long id, String ownerUsername) {
        return projectRepository.findByIdAndOwnerUsername(id, ownerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private ProjectMember addMembership(Project project, User user, ProjectMemberRole role) {
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(role);
        return projectMemberRepository.save(member);
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getKey(),
                project.getDescription(),
                project.getOwner().getId(),
                project.getOwner().getUsername(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private ProjectMemberResponse toMemberResponse(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getUser().getId(),
                member.getUser().getUsername(),
                member.getUser().getFullName(),
                member.getRole().name()
        );
    }
}
