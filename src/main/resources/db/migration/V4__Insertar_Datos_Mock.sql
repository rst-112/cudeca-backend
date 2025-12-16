-- Insertar Evento de Prueba (Mock)
-- Se deja que el ID se genere automáticamente
INSERT INTO eventos (nombre, descripcion, fecha_inicio, fecha_fin, lugar, estado, objetivo_recaudacion)
VALUES ('Evento Mock Frontend', 'Evento para pruebas de integración', NOW(), NOW() + interval '1 day', 'Localhost', 'PUBLICADO', 1000.00);

-- Insertar Tipo de Entrada 1 (Noche de Jazz)
-- Obtenemos el ID del evento insertado recientemente (el más reciente)
INSERT INTO tipos_entrada (evento_id, nombre, coste_base, donacion_implicita, cantidad_total, cantidad_vendida, limite_por_compra)
VALUES ((SELECT id FROM eventos WHERE nombre = 'Evento Mock Frontend' ORDER BY id DESC LIMIT 1),
        'Asiento normal (Jazz)', 15.00, 0.00, 100, 0, 10);

-- Insertar Tipo de Entrada 2 (Gala Benéfica)
INSERT INTO tipos_entrada (evento_id, nombre, coste_base, donacion_implicita, cantidad_total, cantidad_vendida, limite_por_compra)
VALUES ((SELECT id FROM eventos WHERE nombre = 'Evento Mock Frontend' ORDER BY id DESC LIMIT 1),
        'Entrada general (Gala)', 15.00, 0.00, 100, 0, 10);
