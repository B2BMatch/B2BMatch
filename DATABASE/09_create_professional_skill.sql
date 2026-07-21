-- ================================================
-- Tabla: professional_skill
-- Descripción: Relación entre profesionales y sus habilidades. Permite asociar múltiples habilidades a un profesional y compartir una habilidad entre varios profesionales.
-- Utilizada por: professional_profile / skill
-- ================================================

CREATE TABLE professional_skill (

    professional_id BIGINT not null,
    skill_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_professional_skill
        PRIMARY KEY (professional_id, skill_id),

    CONSTRAINT fk_professional_skill_professional
        FOREIGN KEY (professional_id)
        REFERENCES professional_profile(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_professional_skill_skill
        FOREIGN KEY (skill_id)
        REFERENCES skill(id)
        ON DELETE CASCADE

);

COMMENT ON TABLE professional_skill IS 'Relationship between professionals and their skills';

COMMENT ON COLUMN professional_skill.professional_id IS 'Professional identifier';

COMMENT ON COLUMN professional_skill.skill_id IS 'Skill identifier';

COMMENT ON COLUMN professional_skill.created_at IS 'Relationship creation date';

CREATE INDEX idx_professional_skill_professional
ON professional_skill(professional_id);

CREATE INDEX idx_professional_skill_skill
ON professional_skill(skill_id);

/* insert into professional_skill values */