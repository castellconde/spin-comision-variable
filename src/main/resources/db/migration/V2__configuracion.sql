-- =====================================================================
-- V2: Tabla de configuracion de servicio + parametros iniciales
--     (secretos reales NO se guardan aqui; viven en Conjur)
-- =====================================================================

CREATE TABLE configuracion_servicio (
    clave         VARCHAR(80)  PRIMARY KEY,
    valor         VARCHAR(500) NOT NULL,
    descripcion   VARCHAR(300),
    actualizado_en TIMESTAMPTZ DEFAULT now()
);

INSERT INTO configuracion_servicio (clave, valor, descripcion) VALUES
    ('spin.client_id_ref',          'demo-client',  'Referencia (no secreta) del client_id usado contra Spin'),
    ('spin.timeout_ms',             '6000',         'Timeout de consulta a Spin (ms) - referencia operativa'),
    ('spin.retry_max',              '3',            'Numero de reintentos hacia Spin - referencia operativa'),
    ('bitacora.retencion_meses',    '6',            'Meses de retencion de particiones de bitacora'),
    ('bitacora.particiones_futuras','12',           'Meses de particiones futuras a mantener creadas');
