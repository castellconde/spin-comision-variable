package com.oxxo.spin.comisionvariable.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oxxo.spin.comisionvariable.logging.TraceContext;
import com.oxxo.spin.comisionvariable.persistence.entity.BitacoraConsumo;
import com.oxxo.spin.comisionvariable.persistence.repository.BitacoraRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;

/**
 * Persiste la bitacora de cada consumo al API Spin. El registro se hace en una
 * transaccion independiente (REQUIRES_NEW) para que un fallo de Spin no impida
 * dejar evidencia, ni una falla de bitacora tumbe la respuesta al cliente.
 */
@ApplicationScoped
public class BitacoraService {

    private static final Logger LOG = Logger.getLogger(BitacoraService.class);

    @Inject
    BitacoraRepository repository;

    @Inject
    ObjectMapper mapper;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void registrar(BitacoraConsumo b) {
        try {
            b.correlationId = TraceContext.currentCorrelationId();
            b.traceId = TraceContext.currentTraceId();
            b.creadoEn = OffsetDateTime.now();
            repository.persist(b);
        } catch (Exception e) {
            // Nunca propagar: la bitacora no debe romper el flujo principal.
            LOG.errorf(e, "No se pudo escribir bitacora para consulta %s", b.consultaId);
        }
    }

    public String toJson(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }
}
