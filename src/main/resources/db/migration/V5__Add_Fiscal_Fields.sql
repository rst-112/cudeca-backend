-- =========================================================
-- MIGRACIÓN V5: Completar Datos Fiscales (Ciudad, CP, Alias)
-- =========================================================

-- 1. Añadir columnas faltantes a DATOS_FISCALES
ALTER TABLE DATOS_FISCALES
    ADD COLUMN IF NOT EXISTS ciudad VARCHAR(100) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS codigo_postal VARCHAR(10) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS alias VARCHAR(50);

-- Limpiar defaults temporales
ALTER TABLE DATOS_FISCALES ALTER COLUMN ciudad DROP DEFAULT;
ALTER TABLE DATOS_FISCALES ALTER COLUMN codigo_postal DROP DEFAULT;