package com.oxxo.spin.comisionvariable.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Parametros de configuracion de consumo de servicios externos y de
 * mantenimiento de la bitacora. Los secretos reales viven en Conjur.
 */
@Entity
@Table(name = "configuracion_servicio")
public class ConfiguracionServicio {

    @Id
    @Column(name = "clave", length = 80)
    public String clave;

    @Column(name = "valor", length = 500, nullable = false)
    public String valor;

    @Column(name = "descripcion", length = 300)
    public String descripcion;

    @Column(name = "actualizado_en")
    public OffsetDateTime actualizadoEn;
}
