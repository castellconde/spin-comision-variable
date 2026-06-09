package com.oxxo.spin.comisionvariable.adapter.in.rest.exception;

import com.oxxo.spin.comisionvariable.adapter.in.rest.dto.ErrorResponse;
import com.oxxo.spin.comisionvariable.domain.exception.BusinessException;
import com.oxxo.spin.comisionvariable.domain.exception.ComisionProviderException;
import com.oxxo.spin.comisionvariable.domain.model.ResultadoConsulta;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExceptionMappersTest {

    private final ExceptionMappers mappers = new ExceptionMappers();

    @Test
    void mapBusiness_devuelveStatusYCodigo() {
        RestResponse<ErrorResponse> r = mappers.mapBusiness(
                new BusinessException("PRODUCTO_O_SERVICIO_REQUERIDO", "falta", 422));
        assertEquals(422, r.getStatus());
        assertEquals("PRODUCTO_O_SERVICIO_REQUERIDO", r.getEntity().codigo());
    }

    @Test
    void mapProvider_devuelveStatusDelProveedor() {
        RestResponse<ErrorResponse> r = mappers.mapProvider(new ComisionProviderException(
                "SPIN_CIRCUIT_OPEN", "abierto", 503, ResultadoConsulta.CIRCUIT_OPEN, null));
        assertEquals(503, r.getStatus());
        assertEquals("SPIN_CIRCUIT_OPEN", r.getEntity().codigo());
    }

    @Test
    void mapGeneric_devuelve500() {
        RestResponse<ErrorResponse> r = mappers.mapGeneric(new RuntimeException("boom"));
        assertEquals(500, r.getStatus());
        assertEquals("INTERNAL_ERROR", r.getEntity().codigo());
    }

    @Test
    void mapValidation_devuelve400ConDetalle() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> cv = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("consultar.request.plaza");
        when(cv.getPropertyPath()).thenReturn(path);
        when(cv.getMessage()).thenReturn("El campo 'plaza' es obligatorio");

        RestResponse<ErrorResponse> r = mappers.mapValidation(
                new ConstraintViolationException(Set.of(cv)));

        assertEquals(400, r.getStatus());
        assertEquals("VALIDATION_ERROR", r.getEntity().codigo());
        assertEquals("plaza", r.getEntity().errores().get(0).campo());
    }
}
