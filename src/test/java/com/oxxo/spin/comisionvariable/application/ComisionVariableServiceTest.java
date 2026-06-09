package com.oxxo.spin.comisionvariable.application;

import com.oxxo.spin.comisionvariable.domain.exception.BusinessException;
import com.oxxo.spin.comisionvariable.domain.exception.ComisionProviderException;
import com.oxxo.spin.comisionvariable.domain.model.Comision;
import com.oxxo.spin.comisionvariable.domain.model.Consulta;
import com.oxxo.spin.comisionvariable.domain.model.RegistroConsumo;
import com.oxxo.spin.comisionvariable.domain.model.ResultadoConsulta;
import com.oxxo.spin.comisionvariable.domain.port.out.BitacoraPort;
import com.oxxo.spin.comisionvariable.domain.port.out.ComisionProviderPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Prueba UNITARIA PURA del nucleo (sin Quarkus, sin contenedores): se mockean
 * los puertos de salida. Esta velocidad y aislamiento son el beneficio directo
 * de la arquitectura hexagonal.
 */
class ComisionVariableServiceTest {

    private final ComisionProviderPort provider = mock(ComisionProviderPort.class);
    private final BitacoraPort bitacora = mock(BitacoraPort.class);
    private final ComisionVariableService service = new ComisionVariableService(provider, bitacora);

    private Consulta consulta(String producto, String servicio) {
        return new Consulta("POS-1", "PLZ-001", "TND-001", "CAJA-1", "TRX-1", producto, servicio);
    }

    @Test
    void sinProductoNiServicio_lanzaBusinessException_yNoConsultaProveedor() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.consultar(consulta(null, null)));
        assertEquals("PRODUCTO_O_SERVICIO_REQUERIDO", ex.getCodigo());
        verify(provider, never()).obtenerComision(any());
        verify(bitacora, never()).registrar(any());
    }

    @Test
    void exito_registraBitacoraOK_yDevuelveComision() {
        Comision esperada = new Comision("POS-1", "PLZ-001", "TND-001", "CAJA-1", "SERV-TAE-ATT",
                new BigDecimal("2.50"), new BigDecimal("12.75"), "MXN", OffsetDateTime.now());
        when(provider.obtenerComision(any())).thenReturn(esperada);

        Comision r = service.consultar(consulta(null, "SERV-TAE-ATT"));

        assertEquals("MXN", r.moneda());
        ArgumentCaptor<RegistroConsumo> cap = ArgumentCaptor.forClass(RegistroConsumo.class);
        verify(bitacora).registrar(cap.capture());
        assertEquals(ResultadoConsulta.OK, cap.getValue().resultado());
    }

    @Test
    void falloProveedor_registraBitacoraFallo_yRelanza() {
        when(provider.obtenerComision(any())).thenThrow(new ComisionProviderException(
                "SPIN_CIRCUIT_OPEN", "circuito abierto", 503, ResultadoConsulta.CIRCUIT_OPEN, null));

        assertThrows(ComisionProviderException.class,
                () -> service.consultar(consulta(null, "SERV-TAE-ATT")));

        ArgumentCaptor<RegistroConsumo> cap = ArgumentCaptor.forClass(RegistroConsumo.class);
        verify(bitacora).registrar(cap.capture());
        assertEquals(ResultadoConsulta.CIRCUIT_OPEN, cap.getValue().resultado());
        assertEquals(503, cap.getValue().httpStatus());
    }
}
