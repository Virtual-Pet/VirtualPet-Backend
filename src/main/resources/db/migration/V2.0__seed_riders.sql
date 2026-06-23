-- =============================================================================
-- Flyway Migration: V2.0__seed_riders.sql
-- Descripción: Datos seed de repartidores (ROLE_RIDER) en auth.users + auth.riders.
--              Todos los riders comparten la contraseña: Password123!
--              (hash bcrypt $2a$10, generado con BCryptPasswordEncoder).
-- Idempotente: ON CONFLICT DO NOTHING permite reejecutar sin duplicar.
-- =============================================================================

-- 1. Usuarios base (el id se autogenera con gen_random_uuid()).
INSERT INTO auth.users (email, password_hash, role, active)
VALUES
    ('rider@virtualpet.com',  '$2a$10$9bIwZjXbo8qInw3b/DVYuOFCnbKTmKb0hVUp.FPWKEstT/.PEH4jm', 'ROLE_RIDER', TRUE),
    ('rider2@virtualpet.com', '$2a$10$9bIwZjXbo8qInw3b/DVYuOFCnbKTmKb0hVUp.FPWKEstT/.PEH4jm', 'ROLE_RIDER', TRUE),
    ('rider3@virtualpet.com', '$2a$10$9bIwZjXbo8qInw3b/DVYuOFCnbKTmKb0hVUp.FPWKEstT/.PEH4jm', 'ROLE_RIDER', TRUE)
ON CONFLICT (email) DO NOTHING;

-- 2. Extensión rider, enlazada por email para no depender del UUID generado arriba.
INSERT INTO auth.riders (user_id, name, lastname, phone, vehicle_type, license_plate)
SELECT u.id, v.name, v.lastname, v.phone, v.vehicle_type, v.license_plate
FROM (
    VALUES
        ('rider@virtualpet.com',  'Repartidor', 'Demo',      '+54 9 223 555-0001', 'MOTO',      'A123BCD'),
        ('rider2@virtualpet.com', 'Lucía',      'Fernández', '+54 9 223 555-0002', 'BICI',      NULL),
        ('rider3@virtualpet.com', 'Diego',      'Sosa',      '+54 9 223 555-0003', 'CAMIONETA', 'AB456CD')
) AS v (email, name, lastname, phone, vehicle_type, license_plate)
JOIN auth.users u ON u.email = v.email
ON CONFLICT (user_id) DO NOTHING;
