package com.oxxo.spin.comisionvariable.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oxxo.spin.comisionvariable.adapter.out.persistence.entity.BitacoraConsumo;
import com.oxxo.spin.comisionvariable.adapter.out.persistence.repository.BitacoraRepository;
import com.oxxo.spin.comisionvariable.domain.model.RegistroConsumo;
import com.oxxo.spin.comisionvariable.domain.port.out.BitacoraPort;
import com.oxxo.spin.comisionvariable.infrastructure.logging.TraceContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;

/**
 * Adaptador de SALIDA (driven) que implementa {@link BitacoraPort} sobre
 * JPA/PostgreSQL. Serializa los modelos de dominio a JSON y enriquece con la
 * traza (MDC). El registro ocurre en transaccion independiente y nunca propaga
 * errores: la bitacora no debe romper el flujo principal.
 */
@ApplicationScoped
public class BitacoraJpaAdapter implements BitacoraPort {

    private static final Logger LOG = Logger.getLogger(BitacoraJpaAdapter.class);

    @Inject
    BitacoraRepository repository;

    @Inject
    ObjectMapper mapper;

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void registrar(RegistroConsumo registro) {
        try {
            BitacoraConsumo b = new BitacoraConsumo();
            b.consultaId = registro.consulta().id();
            b.plaza = registro.consulta().plaza();
            b.tienda = registro.consulta().tienda();
            b.caja = registro.consulta().caja();
            b.transaccion = registro.consulta().transaccion();
            b.requestPayload = toJson(registro.consulta());
            b.responsePayload = registro.comision() != null ? toJson(registro.comision()) : null;
            b.resultado = registro.resultado().name();
            b.httpStatus = registro.httpStatus();
            b.errorCodigo = registro.errorCodigo();
            b.errorMensaje = truncar(registro.errorMensaje());
            b.latenciaMs = registro.latenciaMs();
            b.correlationId = TraceContext.currentCorrelationId();
            b.traceId = TraceContext.currentTraceId();
            b.creadoEn = OffsetDateTime.now();
            repository.persist(b);
        } catch (Exception e) {
            LOG.errorf(e, "No se pudo escribir bitacora para consulta %s",
                    registro.consulta() != null ? registro.consulta().id() : "?");
        }
    }

    private String toJson(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }

    private String truncar(String s) {
        return (s != null && s.length() > 500) ? s.substring(0, 500) : s;
    }
}
