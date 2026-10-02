CREATE INDEX idx_issues_project_status_created
    ON issues(project_id, status, created_at DESC);

CREATE INDEX idx_issues_project_priority_created
    ON issues(project_id, priority, created_at DESC);
