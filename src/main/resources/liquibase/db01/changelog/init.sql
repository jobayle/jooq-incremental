--liquibase formatted sql

--changeset author:initial
CREATE TABLE IF NOT EXISTS domain(
    id bigserial CONSTRAINT domain_pk PRIMARY KEY,
    name varchar(16) NOT NULL UNIQUE,
    description varchar(512)
);
