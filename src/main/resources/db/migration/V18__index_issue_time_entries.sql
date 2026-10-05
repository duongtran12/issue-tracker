CREATE INDEX idx_time_entries_issue_date
    ON issue_time_entries(issue_id, work_date, id);
CREATE INDEX idx_time_entries_user
    ON issue_time_entries(user_id);
