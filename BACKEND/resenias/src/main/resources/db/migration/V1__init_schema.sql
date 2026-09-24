CREATE TABLE review (
    id BIGSERIAL,
    user_id BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_review PRIMARY KEY (id),
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES usuarios.app_user(id),
    CONSTRAINT fk_review_professional FOREIGN KEY (professional_id) REFERENCES perfiles.professional_profile(id),
    CONSTRAINT uk_review_user_professional UNIQUE (user_id, professional_id)
);

CREATE INDEX IF NOT EXISTS idx_review_user_id ON review(user_id);
CREATE INDEX IF NOT EXISTS idx_review_professional_id ON review(professional_id);