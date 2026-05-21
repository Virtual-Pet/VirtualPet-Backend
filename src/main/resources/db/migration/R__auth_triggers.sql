-- =============================================================================
-- Virtual Pet MDQ — FUNCIONES Y TRIGGERS (Migraciones Repetibles R__)
-- PostgreSQL 15 - Esquema: auth
-- =============================================================================

-- =============================================================================
-- FUNCION/TRIGGER: Actualización automática de timestamp (Auditoría)
-- Descripción: Mantiene la inmutabilidad de la auditoría actualizando 
-- automáticamente el campo 'updated_at' con la fecha/hora del servidor cada vez 
-- que se modifica un registro, ignorando actualizaciones que no alteren datos.
-- =============================================================================
CREATE OR REPLACE FUNCTION auth.fn_actualizar_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON auth.users
FOR EACH ROW
WHEN (OLD.* IS DISTINCT FROM NEW.*) -- Solo se ejecuta si realmente cambió algún dato
EXECUTE FUNCTION auth.fn_actualizar_updated_at();

-- =============================================================================
-- FUNCION/TRIGGER: Revocación de sesiones en cascada (Seguridad)
-- Descripción: Actúa como barrera de seguridad de capa de datos. Si un usuario 
-- es marcado como inactivo (Soft Delete / Baneo), intercepta el cambio y 
-- anula automáticamente todos sus Refresh Tokens, forzando el cierre de sesión 
-- en todos sus dispositivos activos.
-- =============================================================================
CREATE OR REPLACE FUNCTION auth.fn_revocar_sesiones_inactivas()
RETURNS TRIGGER AS $$
BEGIN
    -- Si el usuario acaba de ser desactivado...
    IF NEW.active = FALSE AND OLD.active = TRUE THEN
        -- Revocamos todas sus sesiones activas
        UPDATE auth.refresh_token
        SET revoked = TRUE
        WHERE user_id = NEW.id AND revoked = FALSE;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_desactivar_sesiones
AFTER UPDATE OF active ON auth.users
FOR EACH ROW
EXECUTE FUNCTION auth.fn_revocar_sesiones_inactivas();

-- =============================================================================
-- FUNCION/TRIGGER: Sanitización de correos electrónicos (Normalización)
-- Descripción: Última línea de defensa para la calidad de datos. Obliga a que 
-- todo email insertado o modificado se convierta a minúsculas absolutas y 
-- pierda los espacios en blanco laterales (TRIM), previniendo fallos de login.
-- =============================================================================
CREATE OR REPLACE FUNCTION auth.fn_sanitizar_email()
RETURNS TRIGGER AS $$
BEGIN
    -- Elimina espacios a los costados y pasa todo a minúsculas
    NEW.email = LOWER(TRIM(NEW.email));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_sanitizar_email
BEFORE INSERT OR UPDATE OF email ON auth.users
FOR EACH ROW
EXECUTE FUNCTION auth.fn_sanitizar_email();