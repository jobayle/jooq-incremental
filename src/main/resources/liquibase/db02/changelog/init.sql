--liquibase formatted sql

--changeset author:initial
CREATE TABLE IF NOT EXISTS service(
    id bigserial CONSTRAINT service_pk PRIMARY KEY,
    name varchar(16) NOT NULL UNIQUE,
    description varchar(512)
);
