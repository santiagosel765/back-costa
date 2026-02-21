-- Baseline schema for Ferrisys Service

-- User status catalog
CREATE TABLE IF NOT EXISTS user_status (
    status_id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- Authentication tables
CREATE TABLE IF NOT EXISTS auth_role (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    status INTEGER NOT NULL DEFAULT 1
);
CREATE INDEX IF NOT EXISTS idx_auth_role_status ON auth_role (status);

CREATE TABLE IF NOT EXISTS auth_module (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    status INTEGER NOT NULL DEFAULT 1
);
CREATE INDEX IF NOT EXISTS idx_auth_module_status ON auth_module (status);

CREATE TABLE IF NOT EXISTS auth_user (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    status_id UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_auth_user_status FOREIGN KEY (status_id)
        REFERENCES user_status (status_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_auth_user_status_id ON auth_user (status_id);

CREATE TABLE IF NOT EXISTS auth_user_role (
    id UUID PRIMARY KEY,
    auth_user_id UUID NOT NULL,
    auth_role_id UUID NOT NULL,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_auth_user_role_user FOREIGN KEY (auth_user_id)
        REFERENCES auth_user (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_auth_user_role_role FOREIGN KEY (auth_role_id)
        REFERENCES auth_role (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT uq_auth_user_role UNIQUE (auth_user_id, auth_role_id)
);
CREATE INDEX IF NOT EXISTS idx_auth_user_role_status ON auth_user_role (status);

CREATE TABLE IF NOT EXISTS auth_role_module (
    id UUID PRIMARY KEY,
    auth_role_id UUID NOT NULL,
    auth_module_id UUID NOT NULL,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_auth_role_module_role FOREIGN KEY (auth_role_id)
        REFERENCES auth_role (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_auth_role_module_module FOREIGN KEY (auth_module_id)
        REFERENCES auth_module (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT uq_auth_role_module UNIQUE (auth_role_id, auth_module_id)
);
CREATE INDEX IF NOT EXISTS idx_auth_role_module_status ON auth_role_module (status);

-- Inventory tables
CREATE TABLE IF NOT EXISTS inv_category (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    parent_category_id UUID,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_inv_category_parent FOREIGN KEY (parent_category_id)
        REFERENCES inv_category (id)
        ON UPDATE CASCADE ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_inv_category_status ON inv_category (status);

CREATE TABLE IF NOT EXISTS inv_product (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    category_id UUID NOT NULL,
    company_id UUID,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_inv_product_category FOREIGN KEY (category_id)
        REFERENCES inv_category (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_inv_product_status ON inv_product (status);
CREATE INDEX IF NOT EXISTS idx_inv_product_category ON inv_product (category_id);

-- Business catalogs
CREATE TABLE IF NOT EXISTS bus_client (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(100),
    address TEXT,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_bus_client_status ON bus_client (status);

CREATE TABLE IF NOT EXISTS bus_provider (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact VARCHAR(255),
    phone VARCHAR(100),
    address TEXT,
    ruc VARCHAR(100),
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_bus_provider_status ON bus_provider (status);

-- Business documents
CREATE TABLE IF NOT EXISTS bus_quote (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    description TEXT,
    quote_date DATE,
    total NUMERIC(19, 2),
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_bus_quote_client FOREIGN KEY (client_id)
        REFERENCES bus_client (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_bus_quote_status ON bus_quote (status);
CREATE INDEX IF NOT EXISTS idx_bus_quote_client ON bus_quote (client_id);

CREATE TABLE IF NOT EXISTS bus_quote_detail (
    id UUID PRIMARY KEY,
    quote_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_bus_quote_detail_quote FOREIGN KEY (quote_id)
        REFERENCES bus_quote (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_bus_quote_detail_product FOREIGN KEY (product_id)
        REFERENCES inv_product (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_bus_quote_detail_quote ON bus_quote_detail (quote_id);
CREATE INDEX IF NOT EXISTS idx_bus_quote_detail_product ON bus_quote_detail (product_id);

CREATE TABLE IF NOT EXISTS bus_purchase (
    id UUID PRIMARY KEY,
    provider_id UUID NOT NULL,
    description TEXT,
    purchase_date DATE,
    total NUMERIC(19, 2),
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_bus_purchase_provider FOREIGN KEY (provider_id)
        REFERENCES bus_provider (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_bus_purchase_status ON bus_purchase (status);
CREATE INDEX IF NOT EXISTS idx_bus_purchase_provider ON bus_purchase (provider_id);

CREATE TABLE IF NOT EXISTS bus_purchase_detail (
    id UUID PRIMARY KEY,
    purchase_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_bus_purchase_detail_purchase FOREIGN KEY (purchase_id)
        REFERENCES bus_purchase (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_bus_purchase_detail_product FOREIGN KEY (product_id)
        REFERENCES inv_product (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_bus_purchase_detail_purchase ON bus_purchase_detail (purchase_id);
CREATE INDEX IF NOT EXISTS idx_bus_purchase_detail_product ON bus_purchase_detail (product_id);
