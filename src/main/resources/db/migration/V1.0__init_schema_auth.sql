-- =============================================================================
-- Virtual Pet MDQ — DDL 
-- PostgreSQL 15
-- =============================================================================

-- =============================================================================
-- EXTENSIONES 
-- =============================================================================
CREATE EXTENSION IF NOT EXISTS "pgcrypto";   -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "unaccent";   -- búsqueda sin tildes

-- Forzamos a unaccent a ser inmutable para poder usarlo en columnas autogeneradas
ALTER FUNCTION unaccent(text) IMMUTABLE;
-- =============================================================================

-- =============================================================================
-- SCHEMA: usuario
--    Módulo propietario: com.virtualpet.modulo.usuario
--    Acceso externo: módulo checkout (lee usuario_id para crear pedido)
-- =============================================================================
CREATE SCHEMA IF NOT EXISTS auth;


-- =============================================================================
-- TABLE: user
--    Módulo propietario: com.virtualpet.modulo.user
--    Acceso externo: módulo checkout (lee usuario_id para crear pedido)
--	  Solo datos de acceso. Sirve tanto para clientes registrados como para empleados.
--	  No tiene datos de perfil. El campo role discrimina el tipo y define que tabla de
-- 	  extension existe.
-- =============================================================================
CREATE TABLE auth.users (
    id                      UUID          	NOT NULL DEFAULT gen_random_uuid(),
    email                   VARCHAR(255)  	NOT NULL,
    password_hash           VARCHAR(255)  	NOT NULL,               -- bcrypt
	role                    VARCHAR(30)   	NOT NULL DEFAULT 'ROLE_CUSTOMER',
    active                  BOOLEAN       	NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ  	NOT NULL DEFAULT NOW(),
    updated_at  	        TIMESTAMPTZ  	NOT NULL DEFAULT NOW(),
    
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_user_email UNIQUE (email),
    CONSTRAINT chk_user_role CHECK (role IN ('ROLE_CUSTOMER', 'ROLE_EMPLOYEE','ROLE_ADMIN'))
);


-- =============================================================================
-- TABLE: customers
-- 		Solo existe si el usuario se registro. Si es Guest, no hay fila aca.
--		Clientes del MarketPlace. Relación 1:1 con users.
-- =============================================================================
CREATE TABLE auth.customers (
	user_id			UUID			NOT NULL,
	name			VARCHAR(50) 	NOT NULL,
	lastname		VARCHAR(50)		NOT NULL,
	dni				VARCHAR(20)		,
	phone			VARCHAR(30)		,
	created_at		TIMESTAMPTZ		NOT NULL DEFAULT NOW(),

	CONSTRAINT pk_customers PRIMARY KEY (user_id),
    CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE,
	CONSTRAINT chk_dni_formato CHECK (dni IS NULL OR dni ~ '^\d{7,8}$')
);


-- =============================================================================
-- TABLE: employees
--		Guada los datos de los empleados del BackOffice.
--		Relación 1:1 con users. warehouse_id es ref lógica a schema logística.
-- =============================================================================
CREATE TABLE auth.employees (
	user_id			UUID			NOT NULL,
	name			VARCHAR(50)		NOT NULL,
	lastname		VARCHAR(50)		NOT NULL,
	legajo			VARCHAR(50)		,
	warehouse_id	INT				, 	-- Ref lógica a logistica.deposito(id). Sin FK real cross-schema
										-- por diseño (independencia de módulos). Se completa por ADMIN tras el alta.
	created_at 		TIMESTAMPTZ		NOT NULL DEFAULT NOW(),

	CONSTRAINT pk_employees PRIMARY KEY (user_id),
    CONSTRAINT fk_employees_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE
);


-- =============================================================================
--	TABLE: addresses
--		Direcciones del usuario. Permite tener multiples destinos de envio para
--		una misma cuenta. Relación 1:N con users.
-- =============================================================================
CREATE TABLE auth.addresses (
	id 				UUID 			NOT NULL DEFAULT gen_random_uuid(),
	user_id			UUID			NOT NULL,
	street			VARCHAR(100)	NOT NULL,
	num				VARCHAR(15)		NOT NULL,
	city			VARCHAR(100)	NOT NULL,
	zip_code		VARCHAR(20)		NOT NULL,
	is_default		BOOLEAN			NOT NULL DEFAULT FALSE,
	
	CONSTRAINT pk_addresses PRIMARY KEY (id),
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE
);


-- =============================================================================
-- TABLE: refresh_token
--		Permite tener a los usuarios logueados de forma segura sin tener que
--		pedirles credenciales constantemente.
-- =============================================================================
CREATE TABLE auth.refresh_token (
	id				UUID			NOT NULL DEFAULT gen_random_uuid(),
	user_id			UUID			NOT NULL,
	token_hash		VARCHAR(255)	NOT NULL,
	expires_at		TIMESTAMPTZ		NOT NULL,
	revoked			BOOLEAN			NOT NULL DEFAULT FALSE,
	created_at		TIMESTAMPTZ		NOT NULL DEFAULT NOW(),
	
	CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE,
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT chk_token_expired CHECK (expires_at > created_at)
);



-- =============================================================================
-- ÍNDICES
-- =============================================================================


CREATE INDEX idx_dir_usuario_id ON auth.addresses (user_id);
CREATE UNIQUE INDEX idx_dir_main ON auth.addresses (user_id) WHERE is_default = TRUE;

CREATE INDEX idx_sesion_user_id ON auth.refresh_token(user_id);
CREATE INDEX idx_refresh_active ON auth.refresh_token (user_id, expires_at) WHERE revoked = FALSE;

-- DNI/legajo son opcionales en el alta (se completan después). Los índices
-- parciales preservan la unicidad para los valores presentes y permiten
-- múltiples NULLs.
CREATE UNIQUE INDEX uq_customer_dni_present ON auth.customers (dni) WHERE dni IS NOT NULL;
CREATE UNIQUE INDEX uq_employees_legajo_present ON auth.employees (legajo) WHERE legajo IS NOT NULL;

