-- =============================================================================
-- Flyway Migration: V1.9__Role_Rider.sql
-- Descripción: Agrega el rol ROLE_RIDER a la tabla users y crea la tabla 
--              específica de extensión para los repartidores.
-- =============================================================================

-- =============================================================================
-- 1. ACTUALIZAR EL CHECK CONSTRAINT DE LA TABLA USERS
-- =============================================================================

ALTER TABLE auth.users DROP CONSTRAINT IF EXISTS chk_user_role;

ALTER TABLE auth.users ADD CONSTRAINT chk_user_role 
    CHECK (role IN ('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN', 'ROLE_RIDER'));


-- =============================================================================
-- 2. CREAR TABLA DE EXTENSIÓN: riders
--    Módulo: auth.
--    Relación 1:1 con users.
-- =============================================================================
CREATE TABLE auth.riders (
    user_id         UUID            NOT NULL,
    name            VARCHAR(50)     NOT NULL,
    lastname        VARCHAR(50)     NOT NULL,
    phone           VARCHAR(30)     NOT NULL,  -- Obligatorio para contactar al rider en calle
    vehicle_type    VARCHAR(30)     NOT NULL,  -- Ej: 'MOTO', 'BICI', 'CAMIONETA'
    license_plate   VARCHAR(15)     ,          -- Patente (opcional si va en bici)
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_riders PRIMARY KEY (user_id),
    CONSTRAINT fk_riders_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE,
    CONSTRAINT chk_vehicle_type CHECK (vehicle_type IN ('MOTO', 'BICI', 'CAMIONETA', 'AUTO'))
);

-- =============================================================================
-- 3. ÍNDICES
-- =============================================================================
CREATE UNIQUE INDEX uq_riders_plate_present ON auth.riders (license_plate) WHERE license_plate IS NOT NULL;