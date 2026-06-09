package com.oxxo.spin.comisionvariable.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Entidad JPA de la bitacora (detalle de infraestructura). Tabla PARTICIONADA
 * por mes (ver migraciones Flyway).
 */
@Entity
@Table(name = "bitacora_consumo")
public class BitacoraConsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "correlation_id", length = 64)
    public String correlationId;

    @Column(name = "trace_id", length = 64)
    public String traceId;

    @Column(name = "consulta_id", length = 100)
    public String consultaId;

    @Column(name = "plaza", length = 100)
    public String plaza;

    @Column(name = "tienda", length = 100)
    public String tienda;

    @Column(name = "caja", length = 100)
    public String caja;

    @Column(name = "transaccion", length = 100)
    public String transaccion;

    @Column(name = "request_payload", columnDefinition = "jsonb")
    public String requestPayload;

    @Column(name = "response_payload", columnDefinition = "jsonb")
    public String responsePayload;

    @Column(name = "http_status")
    public Integer httpStatus;

    @Column(name = "resultado", length = 20)
    public String resultado;

    @Column(name = "error_codigo", length = 60)
    public String errorCodigo;

    @Column(name = "error_mensaje", length = 500)
    public String errorMensaje;

    @Column(name = "intentos")
    public Integer intentos;

    @Column(name = "latencia_ms")
    public Long latenciaMs;

    @Column(name = "creado_en", nullable = false)
    public OffsetDateTime creadoEn;
}
