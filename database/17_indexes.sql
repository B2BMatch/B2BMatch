/*
====================================================
 Project : b2bmatch
 File    : 17_indexes.sql
 Author  : Team b2bmatch
====================================================
*/

-- =============================================
-- ADDITIONAL INDEXES
-- Description:
-- Performance optimization for common queries.
-- =============================================

-- Índice para optimizar búsquedas por estado (usado en muchas tablas)
CREATE INDEX idx_job_offer_status ON job_offer(status);
CREATE INDEX idx_quotation_status ON quotation(status);

-- Índice para búsquedas por fecha de creación (ordenar por lo más reciente)
CREATE INDEX idx_app_user_created_at ON app_user(created_at);