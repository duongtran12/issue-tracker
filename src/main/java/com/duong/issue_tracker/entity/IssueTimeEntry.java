package com.duong.issue_tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "issue_time_entries")
@Getter
@Setter
@NoArgsConstructor
public class IssueTimeEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private int minutes;
    @Column(nullable = false)
    private LocalDate workDate;
    @Column(length = 1000)
    private String note;
    @CreationTimestamp @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @UpdateTimestamp @Column(nullable = false)
    private Instant updatedAt;
}
