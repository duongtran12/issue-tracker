package com.duong.issue_tracker.service;

import com.duong.issue_tracker.entity.Issue;
import com.duong.issue_tracker.entity.IssueHistory;
import com.duong.issue_tracker.entity.Project;
import com.duong.issue_tracker.entity.User;
import com.duong.issue_tracker.enums.IssueHistoryEventType;
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

    private User user(String username, Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
