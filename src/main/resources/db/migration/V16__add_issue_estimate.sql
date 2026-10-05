ALTER TABLE issues ADD COLUMN estimate_minutes INTEGER;
ALTER TABLE issues ADD CONSTRAINT chk_issue_estimate_positive
    CHECK (estimate_minutes IS NULL OR estimate_minutes > 0);
