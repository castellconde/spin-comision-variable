-- =====================================================================
-- V1: Bitacora de consumo del API Spin (tabla PARTICIONADA por mes)
-- =====================================================================

CREATE TABLE bitacora_consumo (
    id              BIGINT GENERATED ALWAYS AS IDENTITY,
    correlation_id  VARCHAR(64),
    trace_id        VARCHAR(64),
    consulta_id     VARCHAR(100),
    plaza           VARCHAR(100),
    tienda          VARCHAR(100),
    caja            VARCHAR(100),
    transaccion     VARCHAR(100),
    request_payload JSONB,
    response_payload JSONB,
    http_status     INTEGER,
    resultado       VARCHAR(20),
    error_codigo    VARCHAR(60),
    error_mensaje   VARCHAR(500),
    intentos        INTEGER,
    latencia_ms     BIGINT,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_bitacora_consumo PRIMARY KEY (id, creado_en)
) PARTITION BY RANGE (creado_en);

CREATE INDEX ix_bitacora_consulta_id ON bitacora_consumo (consulta_id);
CREATE INDEX ix_bitacora_trace_id    ON bitacora_consumo (trace_id);
CREATE INDEX ix_bitacora_creado_en   ON bitacora_consumo (creado_en);
CREATE INDEX ix_bitacora_resultado   ON bitacora_consumo (resultado);

-- Particion por defecto (atrapa cualquier fila fuera de rango: red de seguridad)
CREATE TABLE bitacora_consumo_default PARTITION OF bitacora_consumo DEFAULT;

-- ---------------------------------------------------------------------
-- Funcion: crea las particiones mensuales necesarias hacia el FUTURO.
--   p_meses = cuantos meses por delante asegurar (default 12).
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION crear_particiones_futuras(p_meses INTEGER DEFAULT 12)
RETURNS void AS $$
DECLARE
    v_inicio DATE := date_trunc('month', now())::date;
    v_desde  DATE;
    v_hasta  DATE;
    v_nombre TEXT;
    i        INTEGER;
BEGIN
    FOR i IN 0..p_meses LOOP
        v_desde  := (v_inicio + (i || ' month')::interval)::date;
        v_hasta  := (v_inicio + ((i + 1) || ' month')::interval)::date;
        v_nombre := 'bitacora_consumo_' || to_char(v_desde, 'YYYY_MM');
        IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relname = v_nombre) THEN
            EXECUTE format(
                'CREATE TABLE %I PARTITION OF bitacora_consumo FOR VALUES FROM (%L) TO (%L)',
                v_nombre, v_desde, v_hasta);
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------
-- Funcion: purga particiones ANTIGUAS (retencion configurable, default 6 meses).
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION purgar_particiones_antiguas(p_retencion_meses INTEGER DEFAULT 6)
RETURNS void AS $$
DECLARE
    v_limite DATE := (date_trunc('month', now()) - (p_retencion_meses || ' month')::interval)::date;
    r        RECORD;
    v_fecha  DATE;
BEGIN
    FOR r IN
        SELECT c.relname
        FROM pg_inherits i
        JOIN pg_class c ON c.oid = i.inhrelid
        JOIN pg_class p ON p.oid = i.inhparent
        WHERE p.relname = 'bitacora_consumo'
          AND c.relname ~ '^bitacora_consumo_[0-9]{4}_[0-9]{2}$'
    LOOP
        v_fecha := to_date(substring(r.relname from '([0-9]{4}_[0-9]{2})$'), 'YYYY_MM');
        IF v_fecha < v_limite THEN
            EXECUTE format('DROP TABLE IF EXISTS %I', r.relname);
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Crear de una vez las particiones de los proximos 12 meses.
SELECT crear_particiones_futuras(12);
