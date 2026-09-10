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
        String normalizedName = normalizeText(request.name());
        String normalizedKey = normalizeText(request.key());
        String normalizedDescription = normalizeText(request.description());

        if (projectRepository.existsByKey(normalizedKey)) {
            throw new DuplicateResourceException("Project key already exists: " + normalizedKey);
        }

        User owner = findUser(ownerUsername);
        Project project = new Project();
        project.setName(normalizedName);
        project.setKey(normalizedKey);
        project.setDescription(normalizedDescription);
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
        String normalizedName = normalizeText(request.name());
        String normalizedKey = normalizeText(request.key());
        String normalizedDescription = normalizeText(request.description());

        if (!project.getKey().equals(normalizedKey) && projectRepository.existsByKey(normalizedKey)) {
            throw new DuplicateResourceException("Project key already exists: " + normalizedKey);
        }

        project.setName(normalizedName);
        project.setKey(normalizedKey);
        project.setDescription(normalizedDescription);
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void delete(Long id, String ownerUsername) {
        projectRepository.delete(findOwnedProject(id, ownerUsername));
    }

    @Transactional
    public ProjectMemberResponse addMember(Long projectId, String username, String ownerUsername) {
        Project project = findOwnedProject(projectId, ownerUsername);
        String normalizedUsername = username.trim();
        if (projectMemberRepository.existsByProjectIdAndUserUsername(projectId, normalizedUsername)) {
            throw new DuplicateResourceException("User is already a project member: " + normalizedUsername);
        }

        User user = findUser(normalizedUsername);
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
        String normalizedUsername = username.trim();
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserUsername(projectId, normalizedUsername)
            .orElseThrow(() -> new ResourceNotFoundException("Project member not found: " + normalizedUsername));
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
        String normalizedUsername = normalizeText(username);
        return userRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + normalizedUsername));
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
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
