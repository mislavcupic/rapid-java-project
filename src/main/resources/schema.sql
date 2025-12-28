-- ═══════════════════════════════════════════════════════════
-- SCHEMA.SQL - VERZIJA 2.0 (ManyToOne Assignment-Shipment)
-- ═══════════════════════════════════════════════════════════

-- DROP EXISTING TABLES
DROP TABLE IF EXISTS assignment CASCADE;
DROP TABLE IF EXISTS shipment CASCADE;
DROP TABLE IF EXISTS routes CASCADE;
DROP TABLE IF EXISTS vehicle CASCADE;
DROP TABLE IF EXISTS driver CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;
DROP TABLE IF EXISTS refresh_token CASCADE;

-- ═══════════════════════════════════════════════════════════
-- USERS & ROLES
-- ═══════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS app_user (
                                        id BIGSERIAL PRIMARY KEY,
                                        first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    is_enabled BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS roles (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS user_roles (
                                          application_user_id BIGINT NOT NULL,
                                          role_id BIGINT NOT NULL,
                                          PRIMARY KEY (application_user_id, role_id),
    FOREIGN KEY (application_user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS refresh_token (
                                             id BIGSERIAL PRIMARY KEY,
                                             token VARCHAR(500) NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
    );

-- ═══════════════════════════════════════════════════════════
-- DRIVER & VEHICLE
-- ═══════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS driver (
                                      id BIGSERIAL PRIMARY KEY,
                                      user_info_id BIGINT NOT NULL UNIQUE,
                                      license_number VARCHAR(50) NOT NULL UNIQUE,
    license_expiration_date DATE NOT NULL,
    phone_number VARCHAR(20),
    FOREIGN KEY (user_info_id) REFERENCES app_user(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS vehicle (
                                       id BIGSERIAL PRIMARY KEY,
                                       license_plate VARCHAR(20) NOT NULL UNIQUE,
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    model_year INT,
    fuel_type VARCHAR(20),
    load_capacity_kg NUMERIC(10, 2),
    current_driver_id BIGINT,

    -- Maintenance fields
    current_mileage_km BIGINT DEFAULT 0,
    last_service_date DATE,
    next_service_mileage_km BIGINT,
    fuel_consumption_liters_per_100km NUMERIC(5, 2),

    FOREIGN KEY (current_driver_id) REFERENCES driver(id) ON DELETE SET NULL
    );

-- ═══════════════════════════════════════════════════════════
-- ROUTES
-- ═══════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS routes (
                                      id BIGSERIAL PRIMARY KEY,
                                      origin_address VARCHAR(255) NOT NULL,
    origin_latitude DECIMAL(10, 6) NOT NULL,
    origin_longitude DECIMAL(10, 6) NOT NULL,
    destination_address VARCHAR(255) NOT NULL,
    destination_latitude DECIMAL(10, 6) NOT NULL,
    destination_longitude DECIMAL(10, 6) NOT NULL,
    estimated_distance_km DECIMAL(10, 2),
    estimated_duration_minutes BIGINT,
    status VARCHAR(50) NOT NULL
    );

-- ═══════════════════════════════════════════════════════════
-- ASSIGNMENT (bez shipment_id jer je sada OneToMany!)
-- ═══════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS assignment (
                                          id BIGSERIAL PRIMARY KEY,
                                          driver_id BIGINT NOT NULL,
                                          vehicle_id BIGINT NOT NULL,
                                          route_id BIGINT NOT NULL UNIQUE,
                                          start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                                          end_time TIMESTAMP WITHOUT TIME ZONE,
                                          status VARCHAR(50) NOT NULL,

    FOREIGN KEY (driver_id) REFERENCES driver(id) ON DELETE RESTRICT,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id) ON DELETE RESTRICT,
    FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE RESTRICT
    );

-- ═══════════════════════════════════════════════════════════
-- SHIPMENT (sa assignment_id za ManyToOne!)
-- ═══════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS shipment (
                                        id BIGSERIAL PRIMARY KEY,
                                        tracking_number VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    weight_kg NUMERIC(10, 2),
    volume_m3 NUMERIC(10, 2),
    shipment_value NUMERIC(19, 2),

    -- Addresses
    origin_address VARCHAR(255) NOT NULL,
    destination_address VARCHAR(255) NOT NULL,

    -- Coordinates
    origin_latitude DOUBLE PRECISION,
    origin_longitude DOUBLE PRECISION,
    destination_latitude DOUBLE PRECISION,
    destination_longitude DOUBLE PRECISION,

    -- Status & Dates
    status VARCHAR(50) NOT NULL,
    expected_delivery_date TIMESTAMP WITHOUT TIME ZONE,
    actual_delivery_date TIMESTAMP WITHOUT TIME ZONE,

    -- Delivery sequence (for optimization)
    delivery_sequence INT,

    -- Foreign Keys
    route_id BIGINT,
    assignment_id BIGINT,  -- ✅ NOVA KOLONA ZA ManyToOne!

    CONSTRAINT fk_shipment_route FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE SET NULL,
    CONSTRAINT fk_shipment_assignment FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE SET NULL
    );

-- ═══════════════════════════════════════════════════════════
-- INDEXES
-- ═══════════════════════════════════════════════════════════

CREATE INDEX idx_shipment_assignment_id ON shipment(assignment_id);
CREATE INDEX idx_shipment_route_id ON shipment(route_id);
CREATE INDEX idx_shipment_status ON shipment(status);
CREATE INDEX idx_assignment_driver_id ON assignment(driver_id);
CREATE INDEX idx_assignment_vehicle_id ON assignment(vehicle_id);
CREATE INDEX idx_vehicle_current_driver_id ON vehicle(current_driver_id);