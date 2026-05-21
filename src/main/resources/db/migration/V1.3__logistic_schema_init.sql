-- =============================================================================
-- SCHEMA: logistics
--    Módulo propietario: com.virtualpet.modulo.logistics
--    Acceso externo: lee orders.orders (cross-schema lógico, sin FK estricta)
-- =============================================================================
CREATE SCHEMA IF NOT EXISTS logistics;

-- =============================================================================
-- TABLE: warehouses (Centros de distribución)
-- =============================================================================
CREATE TABLE logistics.warehouses (
      id              SERIAL          NOT NULL,
      name            VARCHAR(100)    NOT NULL,
      city            VARCHAR(100)    NOT NULL,
      state           VARCHAR(100)    NOT NULL,
      zip_code        VARCHAR(20)     NOT NULL,
      active          BOOLEAN         NOT NULL DEFAULT TRUE,
      created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
      updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

      CONSTRAINT pk_warehouses PRIMARY KEY (id)
);

-- Inserción del centro de distribución principal
INSERT INTO logistics.warehouses (name, city, state, zip_code)
VALUES ('Depósito Central MdP', 'Mar del Plata', 'Buenos Aires', '7600');

-- =============================================================================
-- TABLE: shipments (Envíos asociados a los pedidos)
-- =============================================================================
CREATE TABLE logistics.shipments (
     id                  UUID            DEFAULT gen_random_uuid(),
     order_id            UUID            NOT NULL,
     warehouse_id        INT             NOT NULL,
     courier             VARCHAR(50)     NOT NULL DEFAULT 'OWN_DELIVERY',
     status              VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
     attempts            SMALLINT        NOT NULL DEFAULT 0,
     last_attempt_at     TIMESTAMPTZ,
     version             BIGINT          NOT NULL DEFAULT 0,
     created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
     updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

     CONSTRAINT pk_shipments PRIMARY KEY (id),
     CONSTRAINT fk_shipments_warehouse FOREIGN KEY (warehouse_id) REFERENCES logistics.warehouses(id) ON DELETE RESTRICT,
     CONSTRAINT chk_courier CHECK (courier IN ('OWN_DELIVERY', 'ANDREANI', 'OCA', 'CORREO_ARG')),
     CONSTRAINT chk_shipment_status CHECK (status IN ('PENDING', 'IN_PREPARATION', 'PREPARED', 'SHIPPED', 'DELIVERED', 'FAILED', 'CANCELED')),
     CONSTRAINT chk_shipment_attempts CHECK (attempts BETWEEN 0 AND 3),
     CONSTRAINT uq_shipment_order UNIQUE (order_id)
);

-- =============================================================================
-- TABLE: shipment_status (Auditoría de tiempos de logística)
-- =============================================================================
CREATE TABLE logistics.shipment_status (
       id              UUID            DEFAULT gen_random_uuid(),
       shipment_id     UUID            NOT NULL,
       prev_status     VARCHAR(50)     NOT NULL,
       new_status      VARCHAR(50)     NOT NULL,
       reason          VARCHAR(100)    ,
       modified_by     UUID            ,   -- ID del operario logístico o sistema
       created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

       CONSTRAINT pk_shipment_status_history PRIMARY KEY (id),
       CONSTRAINT fk_history_shipment FOREIGN KEY (shipment_id) REFERENCES logistics.shipments(id) ON DELETE CASCADE
);
-- =============================================================================
-- TABLE: shipment_attempts (Historial de reintentos)
-- =============================================================================
CREATE TABLE logistics.shipment_attempts (
     id                  UUID            DEFAULT gen_random_uuid(),
     shipment_id         UUID            NOT NULL,
     attempt_number      SMALLINT        NOT NULL,
     result              VARCHAR(20)     NOT NULL,
     observation         TEXT,
     registered_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

     CONSTRAINT pk_shipment_attempts PRIMARY KEY (id),
     CONSTRAINT fk_attempts_shipment FOREIGN KEY (shipment_id) REFERENCES logistics.shipments(id) ON DELETE CASCADE,
     CONSTRAINT chk_attempt_num CHECK (attempt_number BETWEEN 1 AND 3),
     CONSTRAINT chk_attempt_result CHECK (result IN ('SUCCESS', 'FAILED')),
     CONSTRAINT uq_attempt_number UNIQUE (shipment_id, attempt_number)
);

-- =============================================================================
-- TABLE: order_assignments (Asignación de cajas a operarios físicos)
-- =============================================================================
CREATE TABLE logistics.order_assignments (
     id              UUID            DEFAULT gen_random_uuid(),
     order_id        UUID            NOT NULL,     -- Ref lógica a orders.orders.id
     warehouse_id    INT             NOT NULL,
     operator_id     UUID            NOT NULL,     -- Ref lógica a auth.users.id
     status          VARCHAR(30)     NOT NULL DEFAULT 'ASSIGNED',
     version         BIGINT          NOT NULL DEFAULT 0,
     assigned_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
     updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
     completed_at    TIMESTAMPTZ,

     CONSTRAINT pk_order_assignments PRIMARY KEY (id),
     CONSTRAINT fk_assignment_warehouse FOREIGN KEY (warehouse_id) REFERENCES logistics.warehouses(id) ON DELETE RESTRICT,
     CONSTRAINT chk_assignment_status CHECK (status IN ('ASSIGNED', 'IN_PROGRESS', 'COMPLETED')),
     CONSTRAINT chk_completed_after_assigned CHECK (completed_at IS NULL OR completed_at >= assigned_at),
     CONSTRAINT uq_assignment_order UNIQUE (order_id)
);

-- =============================================================================
-- ÍNDICES
-- =============================================================================
CREATE INDEX idx_shipments_order_id ON logistics.shipments (order_id);
CREATE INDEX idx_shipments_status ON logistics.shipments (status);
CREATE INDEX idx_shipments_warehouse ON logistics.shipments (warehouse_id);

CREATE INDEX idx_shipment_history_id ON logistics.shipment_status (shipment_id);

CREATE INDEX idx_shipment_attempts_id ON logistics.shipment_attempts (shipment_id);

CREATE INDEX idx_assignments_order_id ON logistics.order_assignments (order_id);
CREATE INDEX idx_assignments_operator ON logistics.order_assignments (operator_id);
CREATE INDEX idx_assignments_wh_status ON logistics.order_assignments (warehouse_id, status);