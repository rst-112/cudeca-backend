-- V3: Añadir soporte para Mapa de Asientos Visual (JSONB)

-- 1. Añadir metadatos visuales a los asientos (coordenadas x, y, rotación, forma)
-- Esto permite que el Front dibuje el mapa sin romper el modelo relacional
ALTER TABLE ASIENTOS
ADD COLUMN metadata_visual JSONB;

-- 2. Añadir objetos decorativos a las zonas (escenario, barras, textos)
-- No merece la pena crear una tabla SQL para "plantas", se guardan como documento
ALTER TABLE ZONAS_RECINTO
ADD COLUMN objetos_decorativos JSONB;