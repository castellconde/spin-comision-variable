package com.oxxo.spin.spinmock.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "escenario_spin")
public class EscenarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    public String nombre;

    @Column(name = "activo", nullable = false)
    public boolean activo = true;

    @Column(name = "prioridad", nullable = false)
    public int prioridad = 10;

    @Column(name = "match_campo", length = 20)
    public String matchCampo;

    @Column(name = "match_regex", length = 200)
    public String matchRegex;

    @Column(name = "http_status", nullable = false)
    public int httpStatus = 200;

    @Column(name = "porcentaje_comision")
    public BigDecimal porcentajeComision;

    @Column(name = "monto_comision")
    public BigDecimal montoComision;

    @Column(name = "moneda", length = 3)
    public String moneda;

    @Column(name = "estatus", length = 20)
    public String estatus;

    @Column(name = "error_codigo", length = 60)
    public String errorCodigo;

    @Column(name = "error_mensaje", length = 300)
    public String errorMensaje;

    @Column(name = "delay_ms", nullable = false)
    public int delayMs = 0;

    @Column(name = "creado_en")
    public OffsetDateTime creadoEn;

    @Column(name = "actualizado_en")
    public OffsetDateTime actualizadoEn;
}
