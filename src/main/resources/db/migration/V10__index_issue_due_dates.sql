CREATE INDEX idx_issues_project_due_date
    ON issues(project_id, due_date);
