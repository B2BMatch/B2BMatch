CREATE TABLE notification (
    id BIGSERIAL,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_notification PRIMARY KEY (id),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES usuarios.app_user(id)
);

CREATE INDEX IF NOT EXISTS idx_notification_user_id ON notification(user_id);