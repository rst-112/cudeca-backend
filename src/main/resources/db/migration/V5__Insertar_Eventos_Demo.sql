-- Migración V5: Insertar Eventos de Ejemplo con Imágenes

-- 1. Evento Azul
INSERT INTO eventos (nombre, descripcion, fecha_inicio, fecha_fin, lugar, estado, objetivo_recaudacion, imagen_url)
VALUES ('Noche de Jazz Bajo las Estrellas', 'Disfruta de una velada mágica con los mejores artistas de jazz en un entorno inigualable.', NOW() + interval '2 days', NOW() + interval '2 days' + interval '4 hours', 'Jardines del Centro Cudeca', 'PUBLICADO', 2500.00, '/images/evento_azul_1765919431303.png');

INSERT INTO tipos_entrada (evento_id, nombre, coste_base, donacion_implicita, cantidad_total, cantidad_vendida, limite_por_compra)
VALUES ((SELECT id FROM eventos WHERE nombre = 'Noche de Jazz Bajo las Estrellas' ORDER BY id DESC LIMIT 1), 'Entrada General', 20.00, 5.00, 200, 0, 10);

-- 2. Evento Naranja
INSERT INTO eventos (nombre, descripcion, fecha_inicio, fecha_fin, lugar, estado, objetivo_recaudacion, imagen_url)
VALUES ('Festival de Otoño Solidario', 'Un festival para toda la familia con música, comida y actividades para los más pequeños.', NOW() + interval '10 days', NOW() + interval '10 days' + interval '8 hours', 'Parque de la Paloma', 'PUBLICADO', 5000.00, '/images/evento_naranja_1765919446152.png');

INSERT INTO tipos_entrada (evento_id, nombre, coste_base, donacion_implicita, cantidad_total, cantidad_vendida, limite_por_compra)
VALUES ((SELECT id FROM eventos WHERE nombre = 'Festival de Otoño Solidario' ORDER BY id DESC LIMIT 1), 'Entrada Adulto', 10.00, 2.00, 500, 0, 10);

-- 3. Evento Púrpura
INSERT INTO eventos (nombre, descripcion, fecha_inicio, fecha_fin, lugar, estado, objetivo_recaudacion, imagen_url)
VALUES ('Gala Benéfica Anual', 'Nuestra cena de gala anual para recaudar fondos. Etiqueta requerida.', NOW() + interval '1 month', NOW() + interval '1 month' + interval '5 hours', 'Hotel Miramar', 'PUBLICADO', 15000.00, '/images/evento_purpura_1765919460490.png');

INSERT INTO tipos_entrada (evento_id, nombre, coste_base, donacion_implicita, cantidad_total, cantidad_vendida, limite_por_compra)
VALUES ((SELECT id FROM eventos WHERE nombre = 'Gala Benéfica Anual' ORDER BY id DESC LIMIT 1), 'Mesa VIP', 150.00, 50.00, 50, 0, 2);

-- 4. Evento Rosa
INSERT INTO eventos (nombre, descripcion, fecha_inicio, fecha_fin, lugar, estado, objetivo_recaudacion, imagen_url)
VALUES ('Caminata por la Vida', 'Únete a nuestra caminata solidaria para apoyar los cuidados paliativos.', NOW() + interval '15 days', NOW() + interval '15 days' + interval '3 hours', 'Paseo Marítimo', 'PUBLICADO', 3000.00, '/images/evento_rosa_1765919474723.png');

INSERT INTO tipos_entrada (evento_id, nombre, coste_base, donacion_implicita, cantidad_total, cantidad_vendida, limite_por_compra)
VALUES ((SELECT id FROM eventos WHERE nombre = 'Caminata por la Vida' ORDER BY id DESC LIMIT 1), 'Dorsal Solidario', 5.00, 5.00, 1000, 0, 20);

-- 5. Evento Verde
INSERT INTO eventos (nombre, descripcion, fecha_inicio, fecha_fin, lugar, estado, objetivo_recaudacion, imagen_url)
VALUES ('Concierto por la Esperanza', 'Música en vivo para celebrar la vida y la esperanza.', NOW() + interval '20 days', NOW() + interval '20 days' + interval '4 hours', 'Teatro Cervantes', 'PUBLICADO', 8000.00, '/images/evento_verde_1765919418116.png');

INSERT INTO tipos_entrada (evento_id, nombre, coste_base, donacion_implicita, cantidad_total, cantidad_vendida, limite_por_compra)
VALUES ((SELECT id FROM eventos WHERE nombre = 'Concierto por la Esperanza' ORDER BY id DESC LIMIT 1), 'Butaca Preferente', 25.00, 5.00, 300, 0, 6);
