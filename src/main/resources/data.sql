-- ═══════════════════════════════════════════════════════════
-- DATA.SQL - VERZIJA 2.0 (ManyToOne Assignment-Shipment)
-- ═══════════════════════════════════════════════════════════

-- TRUNCATE ALL (redoslijed bitan zbog FK!)
TRUNCATE TABLE shipment RESTART IDENTITY CASCADE;
TRUNCATE TABLE assignment RESTART IDENTITY CASCADE;
TRUNCATE TABLE routes RESTART IDENTITY CASCADE;
TRUNCATE TABLE vehicle RESTART IDENTITY CASCADE;
TRUNCATE TABLE driver RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_roles RESTART IDENTITY CASCADE;
TRUNCATE TABLE roles RESTART IDENTITY CASCADE;
TRUNCATE TABLE refresh_token RESTART IDENTITY CASCADE;
TRUNCATE TABLE app_user RESTART IDENTITY CASCADE;

-- ═══════════════════════════════════════════════════════════
-- 1. ROLES
-- ═══════════════════════════════════════════════════════════

INSERT INTO roles (name) VALUES
                             ('ROLE_ADMIN'),
                             ('ROLE_DISPATCHER'),
                             ('ROLE_DRIVER');

-- ═══════════════════════════════════════════════════════════
-- 2. USERS
-- ═══════════════════════════════════════════════════════════

INSERT INTO app_user (first_name, last_name, username, password, email, is_enabled) VALUES
                                                                                        ('Admin', 'Korisnik', 'admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'admin@fleet.io', TRUE),
                                                                                        ('Petra', 'Petrović', 'dispatcher', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'petra@fleet.io', TRUE),
                                                                                        ('Ivo', 'Ivić', 'ivo.ivic', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'ivo@fleet.io', TRUE),
                                                                                        ('Marko', 'Marić', 'marko.maric', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'marko@fleet.io', TRUE);

-- ═══════════════════════════════════════════════════════════
-- 3. USER ROLES
-- ═══════════════════════════════════════════════════════════

INSERT INTO user_roles (application_user_id, role_id) VALUES
                                                          (1, 1), -- Admin
                                                          (2, 2), -- Petra (DISPATCHER)
                                                          (3, 3), -- Ivo (DRIVER)
                                                          (4, 3); -- Marko (DRIVER)

-- ═══════════════════════════════════════════════════════════
-- 4. DRIVERS
-- ═══════════════════════════════════════════════════════════

INSERT INTO driver (user_info_id, license_number, license_expiration_date, phone_number) VALUES
                                                                                             (3, 'A1001', '2028-05-15', '091111222'), -- Ivo (ID 1)
                                                                                             (4, 'A1002', '2027-10-20', '091333444'); -- Marko (ID 2)

-- ═══════════════════════════════════════════════════════════
-- 5. VEHICLES (sa maintenance podacima)
-- ═══════════════════════════════════════════════════════════

INSERT INTO vehicle (
    license_plate, make, model, model_year, fuel_type, load_capacity_kg,
    current_driver_id,
    current_mileage_km, last_service_date, next_service_mileage_km, fuel_consumption_liters_per_100km
) VALUES
      -- Vozilo 1: Upozorenje blizu servisa (4000 km do servisa)
      ('ZG1234AB', 'MAN', 'TGX', 2021, 'Diesel', 24000.00, 1,
       171000, '2025-09-01', 175000, 28.50),

      -- Vozilo 2: Normalno stanje (15000 km do servisa)
      ('ST5678CD', 'Volvo', 'FH16', 2023, 'HVO', 25000.00, 2,
       85000, '2025-10-10', 100000, 26.00),

      -- Vozilo 3: Hitno - Servis prekoračen (500 km preko)
      ('RI9012EF', 'Renault', 'T-Series', 2020, 'Diesel', 20000.00, NULL,
       270500, '2025-06-20', 270000, 30.10);

-- ═══════════════════════════════════════════════════════════
-- 6. ROUTES
-- ═══════════════════════════════════════════════════════════

INSERT INTO routes (
    origin_address, origin_latitude, origin_longitude,
    destination_address, destination_latitude, destination_longitude,
    estimated_distance_km, estimated_duration_minutes, status
) VALUES
      -- RUTA 1: ZG -> ST (ID 1)
      ('Zagreb, HR', 45.8150, 15.9819, 'Split, HR', 43.5081, 16.4402, 410.50, 240, 'CALCULATED'),

      -- RUTA 2: ST -> RI (ID 2)
      ('Split, HR', 43.5081, 16.4402, 'Rijeka, HR', 45.3271, 14.4422, 390.00, 280, 'CALCULATED'),

      -- RUTA 3: ZG -> OS (ID 3)
      ('Zagreb, HR', 45.8150, 15.9819, 'Osijek, HR', 45.5550, 18.6955, 285.00, 180, 'CALCULATED');

-- ═══════════════════════════════════════════════════════════
-- 7. ASSIGNMENTS (PRIJE shipments!)
-- ═══════════════════════════════════════════════════════════

INSERT INTO assignment (driver_id, vehicle_id, route_id, start_time, status) VALUES
                                                                                 -- Assignment 1: Ivo, MAN kamion, ruta ZG->ST
                                                                                 (1, 1, 1, NOW() + INTERVAL '1 hour', 'SCHEDULED'),

                                                                                 -- Assignment 2: Marko, Volvo kamion, ruta ST->RI
                                                                                 (2, 2, 2, NOW() + INTERVAL '5 hour', 'SCHEDULED');

-- ═══════════════════════════════════════════════════════════
-- 8. SHIPMENTS (sa assignment_id!)
-- ═══════════════════════════════════════════════════════════

INSERT INTO shipment (
    tracking_number, description, weight_kg, volume_m3, shipment_value,
    origin_address, destination_address,
    origin_latitude, origin_longitude, destination_latitude, destination_longitude,
    status, expected_delivery_date, route_id, assignment_id
) VALUES
      -- SHIPMENT 1 & 2: Dodijeljeni Assignment-u 1 (Ivo, ZG->ST)
      ('SHIP001ZG', 'Elektronička oprema', 5000.00, 12.50, 15000.00,
       'Zagreb, HR', 'Split, HR', 45.8150, 15.9819, 43.5081, 16.4402,
       'SCHEDULED', NOW() + INTERVAL '2 day', 1, 1),

      ('SHIP002ZG', 'Namještaj', 3000.00, 8.00, 7500.00,
       'Zagreb, HR', 'Split, HR', 45.8150, 15.9819, 43.5081, 16.4402,
       'SCHEDULED', NOW() + INTERVAL '2 day', 1, 1),

      -- SHIPMENT 3: Dodijeljen Assignment-u 2 (Marko, ST->RI)
      ('SHIP003ST', 'Paleta namještaja', 8000.00, 20.00, 8500.00,
       'Split, HR', 'Rijeka, HR', 43.5081, 16.4402, 45.3271, 14.4422,
       'SCHEDULED', NOW() + INTERVAL '3 day', 2, 2),

      -- SHIPMENT 4 & 5: PENDING (nisu dodijeljeni ni jednom assignment-u)
      ('SHIP004ZG', 'Građevinski materijal', 12000.00, 15.00, 3000.00,
       'Zagreb, HR', 'Osijek, HR', 45.8150, 15.9819, 45.5550, 18.6955,
       'PENDING', NOW() + INTERVAL '4 day', 3, NULL),

      ('SHIP005ZG', 'Prehrambeni proizvodi', 6000.00, 10.00, 4500.00,
       'Zagreb, HR', 'Osijek, HR', 45.8150, 15.9819, 45.5550, 18.6955,
       'PENDING', NOW() + INTERVAL '4 day', 3, NULL);