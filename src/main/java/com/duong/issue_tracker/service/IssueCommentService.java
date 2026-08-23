package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.CommentRequest;
import com.duong.issue_tracker.dto.response.CommentResponse;
import com.duong.issue_tracker.entity.Issue;
import com.duong.issue_tracker.entity.IssueComment;
import com.duong.issue_tracker.entity.User;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.IssueCommentRepository;
import com.duong.issue_tracker.repository.IssueRepository;
import com.duong.issue_tracker.repository.ProjectMemberRepository;
import com.duong.issue_tracker.repository.ProjectRepository;
import com.duong.issue_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueCommentService {

    private final IssueCommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse create(Long projectId, Long issueId, CommentRequest request, String username) {
        Issue issue = findAccessibleIssue(projectId, issueId, username);
        User author = findUser(username);
        IssueComment comment = new IssueComment();
        comment.setIssue(issue);
        comment.setAuthor(author);
        comment.setBody(request.body());
        return toResponse(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findAll(Long projectId, Long issueId, String username) {
        findAccessibleIssue(projectId, issueId, username);
        return commentRepository.findAllByIssueIdOrderByCreatedAtAsc(issueId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse update(Long projectId, Long issueId, Long commentId,
                                  CommentRequest request, String username) {
        findAccessibleIssue(projectId, issueId, username);
        IssueComment comment = commentRepository.findById(commentId)
                .filter(item -> item.getIssue().getId().equals(issueId))
                .filter(item -> item.getAuthor().getUsername().equals(username))
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        comment.setBody(request.body());
        return toResponse(commentRepository.save(comment));
    }

    @Transactional
    public void delete(Long projectId, Long issueId, Long commentId, String username) {
        findAccessibleIssue(projectId, issueId, username);
        IssueComment comment = commentRepository.findById(commentId)
                .filter(item -> item.getIssue().getId().equals(issueId))
                .filter(item -> item.getAuthor().getUsername().equals(username))
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        commentRepository.delete(comment);
    }

    private Issue findAccessibleIssue(Long projectId, Long issueId, String username) {
        Issue issue = issueRepository.findByIdAndProjectId(issueId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));
        boolean owner = issue.getProject().getOwner().getUsername().equals(username);
        boolean member = projectMemberRepository.existsByProjectIdAndUserUsername(projectId, username);
        if (!owner && !member) {
            throw new ResourceNotFoundException("Issue not found: " + issueId);
        }
        return issue;
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private CommentResponse toResponse(IssueComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getIssue().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getUsername(),
                comment.getBody(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
