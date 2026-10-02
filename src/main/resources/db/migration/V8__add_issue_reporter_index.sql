CREATE INDEX idx_issues_reporter_created
    ON issues(reporter_id, created_at DESC);
