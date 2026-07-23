/*
=========================================================
 Project : B2BMatch
 Microservice : catalogo
 Database : PostgreSQL
=========================================================
*/

CREATE SCHEMA IF NOT EXISTS catalogo;

SET search_path TO catalog, public;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";