-- =============================================
-- DDL Modelo de Base de Datos CUDECA
-- =============================================

-- 1. Tipos Enumerados
-- =============================================
-- Si ya existen, estas líneas darán un error "already exists".
CREATE TYPE rol_usuario AS ENUM ('COMPRADOR', 'ADMINISTRADOR', 'PERSONAL_EVENTO');
CREATE TYPE estado_evento AS ENUM ('BORRADOR', 'PUBLICADO', 'CANCELADO', 'FINALIZADO');
CREATE TYPE estado_compra AS ENUM ('PENDIENTE', 'COMPLETADA', 'PARCIAL_REEMBOLSADA', 'REEMBOLSADA', 'CANCELADA');
CREATE TYPE tipo_item AS ENUM ('ENTRADA', 'SORTEO', 'DONACION');
CREATE TYPE estado_entrada AS ENUM ('PENDIENTE', 'VALIDA', 'USADA', 'ANULADA');
CREATE TYPE metodo_pago AS ENUM ('TARJETA', 'PAYPAL', 'BIZUM', 'MONEDERO');
CREATE TYPE estado_pago AS ENUM ('PENDIENTE', 'APROBADO', 'RECHAZADO', 'ANULADO');
CREATE TYPE formato_exportacion AS ENUM ('CSV', 'EXCEL', 'PDF');

-- 2. Función y Trigger para updated_at
-- =============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
   NEW.updated_at = now();
   RETURN NEW;
END;
$$;

-- 3. Entidades Principales
-- =============================================

CREATE TABLE IF NOT EXISTS USUARIOS (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    direccion VARCHAR(255),
    es_socio BOOLEAN NOT NULL DEFAULT FALSE,
    rol rol_usuario NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Trigger
DO $$
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'usuarios') THEN
      IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_usuarios_updated_at') THEN
         CREATE TRIGGER update_usuarios_updated_at
         BEFORE UPDATE ON USUARIOS
         FOR EACH ROW
         EXECUTE FUNCTION update_updated_at_column();
      END IF;
   END IF;
END $$;

CREATE TABLE IF NOT EXISTS MONEDEROS (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id INT UNIQUE NOT NULL REFERENCES USUARIOS(id) ON DELETE CASCADE,
    saldo DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (saldo >= 0)
);

CREATE TABLE IF NOT EXISTS EVENTOS (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP,
    lugar VARCHAR(255),
    estado estado_evento NOT NULL DEFAULT 'BORRADOR',
    objetivo_recaudacion DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (objetivo_recaudacion >= 0),
    recaudado DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (recaudado >= 0)
);

CREATE TABLE IF NOT EXISTS TIPOS_ENTRADA (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    evento_id INT NOT NULL REFERENCES EVENTOS(id) ON DELETE CASCADE,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    cantidad_total INT NOT NULL CHECK (cantidad_total >= 0),
    limite_por_compra INT NOT NULL DEFAULT 10 CHECK (limite_por_compra > 0)
);

CREATE TABLE IF NOT EXISTS COMPRAS (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES USUARIOS(id) ON DELETE RESTRICT,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado estado_compra NOT NULL DEFAULT 'PENDIENTE'
);

CREATE TABLE IF NOT EXISTS ITEMS_COMPRA (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id INT NOT NULL REFERENCES COMPRAS(id) ON DELETE CASCADE,
    tipo_entrada_id INT REFERENCES TIPOS_ENTRADA(id) ON DELETE RESTRICT,
    tipo_item tipo_item NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
    solicita_certificado BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS ENTRADAS_EMITIDAS (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_compra_id INT NOT NULL REFERENCES ITEMS_COMPRA(id) ON DELETE CASCADE,
    codigo_qr VARCHAR(100) UNIQUE NOT NULL,
    estado estado_entrada NOT NULL DEFAULT 'PENDIENTE'
);

CREATE TABLE IF NOT EXISTS VALIDACIONES_ENTRADA (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    entrada_emitida_id INT NOT NULL REFERENCES ENTRADAS_EMITIDAS(id) ON DELETE CASCADE,
    usuario_id INT NOT NULL REFERENCES USUARIOS(id) ON DELETE RESTRICT,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revertida BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS PAGOS (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id INT NOT NULL REFERENCES COMPRAS(id) ON DELETE CASCADE,
    importe DECIMAL(10,2) NOT NULL CHECK (importe > 0),
    metodo metodo_pago NOT NULL,
    estado estado_pago NOT NULL DEFAULT 'PENDIENTE',
    id_transaccion_externa VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS DEVOLUCIONES (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id INT NOT NULL REFERENCES COMPRAS(id) ON DELETE CASCADE,
    pago_id INT REFERENCES PAGOS(id) ON DELETE SET NULL,
    importe DECIMAL(10,2) NOT NULL CHECK (importe > 0),
    motivo VARCHAR(255),
    metodo metodo_pago NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS RECIBOS (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id INT UNIQUE NOT NULL REFERENCES COMPRAS(id) ON DELETE CASCADE,
    fecha_emision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL CHECK (total >= 0)
);

CREATE TABLE IF NOT EXISTS DATOS_FISCALES (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES USUARIOS(id) ON DELETE CASCADE,
    nombre_completo VARCHAR(150) NOT NULL,
    nif VARCHAR(15) NOT NULL,
    direccion VARCHAR(255),
    pais VARCHAR(100),
    UNIQUE (usuario_id, nif)
);

CREATE TABLE IF NOT EXISTS CERTIFICADOS_FISCALES (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id INT NOT NULL REFERENCES COMPRAS(id) ON DELETE CASCADE,
    datos_fiscales_id INT NOT NULL REFERENCES DATOS_FISCALES(id) ON DELETE RESTRICT,
    fecha_emision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    importe_donado DECIMAL(10,2) NOT NULL CHECK (importe_donado >= 0)
);

CREATE TABLE IF NOT EXISTS NOTIFICACIONES (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id INT REFERENCES USUARIOS(id) ON DELETE SET NULL,
    compra_id INT REFERENCES COMPRAS(id) ON DELETE SET NULL,
    tipo VARCHAR(100) NOT NULL,
    destino VARCHAR(255) NOT NULL,
    fecha_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Índices Adicionales
-- =============================================
CREATE INDEX IF NOT EXISTS idx_compras_usuario_id ON COMPRAS(usuario_id);
CREATE INDEX IF NOT EXISTS idx_items_compra_compra_id ON ITEMS_COMPRA(compra_id);
CREATE INDEX IF NOT EXISTS idx_entradas_emitidas_qr ON ENTRADAS_EMITIDAS(codigo_qr);
CREATE INDEX IF NOT EXISTS idx_pagos_compra_id ON PAGOS(compra_id);
CREATE INDEX IF NOT EXISTS idx_eventos_estado ON EVENTOS(estado);
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON USUARIOS(email);
CREATE INDEX IF NOT EXISTS idx_notificaciones_usuario_id ON NOTIFICACIONES(usuario_id);
CREATE INDEX IF NOT EXISTS idx_validaciones_entrada_emitida_id ON VALIDACIONES_ENTRADA(entrada_emitida_id);

-- =============================================
-- Fin del Script DDL CUDECA
-- =============================================