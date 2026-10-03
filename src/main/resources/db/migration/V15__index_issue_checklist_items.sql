CREATE INDEX idx_checklist_items_issue_position
    ON issue_checklist_items(issue_id, position, id);
