-- V10: Promocionar usuario staff@test.com a PERSONAL_EVENTO
-- Se asume que el usuario ya existe (fue creado vía registro frontend o script previo)

DO $$ 
BEGIN
    -- Solo intentamos insertar si el usuario existe
    IF EXISTS (SELECT 1 FROM USUARIOS WHERE email = 'staff@test.com') THEN
        INSERT INTO USUARIOS_ROLES (usuario_id, rol_id)
        VALUES (
            (SELECT id FROM USUARIOS WHERE email = 'staff@test.com'),
            (SELECT id FROM ROLES WHERE nombre = 'PERSONAL_EVENTO')
        )
        ON CONFLICT DO NOTHING;
    END IF;
END $$;
