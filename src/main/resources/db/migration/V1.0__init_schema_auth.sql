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
    id              UUID          	NOT NULL DEFAULT gen_random_uuid(),
    email           VARCHAR(255)  	NOT NULL,
    password_hash   VARCHAR(255)  	NOT NULL,               -- bcrypt 
	role            VARCHAR(30)   	NOT NULL DEFAULT 'ROLE_CUSTOMER',
    active          BOOLEAN       	NOT NULL DEFAULT TRUE,
    email_verified	BOOLEAN		  	NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  	NOT NULL DEFAULT NOW(),
    updated_at  	TIMESTAMPTZ  	NOT NULL DEFAULT NOW(),
    
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_user_email UNIQUE (email),
    CONSTRAINT chk_user_role CHECK (role IN ('ROLE_CUSTOMER', 'ROLE_EMPLOYEE','ROLE_ADMIN'))
);


-- =============================================================================
-- TABLE: password_reset_tokens
-- 		Gestiona el "elvide mi contraseña". EL sistema genera un token de un solo
-- 		uso, lo envia por mail y lo marca como usado al cambiar contraseña.
-- =============================================================================
CREATE TABLE auth.password_reset_tokens (
	id              UUID          	NOT NULL DEFAULT gen_random_uuid(),
    user_id			UUID		  	NOT NULL,
	password_token	VARCHAR(255)  	NOT NULL,
	expires_at		TIMESTAMPTZ	  	NOT NULL,
	used_at			TIMESTAMPTZ	  	,
	created_at		TIMESTAMPTZ		NOT NULL DEFAULT NOW(),
	
	CONSTRAINT pk_reset_tokens PRIMARY KEY (id),
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE,
    CONSTRAINT uq_reset_token UNIQUE (password_token),
    CONSTRAINT chk_reset_expires CHECK (expires_at > created_at),
    CONSTRAINT chk_used_after_created CHECK (used_at IS NULL OR used_at >= created_at)
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
	dni				VARCHAR(20) 	NOT NULL,
	phone			VARCHAR(30)		,
	created_at		TIMESTAMPTZ		NOT NULL DEFAULT NOW(),
	
	CONSTRAINT pk_customers PRIMARY KEY (user_id),
    CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE,
	CONSTRAINT uq_customer_dni UNIQUE (dni),
	CONSTRAINT chk_dni_formato CHECK (dni ~ '^\d{7,8}$')
);


-- =============================================================================
-- TABLE: employees
--		Guada los datos de los empleados del BackOffice.
--		Relación 1:1 con users. deposit_id es ref lógica a schema logistica.
-- =============================================================================
CREATE TABLE auth.employees (
	user_id			UUID			NOT NULL, 
	name			VARCHAR(50)		NOT NULL,
	lastname		VARCHAR(50)		NOT NULL,
	legajo			VARCHAR(50)		NOT NULL,
	deposit_id		INT				NOT NULL, 	-- Ref lógica a logistica.deposito(id). Sin FK real cross-schema
												-- por diseño (independencia de módulos).
	created_at 		TIMESTAMPTZ		NOT NULL DEFAULT NOW(),
	
	CONSTRAINT pk_employees PRIMARY KEY (user_id),
    CONSTRAINT fk_employees_user FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE,
    CONSTRAINT uq_employees_legajo UNIQUE (legajo)
);


-- =============================================================================
--	TABLE: addresses
--		Direcciones del usuario. Permite tener multiples destinos de envio para
--		una misma cuenta. Relacion 1:N con users.
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

