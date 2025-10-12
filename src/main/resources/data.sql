-- *** LOGISTICS FLEET MANAGEMENT DATA ***

-- 1. Umetanje korisnika (app_user tablica)
-- Lozinka za sve korisnike je 'password'
INSERT INTO app_user (first_name,last_name, username, password, email, is_enabled) VALUES
                                                                                       (   'Mislav',
                                                                                           'Čupić',
                                                                                           'admin',
                                                                                           '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW',
                                                                                           'admin@fleet.hr',
                                                                                           TRUE
                                                                                       ),
                                                                                       (   'Mislav',
                                                                                           'Čupić',
                                                                                           'manager',
                                                                                           '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW',
                                                                                           'manager@fleet.hr',
                                                                                           TRUE
                                                                                       ),
                                                                                       (   'Marko',
                                                                                           'Horvat',
                                                                                           'driver1',
                                                                                           '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW',
                                                                                           'driver1@fleet.hr',
                                                                                           TRUE
                                                                                       ),
                                                                                       (   'Ivan',
                                                                                           'Kovač',
                                                                                           'driver2',
                                                                                           '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW',
                                                                                           'driver2@fleet.hr',
                                                                                           TRUE
                                                                                       );

-- 2. Umetanje uloga (roles tablica)
INSERT INTO roles (name) VALUES
                             ('ROLE_ADMIN'),      -- ID 1
                             ('ROLE_MANAGER'),    -- ID 2
                             ('ROLE_DRIVER');     -- ID 3

-- 3. Povezivanje korisnika s ulogama (user_roles tablica)
-- POPRAVLJENO: Korišteno application_user_id umjesto user_id
INSERT INTO user_roles (application_user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (application_user_id, role_id) VALUES (2, 2);
INSERT INTO user_roles (application_user_id, role_id) VALUES (3, 3);
INSERT INTO user_roles (application_user_id, role_id) VALUES (4, 3);

-- 4. Umetanje vozila (vehicle tablica)
INSERT INTO vehicle (license_plate, make, model, model_year, fuel_type, load_capacity_kg, current_driver_id) VALUES
                                                                                                                 ('ZG1234AB', 'Mercedes-Benz', 'Actros 1845', 2020, 'Diesel', 15000.0, 3),
                                                                                                                 ('ST5678CD', 'Volvo', 'FH16 750', 2022, 'Diesel', 18000.0, NULL),
                                                                                                                 ('RI9012EF', 'MAN', 'TGX 440', 2018, 'Diesel', 12500.0, 4);