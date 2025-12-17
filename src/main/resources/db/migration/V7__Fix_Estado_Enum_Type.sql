-- Cambiar el tipo de columna estado de estado_entrada (ENUM) a VARCHAR
-- Esto soluciona el problema de incompatibilidad de tipos con Hibernate

-- Cambiar el tipo usando USING para la conversión
ALTER TABLE entradas_emitidas 
ALTER COLUMN estado TYPE VARCHAR(20) USING estado::text;

-- Actualizar el valor por defecto
ALTER TABLE entradas_emitidas 
ALTER COLUMN estado SET DEFAULT 'VALIDA';

-- Agregar constraint para validar los valores permitidos
ALTER TABLE entradas_emitidas 
ADD CONSTRAINT check_estado_valido 
CHECK (estado IN ('VALIDA', 'USADA', 'ANULADA'));
