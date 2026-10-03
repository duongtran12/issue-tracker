package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.ChecklistItemRequest;
import com.duong.issue_tracker.dto.response.ChecklistItemResponse;
import com.duong.issue_tracker.entity.Issue;
import com.duong.issue_tracker.entity.IssueChecklistItem;
import com.duong.issue_tracker.entity.Project;
import com.duong.issue_tracker.entity.User;
import com.duong.issue_tracker.repository.IssueChecklistItemRepository;
import com.duong.issue_tracker.repository.IssueRepository;
import com.duong.issue_tracker.repository.ProjectMemberRepository;
import com.duong.issue_tracker.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueChecklistServiceTest {

    @Mock IssueChecklistItemRepository checklistRepository;
    @Mock IssueRepository issueRepository;
    @Mock ProjectRepository projectRepository;
    @Mock ProjectMemberRepository projectMemberRepository;
    @InjectMocks IssueChecklistService checklistService;

    @Test
    void create_shouldAppendAndNormalizeChecklistItem() {
        Issue issue = accessibleIssue();
        when(checklistRepository.countByIssueId(5L)).thenReturn(2L);
        when(checklistRepository.save(any(IssueChecklistItem.class))).thenAnswer(invocation -> {
            IssueChecklistItem item = invocation.getArgument(0);
            item.setId(9L);
            return item;
        });

        ChecklistItemResponse response = checklistService.create(
                1L, 5L, new ChecklistItemRequest("  Verify   deployment  ", null, null), "duong");

        assertThat(response.content()).isEqualTo("Verify deployment");
        assertThat(response.position()).isEqualTo(2);
        assertThat(response.completed()).isFalse();
        assertThat(response.issueId()).isEqualTo(issue.getId());
    }

    @Test
    void findAll_shouldKeepChecklistOrder() {
        Issue issue = accessibleIssue();
        IssueChecklistItem first = item(1L, issue, "First", 0);
        IssueChecklistItem second = item(2L, issue, "Second", 1);
        when(checklistRepository.findAllByIssueIdOrderByPositionAscIdAsc(5L))
                .thenReturn(List.of(first, second));

        List<ChecklistItemResponse> response = checklistService.findAll(1L, 5L, "duong");

        assertThat(response).extracting(ChecklistItemResponse::content)
                .containsExactly("First", "Second");
    }

    private Issue accessibleIssue() {
        User owner = new User();
        owner.setUsername("duong");
        Project project = new Project();
        project.setId(1L);
        project.setOwner(owner);
        Issue issue = new Issue();
        issue.setId(5L);
        issue.setProject(project);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(issueRepository.findByIdAndProjectId(5L, 1L)).thenReturn(Optional.of(issue));
        return issue;
    }

    private IssueChecklistItem item(Long id, Issue issue, String content, int position) {
        IssueChecklistItem item = new IssueChecklistItem();
        item.setId(id);
        item.setIssue(issue);
        item.setContent(content);
        item.setPosition(position);
        return item;
    }
}
