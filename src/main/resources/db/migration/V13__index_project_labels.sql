CREATE INDEX idx_labels_project_name ON labels(project_id, name);
CREATE INDEX idx_issue_labels_label ON issue_labels(label_id);
