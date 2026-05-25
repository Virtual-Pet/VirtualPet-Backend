-- =============================================================================
-- Aligns auth schema with the OpenAPI contract:
--   * /auth/register/customer takes only {email, password, firstName, lastName}
--     so dni and phone are no longer required at registration time.
--   * /auth/register/employee takes the same shape; legajo and warehouse_id
--     become optional fields that ADMIN endpoints can populate later.
-- Existing rows are kept; the previous NOT NULL columns become nullable.
-- =============================================================================

ALTER TABLE auth.customers ALTER COLUMN dni DROP NOT NULL;

-- Drop the standalone UNIQUE constraint so NULL dni values can coexist; add a
-- partial UNIQUE index that ignores NULLs (Postgres treats multiple NULLs as
-- distinct, but the explicit predicate documents intent).
ALTER TABLE auth.customers DROP CONSTRAINT IF EXISTS uq_customer_dni;
CREATE UNIQUE INDEX IF NOT EXISTS uq_customer_dni_present
  ON auth.customers (dni) WHERE dni IS NOT NULL;

ALTER TABLE auth.customers DROP CONSTRAINT IF EXISTS chk_dni_formato;
ALTER TABLE auth.customers
  ADD CONSTRAINT chk_dni_formato CHECK (dni IS NULL OR dni ~ '^\d{7,8}$');

ALTER TABLE auth.employees ALTER COLUMN legajo DROP NOT NULL;
ALTER TABLE auth.employees ALTER COLUMN warehouse_id DROP NOT NULL;

ALTER TABLE auth.employees DROP CONSTRAINT IF EXISTS uq_employees_legajo;
CREATE UNIQUE INDEX IF NOT EXISTS uq_employees_legajo_present
  ON auth.employees (legajo) WHERE legajo IS NOT NULL;
