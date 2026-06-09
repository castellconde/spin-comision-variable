package com.oxxo.spin.comisionvariable.adapter.out.spin;

import com.oxxo.spin.comisionvariable.adapter.out.spin.client.SpinApiClient;
import com.oxxo.spin.comisionvariable.adapter.out.spin.config.SpinConfig;
import com.oxxo.spin.comisionvariable.adapter.out.spin.dto.SpinComisionResponse;
import com.oxxo.spin.comisionvariable.domain.exception.ComisionProviderException;
import com.oxxo.spin.comisionvariable.domain.model.Comision;
import com.oxxo.spin.comisionvariable.domain.model.Consulta;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Prueba unitaria del adaptador Spin instanciado directamente (las anotaciones
 * de fault tolerance son inertes fuera del proxy CDI): valida el mapeo y la
 * traduccion de errores HTTP a excepciones de dominio.
 */
class SpinComisionAdapterTest {

    private SpinApiClient apiClient;
    private SpinAuthService authService;
    private SpinConfig spinConfig;
    private SpinComisionAdapter adapter;

    private final Consulta consulta = new Consulta("POS-1", "PLZ", "TND", "CAJA", "TRX", null, "SERV");

    @BeforeEach
    void setUp() {
        apiClient = mock(SpinApiClient.class);
        authService = mock(SpinAuthService.class);
        spinConfig = mock(SpinConfig.class);
        when(authService.bearerToken()).thenReturn("Bearer tok");
        when(spinConfig.apiKey()).thenReturn("api-key");

        adapter = new SpinComisionAdapter();
        adapter.apiClient = apiClient;
        adapter.authService = authService;
        adapter.spinConfig = spinConfig;
    }

    @Test
    void exito_mapeaRespuestaADominio() {
        when(apiClient.calcularComision(eq("Bearer tok"), eq("api-key"), any())).thenReturn(
                new SpinComisionResponse("POS-1", "SERV", new BigDecimal("2.5"),
                        new BigDecimal("12.75"), "MXN", "CALCULADA"));

        Comision c = adapter.obtenerComision(consulta);

        assertEquals("MXN", c.moneda());
        assertEquals("SERV", c.concepto());
        assertEquals(new BigDecimal("12.75"), c.montoComision());
    }

    @Test
    void http401_invalidaTokenYLanzaProviderException() {
        when(apiClient.calcularComision(any(), any(), any()))
                .thenThrow(new WebApplicationException(Response.status(401).build()));

        ComisionProviderException ex = assertThrows(ComisionProviderException.class,
                () -> adapter.obtenerComision(consulta));

        assertEquals(401, ex.getStatus());
        verify(authService, times(1)).invalidate();
    }

    @Test
    void http500_seTraduceA502() {
        when(apiClient.calcularComision(any(), any(), any()))
                .thenThrow(new WebApplicationException(Response.status(500).build()));

        ComisionProviderException ex = assertThrows(ComisionProviderException.class,
                () -> adapter.obtenerComision(consulta));

        assertEquals(502, ex.getStatus());
    }
}
