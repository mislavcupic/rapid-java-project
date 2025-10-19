-- data.sql - Konačna verzija bez ručno postavljenih ID-jeva

-- TRUNCATE TABLE sa RESTART IDENTITY resetira sekvence na 1
TRUNCATE TABLE assignment RESTART IDENTITY CASCADE;
TRUNCATE TABLE shipment RESTART IDENTITY CASCADE;
TRUNCATE TABLE vehicle RESTART IDENTITY CASCADE;
TRUNCATE TABLE driver RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_roles RESTART IDENTITY;
TRUNCATE TABLE roles RESTART IDENTITY CASCADE;
TRUNCATE TABLE app_user RESTART IDENTITY CASCADE;
TRUNCATE TABLE refresh_token RESTART IDENTITY CASCADE;

-- Roles (ID-jevi će se automatski postaviti na 1, 2, 3)
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_DISPATCHER');
INSERT INTO roles (name) VALUES ('ROLE_DRIVER');

-- App_User (ID-jevi će se automatski postaviti na 1, 2, 3, 4)
INSERT INTO app_user (first_name, last_name, username, password, email, is_enabled) VALUES
    ('Admin', 'Korisnik', 'admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'admin@fleet.io', TRUE);
INSERT INTO app_user (first_name, last_name, username, password, email, is_enabled) VALUES
    ('Petra', 'Petrović', 'dispatcher', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'petra@fleet.io', TRUE);
INSERT INTO app_user (first_name, last_name, username, password, email, is_enabled) VALUES
    ('Ivo', 'Ivić', 'ivo.ivic', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'ivo@fleet.io', TRUE);
INSERT INTO app_user (first_name, last_name, username, password, email, is_enabled) VALUES
    ('Marko', 'Marić', 'marko.maric', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'marko@fleet.io', TRUE);

-- User Roles (Koristimo eksplicitne ID-jeve 1, 2, 3, 4, jer znamo da su automatski dodijeljeni)
INSERT INTO user_roles (application_user_id, role_id) VALUES (1, 1); -- Admin (ID 1) dobiva ulogu (ID 1)
INSERT INTO user_roles (application_user_id, role_id) VALUES (2, 2); -- Petra (ID 2) dobiva ulogu (ID 2)
INSERT INTO user_roles (application_user_id, role_id) VALUES (3, 3); -- Ivo (ID 3) dobiva ulogu (ID 3)
INSERT INTO user_roles (application_user_id, role_id) VALUES (4, 3); -- Marko (ID 4) dobiva ulogu (ID 3)

-- Driver Profiles (ID-jevi će se automatski postaviti na 1, 2)
INSERT INTO driver (user_info_id, license_number, license_expiration_date, phone_number) VALUES
    (3, 'A1001', '2028-05-15', '091111222'); -- Povezuje se s Ivom (user_info_id 3)
INSERT INTO driver (user_info_id, license_number, license_expiration_date, phone_number) VALUES
    (4, 'A1002', '2027-10-20', '091333444'); -- Povezuje se s Markom (user_info_id 4)

-- Vehicles (ID se automatski generira, ali mi koristimo licence_plate kao "key")
INSERT INTO vehicle (license_plate, make, model, model_year, fuel_type, load_capacity_kg, current_driver_id) VALUES
    ('ZG1234AB', 'MAN', 'TGX', 2021, 'Diesel', 24000.00, 1); -- current_driver_id 1 (Ivo)
INSERT INTO vehicle (license_plate, make, model, model_year, fuel_type, load_capacity_kg, current_driver_id) VALUES
    ('ST5678CD', 'Volvo', 'FH16', 2023, 'HVO', 25000.00, 2); -- current_driver_id 2 (Marko)
INSERT INTO vehicle (license_plate, make, model, model_year, fuel_type, load_capacity_kg, current_driver_id) VALUES
    ('RI9012EF', 'Renault', 'T-Series', 2020, 'Diesel', 20000.00, NULL);

-- Shipments (ID-jevi će se automatski postaviti na 1, 2)
INSERT INTO shipment (tracking_number, description, weight_kg, origin_address, destination_address, status, expected_delivery_date)
VALUES ('SHIP001ZG', 'Elektronička oprema', 5000.00, 'Zagreb, HR', 'Split, HR', 'PENDING', NOW() + INTERVAL '2 day');
INSERT INTO shipment (tracking_number, description, weight_kg, origin_address, destination_address, status, expected_delivery_date)
VALUES ('SHIP002ST', 'Paleta namještaja', 8000.00, 'Split, HR', 'Rijeka, HR', 'PENDING', NOW() + INTERVAL '3 day');

-- Assignments (ID-jevi će se automatski postaviti na 1, 2)
INSERT INTO assignment (driver_id, vehicle_id, shipment_id, start_time, status)
VALUES (1, 1, 1, NOW() + INTERVAL '1 hour', 'SCHEDULED'); -- driver 1, vehicle 1, shipment 1
INSERT INTO assignment (driver_id, vehicle_id, shipment_id, start_time, status)
VALUES (2, 2, 2, NOW() + INTERVAL '5 hour', 'SCHEDULED'); -- driver 2, vehicle 2, shipment 2