package com.oxxo.spin.comisionvariable.application;

import com.oxxo.spin.comisionvariable.domain.exception.BusinessException;
import com.oxxo.spin.comisionvariable.domain.exception.ComisionProviderException;
import com.oxxo.spin.comisionvariable.domain.model.Comision;
import com.oxxo.spin.comisionvariable.domain.model.Consulta;
import com.oxxo.spin.comisionvariable.domain.model.RegistroConsumo;
import com.oxxo.spin.comisionvariable.domain.port.in.ConsultarComisionUseCase;
import com.oxxo.spin.comisionvariable.domain.port.out.BitacoraPort;
import com.oxxo.spin.comisionvariable.domain.port.out.ComisionProviderPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Servicio de APLICACION: implementa el caso de uso orquestando los puertos de
 * salida. Depende SOLO de abstracciones del dominio (puertos + modelos +
 * excepciones); no conoce REST, Spin, JPA, ni la libreria de resiliencia.
 *
 * <p>La unica concesion a la infraestructura es la anotacion CDI de Quarkus
 * para el cableado de dependencias, que no contamina la logica.</p>
 */
@ApplicationScoped
public class ComisionVariableService implements ConsultarComisionUseCase {

    private final ComisionProviderPort comisionProvider;
    private final BitacoraPort bitacora;

    @Inject
    public ComisionVariableService(ComisionProviderPort comisionProvider, BitacoraPort bitacora) {
        this.comisionProvider = comisionProvider;
        this.bitacora = bitacora;
    }

    @Override
    public Comision consultar(Consulta consulta) {
        // Regla de negocio del dominio
        if (!consulta.tieneProductoOServicio()) {
            throw new BusinessException(
                    "PRODUCTO_O_SERVICIO_REQUERIDO",
                    "Debe indicar al menos 'producto' o 'servicio'",
                    422);
        }

        long t0 = System.nanoTime();
        try {
            Comision comision = comisionProvider.obtenerComision(consulta);
            bitacora.registrar(RegistroConsumo.exito(consulta, comision, millis(t0)));
            return comision;
        } catch (ComisionProviderException e) {
            bitacora.registrar(RegistroConsumo.fallo(
                    consulta, e.getResultado(), e.getCodigo(), e.getStatus(), e.getMessage(), millis(t0)));
            throw e;
        }
    }

    private long millis(long t0) {
        return (System.nanoTime() - t0) / 1_000_000;
    }
}
