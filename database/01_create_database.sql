/*
=========================================================
 Project : ExpertConnect
 Database: PostgreSQL
 Version : 1.0
 Author  : Team ExpertConnect
=========================================================
*/

-- ============================================
-- Create Database
-- ============================================

CREATE DATABASE b2bmatch
WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'C'
    LC_CTYPE = 'C'
    TEMPLATE = template0;

-- ============================================
-- Connect to Database
-- (Execute this manually in pgAdmin or psql)
-- ============================================

-- \c b2bmatch;

-- ============================================
-- Recommended Schema
-- ============================================

CREATE SCHEMA IF NOT EXISTS b2bmatch;

SET search_path TO b2bmatch;

-- ============================================
-- Extensions
-- ============================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================
-- Verify
-- ============================================

-- SELECT current_database();

-- SELECT current_schema();

-- SELECT version();