--liquibase formatted sql

--changeset author:add_extension
CREATE TABLE IF NOT EXISTS service_extension(
    id bigint CONSTRAINT service_extension_fk REFERENCES service(id) NOT NULL UNIQUE,
    min_value int NOT NULL DEFAULT 0,
    max_value int NOT NULL DEFAULT 1024
);

--changeset author:add_threshold
ALTER TABLE service_extension
    ADD COLUMN IF NOT EXISTS threshold int;
