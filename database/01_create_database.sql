/*
=========================================================
 Project : ExpertConnect
 Database: PostgreSQL
 Version : 1.0
 Author  : Team ExpertConnect
=========================================================
*/

-- ============================================
-- Recommended Schema
-- ============================================

CREATE SCHEMA IF NOT EXISTS b2bmatch;

-- Establecemos el esquema por defecto para las siguientes tablas
SET search_path TO b2bmatch, public;

-- ============================================
-- Extensions
-- ============================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";