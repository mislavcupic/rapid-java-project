-- DML KOREKCIJA: H2 kompatibilna sintaksa za datume i očuvanje hasheva

DELETE FROM assignment;
DELETE FROM shipment;
DELETE FROM vehicle;
DELETE FROM driver;
DELETE FROM user_roles;
DELETE FROM roles;
DELETE FROM app_user;

-- Roles
INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_DISPATCHER');
INSERT INTO roles (id, name) VALUES (3, 'ROLE_DRIVER');

-- App_User (Lozinke OSTALE ISTO - hash: $2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW, što je 'password')
INSERT INTO app_user (id, first_name, last_name, username, password, email, is_enabled) VALUES
    (1, 'Admin', 'Korisnik', 'admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'admin@fleet.io', TRUE);
INSERT INTO app_user (id, first_name, last_name, username, password, email, is_enabled) VALUES
    (2, 'Petra', 'Petrović', 'dispatcher', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'petra@fleet.io', TRUE);
INSERT INTO app_user (id, first_name, last_name, username, password, email, is_enabled) VALUES
    (3, 'Ivo', 'Ivić', 'ivo.ivic', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'ivo@fleet.io', TRUE);
INSERT INTO app_user (id, first_name, last_name, username, password, email, is_enabled) VALUES
    (4, 'Marko', 'Marić', 'marko.maric', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'marko@fleet.io', TRUE);

-- User Roles
INSERT INTO user_roles (application_user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (application_user_id, role_id) VALUES (2, 2);
INSERT INTO user_roles (application_user_id, role_id) VALUES (3, 3);
INSERT INTO user_roles (application_user_id, role_id) VALUES (4, 3);

-- Driver Profiles (Povezuju se na App_User id 3 i 4)
INSERT INTO driver (id, user_info_id, license_number, license_expiration_date, phone_number) VALUES
    (1, 3, 'A1001', '2028-05-15', '091111222');
INSERT INTO driver (id, user_info_id, license_number, license_expiration_date, phone_number) VALUES
    (2, 4, 'A1002', '2027-10-20', '091333444');

-- Vehicles (Koriste Driver ID)
INSERT INTO vehicle (license_plate, make, model, model_year, fuel_type, load_capacity_kg, current_driver_id) VALUES
    ('ZG1234AB', 'MAN', 'TGX', 2021, 'Diesel', 24000.00, 1);
INSERT INTO vehicle (license_plate, make, model, model_year, fuel_type, load_capacity_kg, current_driver_id) VALUES
    ('ST5678CD', 'Volvo', 'FH16', 2023, 'HVO', 25000.00, 2);
INSERT INTO vehicle (license_plate, make, model, model_year, fuel_type, load_capacity_kg, current_driver_id) VALUES
    ('RI9012EF', 'Renault', 'T-Series', 2020, 'Diesel', 20000.00, NULL);

-- Shipments
INSERT INTO shipment (id, tracking_number, description, weight_kg, origin_address, destination_address, status, expected_delivery_date)
VALUES (1, 'SHIP001ZG', 'Elektronička oprema', 5000.00, 'Zagreb, HR', 'Split, HR', 'PENDING', DATEADD('DAY', 2, CURRENT_TIMESTAMP()));
--                                                                                                                ^ H2 SINTAKSA ZA DATUM
INSERT INTO shipment (id, tracking_number, description, weight_kg, origin_address, destination_address, status, expected_delivery_date)
VALUES (2, 'SHIP002ST', 'Paleta namještaja', 8000.00, 'Split, HR', 'Rijeka, HR', 'PENDING', DATEADD('DAY', 3, CURRENT_TIMESTAMP()));
--                                                                                                                ^ H2 SINTAKSA ZA DATUM

-- Assignments
INSERT INTO assignment (id, driver_id, vehicle_id, shipment_id, start_time, status)
VALUES (1, 1, 1, 1, DATEADD('HOUR', 1, CURRENT_TIMESTAMP()), 'SCHEDULED');
--                           ^ H2 SINTAKSA ZA DATUM
INSERT INTO assignment (id, driver_id, vehicle_id, shipment_id, start_time, status)
VALUES (2, 2, 2, 2, DATEADD('HOUR', 5, CURRENT_TIMESTAMP()), 'SCHEDULED');
--                           ^ H2 SINTAKSA ZA DATUM