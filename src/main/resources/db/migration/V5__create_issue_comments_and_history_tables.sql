CREATE TABLE issue_comments (
    id SERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    body VARCHAR(5000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_issue_comments_issue FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE,
    CONSTRAINT fk_issue_comments_author FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE issue_history (
    id SERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL,
    actor_id BIGINT,
    event_type VARCHAR(30) NOT NULL,
    field_name VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_issue_history_issue FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE,
    CONSTRAINT fk_issue_history_actor FOREIGN KEY (actor_id) REFERENCES users(id)
);

CREATE INDEX idx_issue_comments_issue_created ON issue_comments(issue_id, created_at);
CREATE INDEX idx_issue_history_issue_created ON issue_history(issue_id, created_at);
