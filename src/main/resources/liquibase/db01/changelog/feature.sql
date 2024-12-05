--liquibase formatted sql

--changeset author:record
CREATE TABLE IF NOT EXISTS record(
    id bigserial constraint record_pk primary key,
    name varchar(16) NOT NULL UNIQUE,
    description varchar(512)
);
