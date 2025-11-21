-- =========================================================
-- DDL Modelo de Base de Datos CUDECA (PostgreSQL)
-- =========================================================

-- 1) Tipos Enumerados
-- =========================================================
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_evento') THEN
        CREATE TYPE estado_evento AS ENUM ('BORRADOR','PUBLICADO','CANCELADO','FINALIZADO');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_compra') THEN
        CREATE TYPE estado_compra AS ENUM ('PENDIENTE','COMPLETADA','PARCIAL_REEMBOLSADA','REEMBOLSADA','CANCELADA');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_item') THEN
        CREATE TYPE tipo_item AS ENUM ('ENTRADA','SORTEO','DONACION');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_entrada') THEN
        CREATE TYPE estado_entrada AS ENUM ('VALIDA','USADA','ANULADA');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'metodo_pago') THEN
        CREATE TYPE metodo_pago AS ENUM ('TARJETA','PAYPAL','BIZUM','MONEDERO');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_pago') THEN
        CREATE TYPE estado_pago AS ENUM ('PENDIENTE','APROBADO','RECHAZADO','ANULADO');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'formato_exportacion') THEN
        CREATE TYPE formato_exportacion AS ENUM ('CSV','EXCEL','PDF');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_notificacion') THEN
        CREATE TYPE estado_notificacion AS ENUM ('PENDIENTE','ENVIADA','ERROR');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_suscripcion') THEN
        CREATE TYPE estado_suscripcion AS ENUM ('ACTIVA','PENDIENTE','RENOVACION','CANCELADA');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_mov_mon') THEN
        -- 'RETIRO' Añadido para el Requisito 3
        CREATE TYPE tipo_mov_mon AS ENUM ('ABONO','CARGO','RETIRO');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_devolucion') THEN
        CREATE TYPE tipo_devolucion AS ENUM ('PASARELA','MONEDERO');
    END IF;

    -- NUEVOS ENUMS (Para Mapa y Reglas)
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_ajuste_regla') THEN
        CREATE TYPE tipo_ajuste_regla AS ENUM ('PORCENTAJE', 'FIJO');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_asiento') THEN
        CREATE TYPE estado_asiento AS ENUM ('LIBRE', 'BLOQUEADO', 'VENDIDO');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_retiro') THEN
        CREATE TYPE estado_retiro AS ENUM ('PENDIENTE', 'PROCESADA', 'RECHAZADA');
    END IF;
END $$;

-- 2) Función + Trigger updated_at
-- =========================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
   NEW.updated_at = now();
   RETURN NEW;
END;
$$;

-- =========================================================
-- 3) TABLAS PRINCIPALES
-- =========================================================

-- 3.1 USUARIOS / RBAC
-- =========================================================
CREATE TABLE IF NOT EXISTS USUARIOS (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre        VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    direccion     VARCHAR(255),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_usuarios_updated_at') THEN
        CREATE TRIGGER trg_usuarios_updated_at BEFORE UPDATE ON USUARIOS
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS ROLES (
    id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS PERMISOS (
    id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS USUARIOS_ROLES (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id  BIGINT NOT NULL REFERENCES USUARIOS(id) ON DELETE CASCADE,
    rol_id      BIGINT NOT NULL REFERENCES ROLES(id)    ON DELETE CASCADE,
    asignado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_usuario_rol UNIQUE (usuario_id, rol_id)
);

CREATE TABLE IF NOT EXISTS ROLES_PERMISOS (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rol_id     BIGINT NOT NULL REFERENCES ROLES(id)    ON DELETE CASCADE,
    permiso_id BIGINT NOT NULL REFERENCES PERMISOS(id) ON DELETE CASCADE,
    CONSTRAINT ux_rol_permiso UNIQUE (rol_id, permiso_id)
);

-- 3.2 Invitados + verificaciones (con XOR)
-- =========================================================
CREATE TABLE IF NOT EXISTS INVITADOS (
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS VERIFICACIONES_CUENTA (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id  BIGINT REFERENCES USUARIOS(id)  ON DELETE CASCADE,
    invitado_id BIGINT REFERENCES INVITADOS(id) ON DELETE CASCADE,
    email       VARCHAR(150) NOT NULL,
    token       VARCHAR(180) NOT NULL UNIQUE,
    expira_en   TIMESTAMPTZ  NOT NULL,
    usado       BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_verif_xor CHECK ( num_nonnulls(usuario_id, invitado_id) = 1 )
);
CREATE INDEX IF NOT EXISTS ix_verif_email ON VERIFICACIONES_CUENTA(email);

-- 3.3 Monedero y Retiros (REQ 3)
-- =========================================================
CREATE TABLE IF NOT EXISTS MONEDEROS (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES USUARIOS(id) ON DELETE CASCADE,
    saldo      NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (saldo >= 0)
);

CREATE TABLE IF NOT EXISTS MOVIMIENTOS_MONEDERO (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    monedero_id BIGINT NOT NULL REFERENCES MONEDEROS(id) ON DELETE CASCADE,
    tipo        tipo_mov_mon NOT NULL,
    importe     NUMERIC(12,2) NOT NULL CHECK (importe > 0),
    fecha       TIMESTAMPTZ NOT NULL DEFAULT now(),
    referencia  VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS ix_movmon_monedero_fecha ON MOVIMIENTOS_MONEDERO(monedero_id, fecha);

-- NUEVA TABLA (REQ 3): SOLICITUDES DE RETIRO
CREATE TABLE IF NOT EXISTS SOLICITUDES_RETIRO (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id      BIGINT NOT NULL REFERENCES USUARIOS(id) ON DELETE CASCADE,
    importe         NUMERIC(12,2) NOT NULL CHECK (importe > 0),
    iban_destino    VARCHAR(34) NOT NULL,
    estado          estado_retiro NOT NULL DEFAULT 'PENDIENTE',
    fecha_solicitud TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 3.4 Eventos, Imágenes, Reglas y Mapa (REQ 1, 2, 4)
-- =========================================================
CREATE TABLE IF NOT EXISTS EVENTOS (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre               VARCHAR(150) NOT NULL,
    descripcion          TEXT,
    fecha_inicio         TIMESTAMPTZ  NOT NULL,
    fecha_fin            TIMESTAMPTZ,
    lugar                VARCHAR(255),
    estado               estado_evento NOT NULL DEFAULT 'BORRADOR',
    objetivo_recaudacion NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (objetivo_recaudacion >= 0),
    imagen_url           VARCHAR(255) NULL -- Imagen Destacada (Thumbnail)
);
CREATE INDEX IF NOT EXISTS ix_eventos_estado ON EVENTOS(estado);

CREATE TABLE IF NOT EXISTS IMAGENES_EVENTO (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    evento_id    BIGINT NOT NULL REFERENCES EVENTOS(id) ON DELETE CASCADE,
    url          VARCHAR(255) NOT NULL,
    descripcion  VARCHAR(255),
    orden        INT DEFAULT 0,
    es_resumen   BOOLEAN DEFAULT FALSE,
    es_principal BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS ix_imagenes_evento ON IMAGENES_EVENTO(evento_id);

-- NUEVA TABLA (REQ 1): REGLAS DE PRECIOS (DESCUENTOS)
CREATE TABLE IF NOT EXISTS REGLAS_PRECIOS (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    evento_id            BIGINT NOT NULL REFERENCES EVENTOS(id) ON DELETE CASCADE,
    nombre               VARCHAR(100) NOT NULL,
    tipo_ajuste          tipo_ajuste_regla NOT NULL,
    valor                NUMERIC(10,2) NOT NULL CHECK (valor > 0),
    requiere_suscripcion BOOLEAN DEFAULT FALSE
);

-- TABLA MODIFICADA (REQ 4 y 5): TIPOS_ENTRADA (División de precios)
CREATE TABLE IF NOT EXISTS TIPOS_ENTRADA (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    evento_id          BIGINT NOT NULL REFERENCES EVENTOS(id) ON DELETE CASCADE,
    nombre             VARCHAR(100) NOT NULL,
    coste_base         NUMERIC(10,2) NOT NULL CHECK (coste_base >= 0),
    donacion_implicita NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (donacion_implicita >= 0),
    cantidad_total     INT NOT NULL CHECK (cantidad_total >= 0),
    cantidad_vendida   INT NOT NULL DEFAULT 0 CHECK (cantidad_vendida >= 0),
    limite_por_compra  INT NOT NULL DEFAULT 10 CHECK (limite_por_compra > 0),
    CHECK (cantidad_vendida <= cantidad_total)
);
CREATE INDEX IF NOT EXISTS ix_tipos_evento ON TIPOS_ENTRADA(evento_id);

-- NUEVAS TABLAS (REQ 2): MAPA INTERACTIVO
CREATE TABLE IF NOT EXISTS ZONAS_RECINTO (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    evento_id   BIGINT NOT NULL REFERENCES EVENTOS(id) ON DELETE CASCADE,
    nombre      VARCHAR(100) NOT NULL,
    aforo_total INT NOT NULL CHECK (aforo_total > 0)
);

CREATE TABLE IF NOT EXISTS ASIENTOS (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    zona_id         BIGINT NOT NULL REFERENCES ZONAS_RECINTO(id) ON DELETE CASCADE,
    tipo_entrada_id BIGINT NOT NULL REFERENCES TIPOS_ENTRADA(id) ON DELETE RESTRICT,
    codigo_etiqueta VARCHAR(20) NOT NULL, -- Ej: "A-12"
    fila            INT,
    columna         INT,
    estado          estado_asiento NOT NULL DEFAULT 'LIBRE',
    UNIQUE(zona_id, codigo_etiqueta)
);

-- 3.5 Compras, Ítems, Ajustes, Consentimientos
-- =========================================================
CREATE TABLE IF NOT EXISTS COMPRAS (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id     BIGINT REFERENCES USUARIOS(id)  ON DELETE RESTRICT,
    invitado_id    BIGINT REFERENCES INVITADOS(id) ON DELETE RESTRICT,
    email_contacto VARCHAR(150),
    fecha          TIMESTAMPTZ NOT NULL DEFAULT now(),
    estado         estado_compra NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT chk_compra_usuario_invitado CHECK ( num_nonnulls(usuario_id, invitado_id) = 1 )
);
CREATE INDEX IF NOT EXISTS ix_compras_email_contacto ON COMPRAS(email_contacto);

-- TABLA MODIFICADA: ITEMS_COMPRA (Incluye asiento_id)
CREATE TABLE IF NOT EXISTS ITEMS_COMPRA (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id            BIGINT NOT NULL REFERENCES COMPRAS(id) ON DELETE CASCADE,
    tipo_entrada_id      BIGINT REFERENCES TIPOS_ENTRADA(id) ON DELETE RESTRICT,
    asiento_id           BIGINT REFERENCES ASIENTOS(id) ON DELETE SET NULL, -- Nuevo FK
    tipo_item            tipo_item NOT NULL,
    cantidad             INT NOT NULL CHECK (cantidad > 0),
    precio_unitario      NUMERIC(10,2) NOT NULL CHECK (precio_unitario >= 0),
    solicita_certificado BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT chk_items_tipoentrada CHECK (
        (tipo_item IN ('ENTRADA', 'SORTEO') AND tipo_entrada_id IS NOT NULL) OR
        (tipo_item = 'DONACION' AND tipo_entrada_id IS NULL)
    )
);
CREATE INDEX IF NOT EXISTS ix_items_compra ON ITEMS_COMPRA(compra_id);
CREATE INDEX IF NOT EXISTS ix_items_asiento ON ITEMS_COMPRA(asiento_id);

CREATE TABLE IF NOT EXISTS AJUSTES_PRECIO (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id BIGINT NOT NULL REFERENCES COMPRAS(id) ON DELETE CASCADE,
    item_id   BIGINT REFERENCES ITEMS_COMPRA(id) ON DELETE CASCADE,
    tipo      VARCHAR(60) NOT NULL,
    base      NUMERIC(12,2) NOT NULL DEFAULT 0,
    valor     NUMERIC(12,2) NOT NULL,
    motivo    VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS ix_ajustes_compra ON AJUSTES_PRECIO(compra_id);

CREATE TABLE IF NOT EXISTS CONSENTIMIENTOS (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id BIGINT NOT NULL REFERENCES COMPRAS(id) ON DELETE CASCADE,
    tipo      VARCHAR(60) NOT NULL,
    version   VARCHAR(40) NOT NULL,
    fecha     TIMESTAMPTZ NOT NULL DEFAULT now(),
    otorgado  BOOLEAN   NOT NULL,
    CONSTRAINT ux_compra_tipo UNIQUE (compra_id, tipo)
);

-- 3.6 Entradas + validaciones
-- =========================================================
CREATE TABLE IF NOT EXISTS ENTRADAS_EMITIDAS (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_compra_id BIGINT NOT NULL REFERENCES ITEMS_COMPRA(id) ON DELETE CASCADE,
    codigo_qr      VARCHAR(120) NOT NULL UNIQUE,
    estado         estado_entrada NOT NULL DEFAULT 'VALIDA'
);
CREATE INDEX IF NOT EXISTS ix_entradas_item ON ENTRADAS_EMITIDAS(item_compra_id);

CREATE TABLE IF NOT EXISTS VALIDACIONES_ENTRADA (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    entrada_emitida_id BIGINT NOT NULL REFERENCES ENTRADAS_EMITIDAS(id) ON DELETE CASCADE,
    usuario_id         BIGINT NOT NULL REFERENCES USUARIOS(id) ON DELETE RESTRICT,
    fecha_hora         TIMESTAMPTZ NOT NULL DEFAULT now(),
    dispositivo_id     VARCHAR(120),
    revertida          BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS ix_validaciones_entrada ON VALIDACIONES_ENTRADA(entrada_emitida_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_validacion_activa
    ON VALIDACIONES_ENTRADA(entrada_emitida_id) WHERE revertida = false;

-- 3.7 Suscripciones, Pagos, Devoluciones, Recibos
-- =========================================================
CREATE TABLE IF NOT EXISTS SUSCRIPCIONES (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id            BIGINT NOT NULL REFERENCES USUARIOS(id) ON DELETE CASCADE,
    fecha_inicio          TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_fin             TIMESTAMPTZ,
    estado                estado_suscripcion NOT NULL DEFAULT 'PENDIENTE',
    renovacion_automatica BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS ix_susc_usuario ON SUSCRIPCIONES(usuario_id);

CREATE TABLE IF NOT EXISTS PAGOS (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id              BIGINT NOT NULL REFERENCES COMPRAS(id) ON DELETE CASCADE,
    suscripcion_id         BIGINT REFERENCES SUSCRIPCIONES(id) ON DELETE SET NULL,
    importe                NUMERIC(12,2) NOT NULL CHECK (importe > 0),
    metodo                 metodo_pago NOT NULL,
    estado                 estado_pago  NOT NULL DEFAULT 'PENDIENTE',
    id_transaccion_externa VARCHAR(255),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_pagos_compra_estado ON PAGOS(compra_id, estado);
CREATE UNIQUE INDEX IF NOT EXISTS ux_pagos_tx_externa
    ON PAGOS(id_transaccion_externa) WHERE id_transaccion_externa IS NOT NULL;

CREATE TABLE IF NOT EXISTS DEVOLUCIONES (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id       BIGINT NOT NULL REFERENCES COMPRAS(id) ON DELETE CASCADE,
    pago_id         BIGINT REFERENCES PAGOS(id) ON DELETE SET NULL,
    mov_monedero_id BIGINT REFERENCES MOVIMIENTOS_MONEDERO(id) ON DELETE SET NULL,
    importe         NUMERIC(12,2) NOT NULL CHECK (importe > 0),
    motivo          VARCHAR(255),
    tipo            tipo_devolucion NOT NULL,
    fecha           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_devol_tipo CHECK (
        (tipo = 'MONEDERO' AND mov_monedero_id IS NOT NULL AND pago_id IS NULL) OR
        (tipo = 'PASARELA'  AND pago_id IS NOT NULL AND mov_monedero_id IS NULL)
    )
);
CREATE INDEX IF NOT EXISTS ix_devol_compra          ON DEVOLUCIONES(compra_id);

CREATE TABLE IF NOT EXISTS RECIBOS (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id     BIGINT NOT NULL UNIQUE REFERENCES COMPRAS(id) ON DELETE CASCADE,
    fecha_emision TIMESTAMPTZ NOT NULL DEFAULT now(),
    total         NUMERIC(12,2) NOT NULL CHECK (total >= 0),
    resumen       TEXT
);

-- 3.8 Fiscalidad y Notificaciones (MODELO LIBRETA DE DIRECCIONES) (REQ 5)
-- =========================================================
CREATE TABLE IF NOT EXISTS DATOS_FISCALES (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id      BIGINT REFERENCES USUARIOS(id) ON DELETE SET NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    nif             VARCHAR(20)  NOT NULL,
    direccion       VARCHAR(255) NOT NULL,
    pais            VARCHAR(100) NOT NULL
);
CREATE INDEX IF NOT EXISTS ix_dfisc_usuario ON DATOS_FISCALES(usuario_id);

CREATE TABLE IF NOT EXISTS CERTIFICADOS_FISCALES (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id           BIGINT NOT NULL UNIQUE REFERENCES COMPRAS(id) ON DELETE CASCADE,
    datos_fiscales_id   BIGINT NOT NULL REFERENCES DATOS_FISCALES(id) ON DELETE RESTRICT,
    fecha_emision       TIMESTAMPTZ NOT NULL DEFAULT now(),
    importe_donado      NUMERIC(12,2) NOT NULL CHECK (importe_donado >= 0),
    numero_serie        VARCHAR(80) NOT NULL UNIQUE,
    hash_documento      VARCHAR(120),
    datos_snapshot_json JSONB
);
CREATE INDEX IF NOT EXISTS ix_certs_datos  ON CERTIFICADOS_FISCALES(datos_fiscales_id);

CREATE TABLE IF NOT EXISTS NOTIFICACIONES (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id   BIGINT REFERENCES USUARIOS(id) ON DELETE SET NULL,
    compra_id    BIGINT REFERENCES COMPRAS(id) ON DELETE SET NULL,
    tipo         VARCHAR(120) NOT NULL,
    destino      VARCHAR(255) NOT NULL,
    estado       estado_notificacion NOT NULL DEFAULT 'PENDIENTE',
    intentos     INT NOT NULL DEFAULT 0,
    payload_json JSONB,
    fecha_envio  TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS ix_notif_estado          ON NOTIFICACIONES(estado);

-- 3.9 Beneficios de socio
-- =========================================================
CREATE TABLE IF NOT EXISTS BENEFICIOS_SOCIO (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    suscripcion_id BIGINT NOT NULL REFERENCES SUSCRIPCIONES(id) ON DELETE CASCADE,
    tipo           VARCHAR(60) NOT NULL,
    valor          NUMERIC(12,2) NOT NULL,
    descripcion    VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS ix_benef_susc ON BENEFICIOS_SOCIO(suscripcion_id);

-- 3.10 Auditorías y exportaciones
-- =========================================================
CREATE TABLE IF NOT EXISTS AUDITORIAS (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id BIGINT REFERENCES USUARIOS(id) ON DELETE SET NULL,
    entidad    VARCHAR(120) NOT NULL,
    entidad_id VARCHAR(120) NOT NULL,
    accion     VARCHAR(120) NOT NULL,
    fecha      TIMESTAMPTZ NOT NULL DEFAULT now(),
    detalle    TEXT
);

CREATE TABLE IF NOT EXISTS EXPORTACIONES (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    autor_id    BIGINT NOT NULL REFERENCES USUARIOS(id) ON DELETE CASCADE,
    formato     formato_exportacion NOT NULL,
    generado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 4) Vistas útiles
-- =========================================================
CREATE OR REPLACE VIEW vw_eventos_recaudado AS
-- Calcula el neto REAL de cada compra individual
WITH NetoPorCompra AS (
    SELECT
        c.id AS compra_id,
        (COALESCE(p.total_pagado, 0) - COALESCE(d.total_devuelto, 0)) AS importe_neto_compra
    FROM COMPRAS c
    -- Subconsulta de Pagos Aprobados
    LEFT JOIN (
        SELECT compra_id, SUM(importe) AS total_pagado
        FROM PAGOS
        WHERE estado = 'APROBADO'
        GROUP BY compra_id
    ) p ON c.id = p.compra_id
    -- Subconsulta de TODAS las Devoluciones
    LEFT JOIN (
        SELECT compra_id, SUM(importe) AS total_devuelto
        FROM DEVOLUCIONES
        GROUP BY compra_id
    ) d ON c.id = d.compra_id
),
-- Identifica qué compras (únicas) pertenecen a qué evento
ComprasPorEvento AS (
    SELECT DISTINCT
        t.evento_id,
        i.compra_id
    FROM ITEMS_COMPRA i
    JOIN TIPOS_ENTRADA t ON i.tipo_entrada_id = t.id
    WHERE i.tipo_item = 'ENTRADA'
)
-- Consulta Final: Suma el neto de las compras asociadas a cada evento
SELECT
    e.id,
    e.nombre,
    e.objetivo_recaudacion,
    e.imagen_url,
    COALESCE(SUM(npc.importe_neto_compra), 0) AS recaudado_neto
FROM EVENTOS e
LEFT JOIN ComprasPorEvento cpe ON e.id = cpe.evento_id
LEFT JOIN NetoPorCompra npc      ON cpe.compra_id = npc.compra_id
GROUP BY e.id, e.nombre, e.objetivo_recaudacion, e.imagen_url;