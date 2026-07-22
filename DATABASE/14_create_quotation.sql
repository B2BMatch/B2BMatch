/*
====================================================
 Project : b2bmatch
 File    : 14_create_quotation.sql
 Author  : Team b2bmatch
====================================================
*/

-- =============================================
-- TABLE: quotation
-- Description:
-- Stores the quotations sent by professionals for services.
-- =============================================

CREATE TABLE quotation (

    id BIGSERIAL,
    service_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    message TEXT,
    
    -- MODIFICACIÓN: CHECK constraint para asegurar que solo ingresen estados válidos
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'PENDING', 'SUSPENDED', 'INACTIVE', 'DELETED')),
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT pk_quotation
        PRIMARY KEY (id),

    CONSTRAINT fk_quotation_service
        FOREIGN KEY (service_id) REFERENCES service(id),

    CONSTRAINT fk_quotation_customer
        FOREIGN KEY (customer_id) REFERENCES customer_profile(id)

);

COMMENT ON TABLE quotation IS 'Stores service quotations';
COMMENT ON COLUMN quotation.id IS 'Primary key';
COMMENT ON COLUMN quotation.service_id IS 'Foreign key to service';
COMMENT ON COLUMN quotation.customer_id IS 'Foreign key to customer_profile';
COMMENT ON COLUMN quotation.message IS 'Proposal message';
COMMENT ON COLUMN quotation.status IS 'Quotation status with strict CHECK constraint';
COMMENT ON COLUMN quotation.created_at IS 'Creation date';
COMMENT ON COLUMN quotation.updated_at IS 'Last update';

CREATE INDEX idx_quotation_service_id ON quotation(service_id);
CREATE INDEX idx_quotation_customer_id ON quotation(customer_id);