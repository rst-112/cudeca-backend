-- V11: Asegurar rol ADMINISTRADOR para admin@test.com (Schema Correcto)

-- 1. Insertar usuario admin si no existe
INSERT INTO USUARIOS (email, password_hash, nombre)
VALUES ('admin@test.com', '$2a$10$X/hXpD3g.d1Q8wX6i4K.Oe.j8.j.j.j.j.j.j.j.j.j', 'Admin')
ON CONFLICT (email) DO NOTHING;

-- 2. Asignar rol ADMINISTRADOR
-- Usamos subselects seguras compatible con Postgres
INSERT INTO USUARIOS_ROLES (usuario_id, rol_id)
SELECT u.id, r.id
FROM USUARIOS u, ROLES r
WHERE u.email = 'admin@test.com' AND r.nombre = 'ADMINISTRADOR'
ON CONFLICT (usuario_id, rol_id) DO NOTHING;
