-- =========================================================
-- V2: Inserción de Roles por Defecto
-- =========================================================

-- Insertamos los roles base que la aplicación espera encontrar.
-- Usamos 'ON CONFLICT DO NOTHING' para que el script sea idempotente (se puede re-ejecutar sin fallar).

INSERT INTO ROLES (nombre) VALUES ('ADMINISTRADOR') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO ROLES (nombre) VALUES ('COMPRADOR') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO ROLES (nombre) VALUES ('PERSONAL_EVENTO') ON CONFLICT (nombre) DO NOTHING;