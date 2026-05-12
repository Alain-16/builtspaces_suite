CREATE EXTENSION IF NOT EXISTS postgis;

-- ORGANIZATION
-- No org_id column — this IS the tenant root, not scoped within one.
CREATE TABLE organization (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    logo_url   VARCHAR(500),
    plan_tier  VARCHAR(20)  NOT NULL DEFAULT 'STARTER',
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- USERS
-- "user" is reserved in PostgreSQL — table is named "users".
-- org_id has no ON DELETE CASCADE — organizations are never deleted, only deactivated.
CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id        UUID         NOT NULL REFERENCES organization(id),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'AGENT',
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_org   ON users(org_id);
CREATE INDEX idx_user_email ON users(email);
