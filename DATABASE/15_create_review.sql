/*
====================================================
 Project : b2bmatch
 File    : 15_create_review.sql
 Author  : Team b2bmatch
====================================================
*/

-- =============================================
-- TABLE: review
-- Description:
-- Stores reviews and ratings provided by customers.
-- =============================================

CREATE TABLE review (

    id BIGSERIAL,
    customer_id BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    
    -- MODIFICACIÓN: CHECK constraint para asegurar que la calificación sea entre 1 y 5
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT pk_review
        PRIMARY KEY (id),

    CONSTRAINT fk_review_customer
        FOREIGN KEY (customer_id) REFERENCES customer_profile(id),

    CONSTRAINT fk_review_professional
        FOREIGN KEY (professional_id) REFERENCES professional_profile(id)

);

COMMENT ON TABLE review IS 'Stores customer reviews and ratings';
COMMENT ON COLUMN review.id IS 'Primary key';
COMMENT ON COLUMN review.customer_id IS 'Foreign key to customer_profile';
COMMENT ON COLUMN review.professional_id IS 'Foreign key to professional_profile';
COMMENT ON COLUMN review.rating IS 'Numerical rating strictly between 1 and 5';
COMMENT ON COLUMN review.comment IS 'Review comment';
COMMENT ON COLUMN review.created_at IS 'Creation date';
COMMENT ON COLUMN review.updated_at IS 'Last update';

CREATE INDEX idx_review_customer_id ON review(customer_id);
CREATE INDEX idx_review_professional_id ON review(professional_id);