package com.oxxo.spin.comisionvariable.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Parametros de configuracion de consumo de servicios externos y de
 * mantenimiento de la tabla transaccional (bitacora). Permite ajustar valores
 * sin redeploy (ej. retencion de particiones, client_id de referencia).
 *
 * <p>NOTA: los secretos reales (client_secret) NO se guardan aqui; viven en
 * Conjur y se inyectan por variable de ambiente. Esta tabla guarda metadatos y
 * parametros no sensibles.</p>
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
