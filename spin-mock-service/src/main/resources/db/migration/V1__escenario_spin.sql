-- =====================================================================
-- Tabla de escenarios del mock Spin. El servicio la lee en cada request
-- para decidir la respuesta simulada. Administrable por CRUD /admin/escenarios.
-- =====================================================================
CREATE TABLE escenario_spin (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre              VARCHAR(120) NOT NULL,
    activo              BOOLEAN      NOT NULL DEFAULT true,
    prioridad           INTEGER      NOT NULL DEFAULT 10,   -- menor = se evalua primero
    match_campo         VARCHAR(20),                        -- transaccion | servicio | producto | NULL (default)
    match_regex         VARCHAR(200),                       -- regex Java; NULL = comodin (default)
    http_status         INTEGER      NOT NULL DEFAULT 200,
    porcentaje_comision NUMERIC(6,2),
    monto_comision      NUMERIC(12,2),
    moneda              VARCHAR(3)   DEFAULT 'MXN',
    estatus             VARCHAR(20)  DEFAULT 'CALCULADA',
    error_codigo        VARCHAR(60),
    error_mensaje       VARCHAR(300),
    delay_ms            INTEGER      NOT NULL DEFAULT 0,
    creado_en           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_escenario_activo_prio ON escenario_spin (activo, prioridad);

-- Seed: mismos escenarios que el mock anterior, ahora administrables en BD.
INSERT INTO escenario_spin
  (nombre, prioridad, match_campo, match_regex, http_status, porcentaje_comision, monto_comision, moneda, estatus, error_codigo, error_mensaje, delay_ms) VALUES
  ('Error 500 interno',        1, 'transaccion', '.*(ERR-500|FORCE-ERROR).*', 500, NULL,  NULL,  NULL,  NULL,        'SPIN_INTERNAL',        'Falla interna simulada (reintentos / circuit breaker)', 0),
  ('Error 404 sin tarifa',     1, 'transaccion', '.*ERR-404.*',               404, NULL,  NULL,  NULL,  NULL,        'TARIFA_NO_ENCONTRADA', 'No existe tarifa para el concepto solicitado',          0),
  ('Error 422 datos invalidos',1, 'transaccion', '.*ERR-422.*',               422, NULL,  NULL,  NULL,  NULL,        'DATOS_INVALIDOS',      'Datos de entrada invalidos para el calculo',            0),
  ('Latencia / timeout',       1, 'transaccion', '.*SLOW.*',                  200, 2.50,  12.75, 'MXN', 'CALCULADA', NULL,                   NULL,                                                    7000),
  ('Comision TAE',             2, 'servicio',    '^SERV-TAE.*',               200, 3.50,  17.50, 'MXN', 'CALCULADA', NULL,                   NULL,                                                    0),
  ('Comision pago servicios',  2, 'servicio',    '^SERV-PAGO.*',              200, 1.50,  7.50,  'MXN', 'CALCULADA', NULL,                   NULL,                                                    0),
  ('Comision recarga',         2, 'producto',    '^PROD-RECARGA.*',           200, 2.00,  10.00, 'MXN', 'CALCULADA', NULL,                   NULL,                                                    0),
  ('Comision por defecto',    10, NULL,          NULL,                        200, 2.50,  12.75, 'MXN', 'CALCULADA', NULL,                   NULL,                                                    0);
