-- data.sql - APSOLUTNA KONAČNA KOREKCIJA (UKLJUČUJUĆI VEHICLE MAINTENANCE)

--korekcije sheme i dodavanje novih kolona

-- Dodavanje kolona za Pošiljke i Rute (ako nisu postojale)
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS origin_latitude DOUBLE PRECISION;
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS origin_longitude DOUBLE PRECISION;
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS destination_latitude DOUBLE PRECISION;
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS destination_longitude DOUBLE PRECISION;
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS actual_delivery_date TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS shipment_value NUMERIC(19, 2);
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS volume_m3 NUMERIC(19, 2);
ALTER TABLE assignment ADD COLUMN IF NOT EXISTS route_id BIGINT;


ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS current_mileage_km BIGINT;
ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS last_service_date DATE;
ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS next_service_mileage_km BIGINT;
ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS fuel_consumption_liters_per_100km NUMERIC(5, 2);

-- Drop Foreign Key (Ako je ranije bio krivo referenciran)
ALTER TABLE shipment DROP CONSTRAINT IF EXISTS fk_route;




TRUNCATE TABLE assignment RESTART IDENTITY CASCADE;
TRUNCATE TABLE routes RESTART IDENTITY CASCADE;
TRUNCATE TABLE shipment RESTART IDENTITY CASCADE;
TRUNCATE TABLE vehicle RESTART IDENTITY CASCADE;
TRUNCATE TABLE driver RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_roles RESTART IDENTITY;
TRUNCATE TABLE roles RESTART IDENTITY CASCADE;
TRUNCATE TABLE app_user RESTART IDENTITY CASCADE;
TRUNCATE TABLE refresh_token RESTART IDENTITY CASCADE;



-- Roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_DISPATCHER');
INSERT INTO roles (name) VALUES ('ROLE_DRIVER');

-- App_User
INSERT INTO app_user (first_name, last_name, username, password, email, is_enabled) VALUES
    ('Admin', 'Korisnik', 'admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'admin@fleet.io', TRUE);
INSERT INTO app_user (first_name, last_name, username, password, email, is_enabled) VALUES
    ('Petra', 'Petrović', 'dispatcher', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'petra@fleet.io', TRUE);
INSERT INTO app_user (first_name, last_name, username, password, email, is_enabled) VALUES
    ('Ivo', 'Ivić', 'ivo.ivic', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'ivo@fleet.io', TRUE);
INSERT INTO app_user (first_name, last_name, username, password, email, is_enabled) VALUES
    ('Marko', 'Marić', 'marko.maric', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'marko@fleet.io', TRUE);

-- User Roles
INSERT INTO user_roles (application_user_id, role_id) VALUES (1, 1); -- Admin
INSERT INTO user_roles (application_user_id, role_id) VALUES (2, 2); -- Petra (DISPATCHER)
INSERT INTO user_roles (application_user_id, role_id) VALUES (3, 3); -- Ivo (DRIVER)
INSERT INTO user_roles (application_user_id, role_id) VALUES (4, 3); -- Marko (DRIVER)

-- Driver Profiles
INSERT INTO driver (user_info_id, license_number, license_expiration_date, phone_number) VALUES
    (3, 'A1001', '2028-05-15', '091111222'); -- Ivo (ID 1)
INSERT INTO driver (user_info_id, license_number, license_expiration_date, phone_number) VALUES
    (4, 'A1002', '2027-10-20', '091333444'); -- Marko (ID 2)

-- =================================================================
-- 4. UNOS VOZILA (TESTIRANJE ODRŽAVANJA)
-- =================================================================

INSERT INTO vehicle (
    license_plate, make, model, model_year, fuel_type, load_capacity_kg,
    current_driver_id,
    current_mileage_km, last_service_date, next_service_mileage_km, fuel_consumption_liters_per_100km
) VALUES
      -- Vozilo 1: Upozorenje blizu servisa (4000 km do servisa -> Žuto)
      ('ZG1234AB', 'MAN', 'TGX', 2021, 'Diesel', 24000.00, 1,
       171000, '2025-09-01', 175000, 28.50),

      -- Vozilo 2: Normalno stanje (15000 km do servisa -> Zeleno)
      ('ST5678CD', 'Volvo', 'FH16', 2023, 'HVO', 25000.00, 2,
       85000, '2025-10-10', 100000, 26.00),

      -- Vozilo 3: Hitno - Servis prekoračen (Prekoračeno za 500 km -> Crveno)
      ('RI9012EF', 'Renault', 'T-Series', 2020, 'Diesel', 20000.00, NULL,
       270500, '2025-06-20', 270000, 30.10);


-- =================================================================
-- 5. RUTE, POŠILJKE, ASSIGNMENTS
-- =================================================================

-- RUTA 1: ZG -> ST (ID 1)
INSERT INTO routes (origin_address, origin_latitude, origin_longitude, destination_address, destination_latitude, destination_longitude, estimated_distance_km, estimated_duration_minutes, status)
VALUES ('Zagreb, HR', 45.8150, 15.9819, 'Split, HR', 43.5081, 16.4402, 410.50, 240, 'CALCULATED');

-- RUTA 2: ST -> RI (ID 2)
INSERT INTO routes (origin_address, origin_latitude, origin_longitude, destination_address, destination_latitude, destination_longitude, estimated_distance_km, estimated_duration_minutes, status)
VALUES ('Split, HR', 43.5081, 16.4402, 'Rijeka, HR', 45.3271, 14.4422, 390.00, 280, 'CALCULATED');


-- SHIPMENT 1 (ID 1)
INSERT INTO shipment (tracking_number, description, weight_kg, origin_address, destination_address, origin_latitude, origin_longitude, destination_latitude, destination_longitude, shipment_value, volume_m3, status, expected_delivery_date, route_id)
VALUES ('SHIP001ZG', 'Elektronička oprema', 5000.00, 'Zagreb, HR', 'Split, HR', 45.8150, 15.9819, 43.5081, 16.4402, 15000.00, 12.50, 'PENDING', NOW() + INTERVAL '2 day', 1);

-- SHIPMENT 2 (ID 2)
INSERT INTO shipment (tracking_number, description, weight_kg, origin_address, destination_address, origin_latitude, origin_longitude, destination_latitude, destination_longitude, shipment_value, volume_m3, status, expected_delivery_date, route_id)
VALUES ('SHIP002ST', 'Paleta namještaja', 8000.00, 'Split, HR', 'Rijeka, HR', 43.5081, 16.4402, 45.3271, 14.4422, 8500.00, 20.00, 'PENDING', NOW() + INTERVAL '3 day', 2);

-- PONOVNO KREIRANJE FOREIGN KEYA
ALTER TABLE shipment ADD CONSTRAINT fk_routes FOREIGN KEY (route_id) REFERENCES routes(id);

-- ASSIGNMENTS
INSERT INTO assignment (driver_id, vehicle_id, shipment_id, route_id, start_time, status)
VALUES (1, 1, 1, 1, NOW() + INTERVAL '1 hour', 'SCHEDULED');

INSERT INTO assignment (driver_id, vehicle_id, shipment_id, route_id, start_time, status)
VALUES (2, 2, 2, 2, NOW() + INTERVAL '5 hour', 'SCHEDULED');