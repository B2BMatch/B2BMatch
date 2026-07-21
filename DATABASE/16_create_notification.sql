/*
====================================================
 Project : b2bmatch
 File    : 16_create_notification.sql
 Author  : Team b2bmatch
====================================================
*/

-- =============================================
-- TABLE: notification
-- Description:
-- Stores notifications for users.
-- =============================================

CREATE TABLE notification (

    id BIGSERIAL,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_notification
        PRIMARY KEY (id),

    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id) REFERENCES app_user(id)

);

COMMENT ON TABLE notification IS 'Stores user notifications';
COMMENT ON COLUMN notification.id IS 'Primary key';
COMMENT ON COLUMN notification.user_id IS 'Foreign key to app_user';
COMMENT ON COLUMN notification.title IS 'Notification title';
COMMENT ON COLUMN notification.message IS 'Notification content';
COMMENT ON COLUMN notification.is_read IS 'Read status (True/False)';
COMMENT ON COLUMN notification.created_at IS 'Creation date';

CREATE INDEX idx_notification_user_id ON notification(user_id);