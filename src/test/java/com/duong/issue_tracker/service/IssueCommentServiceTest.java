package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.CommentRequest;
import com.duong.issue_tracker.dto.response.CommentResponse;
import com.duong.issue_tracker.entity.Issue;
import com.duong.issue_tracker.entity.IssueComment;
import com.duong.issue_tracker.entity.Project;
import com.duong.issue_tracker.entity.User;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.IssueCommentRepository;
import com.duong.issue_tracker.repository.IssueRepository;
import com.duong.issue_tracker.repository.ProjectMemberRepository;
import com.duong.issue_tracker.repository.ProjectRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueCommentServiceTest {

    @Mock
    private IssueCommentRepository commentRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IssueCommentService commentService;

    @Test
    void create_shouldSaveCommentForProjectMember() {
        User author = user("duong", 10L);
        Issue issue = issue(5L, author);
        CommentRequest request = new CommentRequest("I will investigate this.");

        when(issueRepository.findByIdAndProjectId(5L, 1L)).thenReturn(Optional.of(issue));
        when(projectMemberRepository.existsByProjectIdAndUserUsername(1L, "duong")).thenReturn(false);
        when(userRepository.findByUsername("duong")).thenReturn(Optional.of(author));
        when(commentRepository.save(any(IssueComment.class))).thenAnswer(invocation -> {
            IssueComment comment = invocation.getArgument(0);
            comment.setId(7L);
            return comment;
        });

        CommentResponse response = commentService.create(1L, 5L, request, "duong");

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.body()).isEqualTo(request.body());
        assertThat(response.authorUsername()).isEqualTo("duong");
    }

    @Test
    void create_shouldRejectUserOutsideProject() {
        User owner = user("owner", 1L);
        Issue issue = issue(5L, owner);
        when(issueRepository.findByIdAndProjectId(5L, 1L)).thenReturn(Optional.of(issue));
        when(projectMemberRepository.existsByProjectIdAndUserUsername(1L, "intruder")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.create(1L, 5L, new CommentRequest("Nope"), "intruder"));
    }

    private Issue issue(Long id, User owner) {
        Project project = new Project();
        project.setId(1L);
        project.setOwner(owner);
        Issue issue = new Issue();
        issue.setId(id);
        issue.setProject(project);
        return issue;
    }

    private User user(String username, Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setFullName(username);
        return user;
    }
}
