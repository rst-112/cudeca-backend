-- Migración: Crear evento de prueba con mapa de asientos y tipos de entrada
-- Este evento se usa para demonstrar el selector de asientos interactivo

-- 1. Insertar evento de prueba
INSERT INTO EVENTOS (
    nombre,
    descripcion,
    fecha_inicio,
    fecha_fin,
    lugar,
    estado,
    objetivo_recaudacion,
    imagen_url
) VALUES (
    'Concierto Benéfico de Jazz',
    'Una noche mágica con música en vivo y cena gourmet para recaudar fondos para cuidados paliativos. Disfruta de actuaciones de artistas locales, subastas solidarias y una velada inolvidable.',
    '2025-03-15 20:00:00',
    '2025-03-15 23:30:00',
    'Auditorio Municipal de Málaga',
    'PUBLICADO',
    15000.00,
    'https://images.unsplash.com/photo-1514320291840-2e0a9bf2a9ae?w=800'
) ON CONFLICT DO NOTHING;

-- 2. Crear tipos de entrada para el evento
-- Nota: Usamos una subconsulta para obtener el ID del evento insertado
INSERT INTO TIPOS_ENTRADA (
    evento_id,
    nombre,
    coste_base,
    donacion_implicita,
    cantidad_total,
    cantidad_vendida,
    limite_por_compra
)
SELECT
    e.id,
    'VIP',
    40.00,
    10.00,
    50,
    0,
    10
FROM EVENTOS e
WHERE e.nombre = 'Concierto Benéfico de Jazz'
ON CONFLICT DO NOTHING;

INSERT INTO TIPOS_ENTRADA (
    evento_id,
    nombre,
    coste_base,
    donacion_implicita,
    cantidad_total,
    cantidad_vendida,
    limite_por_compra
)
SELECT
    e.id,
    'General',
    20.00,
    5.00,
    100,
    0,
    10
FROM EVENTOS e
WHERE e.nombre = 'Concierto Benéfico de Jazz'
ON CONFLICT DO NOTHING;

-- 3. Crear zonas del recinto
INSERT INTO ZONAS_RECINTO (evento_id, nombre, aforo_total)
SELECT
    e.id,
    'Zona VIP - Platea',
    50
FROM EVENTOS e
WHERE e.nombre = 'Concierto Benéfico de Jazz'
ON CONFLICT DO NOTHING;

INSERT INTO ZONAS_RECINTO (evento_id, nombre, aforo_total)
SELECT
    e.id,
    'Zona General - Anfiteatro',
    100
FROM EVENTOS e
WHERE e.nombre = 'Concierto Benéfico de Jazz'
ON CONFLICT DO NOTHING;

-- 4. Crear asientos para Zona VIP (5 filas x 10 columnas = 50 asientos)
INSERT INTO ASIENTOS (zona_id, tipo_entrada_id, codigo_etiqueta, fila, columna, estado)
SELECT
    z.id,
    te.id,
    'VIP-' || chr(64 + f) || '-' || c::text,  -- Genera VIP-A-1, VIP-A-2, etc.
    f,
    c,
    CASE
        -- Marcar algunos asientos como vendidos para demostración
        WHEN (f = 1 AND c IN (3, 7)) THEN 'VENDIDO'::estado_asiento
        WHEN (f = 2 AND c IN (2, 5, 8)) THEN 'VENDIDO'::estado_asiento
        ELSE 'LIBRE'::estado_asiento
    END
FROM ZONAS_RECINTO z
CROSS JOIN TIPOS_ENTRADA te
CROSS JOIN generate_series(1, 5) f  -- 5 filas
CROSS JOIN generate_series(1, 10) c -- 10 columnas por fila
INNER JOIN EVENTOS e ON e.id = z.evento_id
WHERE z.nombre = 'Zona VIP - Platea'
  AND te.nombre = 'VIP'
  AND e.nombre = 'Concierto Benéfico de Jazz'
ON CONFLICT DO NOTHING;

-- 5. Crear asientos para Zona General (10 filas x 10 columnas = 100 asientos)
INSERT INTO ASIENTOS (zona_id, tipo_entrada_id, codigo_etiqueta, fila, columna, estado)
SELECT
    z.id,
    te.id,
    'GEN-' || chr(64 + f) || '-' || c::text,  -- Genera GEN-A-1, GEN-A-2, etc.
    f,
    c,
    CASE
        -- Marcar algunos asientos como vendidos para demostración
        WHEN (f <= 3 AND c IN (4, 5, 6, 7)) THEN 'VENDIDO'::estado_asiento
        WHEN (f = 5 AND c = 5) THEN 'VENDIDO'::estado_asiento
        ELSE 'LIBRE'::estado_asiento
    END
FROM ZONAS_RECINTO z
CROSS JOIN TIPOS_ENTRADA te
CROSS JOIN generate_series(1, 10) f  -- 10 filas
CROSS JOIN generate_series(1, 10) c -- 10 columnas por fila
INNER JOIN EVENTOS e ON e.id = z.evento_id
WHERE z.nombre = 'Zona General - Anfiteatro'
  AND te.nombre = 'General'
  AND e.nombre = 'Concierto Benéfico de Jazz'
ON CONFLICT DO NOTHING;
