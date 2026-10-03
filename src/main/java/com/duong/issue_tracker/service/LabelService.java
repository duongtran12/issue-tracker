package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.LabelRequest;
import com.duong.issue_tracker.dto.response.LabelResponse;
import com.duong.issue_tracker.entity.Label;
import com.duong.issue_tracker.entity.Project;
import com.duong.issue_tracker.exception.DuplicateResourceException;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.LabelRepository;
import com.duong.issue_tracker.repository.ProjectMemberRepository;
import com.duong.issue_tracker.repository.ProjectRepository;
import com.duong.issue_tracker.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional(readOnly = true)
    public List<LabelResponse> findAll(Long projectId, String username) {
        findAccessibleProject(projectId, username);
        return labelRepository.findAllByProjectIdOrderByNameAsc(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LabelResponse create(Long projectId, LabelRequest request, String username) {
        Project project = findAccessibleProject(projectId, username);
        String name = TextNormalizer.compact(request.name());
        if (labelRepository.existsByProjectIdAndNameIgnoreCase(projectId, name)) {
            throw new DuplicateResourceException("Label already exists: " + name);
        }
        Label label = new Label();
        label.setProject(project);
        label.setName(name);
        label.setColor(request.color().toUpperCase());
        return toResponse(labelRepository.save(label));
    }

    @Transactional
    public LabelResponse update(Long projectId, Long labelId, LabelRequest request, String username) {
        findAccessibleProject(projectId, username);
        Label label = findLabel(projectId, labelId);
        String name = TextNormalizer.compact(request.name());
        if (!label.getName().equalsIgnoreCase(name)
                && labelRepository.existsByProjectIdAndNameIgnoreCase(projectId, name)) {
            throw new DuplicateResourceException("Label already exists: " + name);
        }
        label.setName(name);
        label.setColor(request.color().toUpperCase());
        return toResponse(labelRepository.save(label));
    }

    @Transactional
    public void delete(Long projectId, Long labelId, String username) {
        findAccessibleProject(projectId, username);
        labelRepository.delete(findLabel(projectId, labelId));
    }

    private Project findAccessibleProject(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        boolean owner = project.getOwner().getUsername().equals(username);
        boolean member = projectMemberRepository.existsByProjectIdAndUserUsername(projectId, username);
        if (!owner && !member) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }
        return project;
    }

    private Label findLabel(Long projectId, Long labelId) {
        return labelRepository.findByIdAndProjectId(labelId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + labelId));
    }

    private LabelResponse toResponse(Label label) {
        return new LabelResponse(label.getId(), label.getProject().getId(), label.getName(), label.getColor());
    }
}
