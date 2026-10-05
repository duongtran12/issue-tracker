package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.TimeEntryRequest;
import com.duong.issue_tracker.dto.response.TimeEntryResponse;
import com.duong.issue_tracker.entity.*;
import com.duong.issue_tracker.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueTimeEntryServiceTest {
    @Mock IssueTimeEntryRepository timeEntryRepository;
    @Mock IssueRepository issueRepository;
    @Mock ProjectRepository projectRepository;
    @Mock ProjectMemberRepository projectMemberRepository;
    @Mock UserRepository userRepository;
    @InjectMocks IssueTimeEntryService service;

    @Test
    void create_shouldSaveNormalizedEntryForCurrentUser() {
        Issue issue = accessibleIssue("duong");
        User user = issue.getProject().getOwner();
        user.setId(10L);
        when(userRepository.findByUsername("duong")).thenReturn(Optional.of(user));
        when(timeEntryRepository.save(any(IssueTimeEntry.class))).thenAnswer(invocation -> {
            IssueTimeEntry entry = invocation.getArgument(0);
            entry.setId(7L);
            return entry;
        });

        TimeEntryResponse response = service.create(1L, 5L,
                new TimeEntryRequest(90, LocalDate.of(2026, 10, 5), "  Review   release  "), "duong");

        assertThat(response.minutes()).isEqualTo(90);
        assertThat(response.note()).isEqualTo("Review release");
        assertThat(response.username()).isEqualTo("duong");
    }

    private Issue accessibleIssue(String username) {
        User owner = new User();
        owner.setUsername(username);
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
}
