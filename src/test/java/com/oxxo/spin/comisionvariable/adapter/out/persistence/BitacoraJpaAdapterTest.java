package com.oxxo.spin.comisionvariable.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oxxo.spin.comisionvariable.adapter.out.persistence.entity.BitacoraConsumo;
import com.oxxo.spin.comisionvariable.adapter.out.persistence.repository.BitacoraRepository;
import com.oxxo.spin.comisionvariable.domain.model.Consulta;
import com.oxxo.spin.comisionvariable.domain.model.RegistroConsumo;
import com.oxxo.spin.comisionvariable.domain.model.ResultadoConsulta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BitacoraJpaAdapterTest {

    private BitacoraRepository repository;
    private BitacoraJpaAdapter adapter;

    private final Consulta consulta = new Consulta("POS-1", "PLZ", "TND", "CAJA", "TRX", null, "SERV");

    @BeforeEach
    void setUp() {
        repository = mock(BitacoraRepository.class);
        adapter = new BitacoraJpaAdapter();
        adapter.repository = repository;
        adapter.mapper = new ObjectMapper();
    }

    @Test
    void registrar_mapeaYPersisteEntidad() {
        RegistroConsumo r = RegistroConsumo.fallo(consulta, ResultadoConsulta.TIMEOUT,
                "SPIN_TIMEOUT", 504, "timeout", 6000);

        adapter.registrar(r);

        ArgumentCaptor<BitacoraConsumo> cap = ArgumentCaptor.forClass(BitacoraConsumo.class);
        verify(repository).persist(cap.capture());
        BitacoraConsumo b = cap.getValue();
        assertEquals("POS-1", b.consultaId);
        assertEquals("PLZ", b.plaza);
        assertEquals("TRX", b.transaccion);
        assertEquals("TIMEOUT", b.resultado);
        assertEquals(504, b.httpStatus);
        assertEquals(6000L, b.latenciaMs);
        assertNotNull(b.requestPayload);
        assertNotNull(b.creadoEn);
    }

    @Test
    void registrar_nuncaPropagaErrores() {
        doThrow(new RuntimeException("db down")).when(repository).persist(any(BitacoraConsumo.class));
        RegistroConsumo r = RegistroConsumo.fallo(consulta, ResultadoConsulta.ERROR, "X", 502, "m", 1);
        assertDoesNotThrow(() -> adapter.registrar(r));
    }
}
