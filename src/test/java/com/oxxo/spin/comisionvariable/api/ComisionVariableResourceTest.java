package com.oxxo.spin.comisionvariable.api;

import com.oxxo.spin.comisionvariable.api.dto.ComisionResponse;
import com.oxxo.spin.comisionvariable.domain.ComisionVariableService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestHTTPEndpoint(ComisionVariableResource.class)
class ComisionVariableResourceTest {

    @InjectMock
    ComisionVariableService service;

    @Test
    void sinAutenticacion_retorna401() {
        given()
                .contentType("application/json")
                .body("{}")
                .when().post("/comision")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "pos-test", roles = {"pos-consumer"})
    void requestInvalido_retorna400ConDetalle() {
        given()
                .contentType("application/json")
                .body("{\"id\":\"x\",\"plaza\":\"\",\"tienda\":\"TND-001\",\"caja\":\"CAJA-1\",\"transaccion\":\"TRX-1\"}")
                .when().post("/comision")
                .then()
                .statusCode(400)
                .body("codigo", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @TestSecurity(user = "pos-test", roles = {"pos-consumer"})
    void sinProductoNiServicio_retorna422() {
        given()
                .contentType("application/json")
                .body("{\"id\":\"POS-1\",\"plaza\":\"PLZ-001\",\"tienda\":\"TND-001\",\"caja\":\"CAJA-1\",\"transaccion\":\"TRX-1\"}")
                .when().post("/comision")
                .then()
                .statusCode(422)
                .body("codigo", equalTo("PRODUCTO_O_SERVICIO_REQUERIDO"));
    }

    @Test
    @TestSecurity(user = "pos-test", roles = {"pos-consumer"})
    void requestValido_retorna200() {
        Mockito.when(service.consultar(Mockito.any())).thenReturn(new ComisionResponse(
                "POS-1", "PLZ-001", "TND-001", "CAJA-1", "SERV-TAE-ATT",
                new BigDecimal("2.50"), new BigDecimal("12.75"), "MXN", OffsetDateTime.now()));

        given()
                .contentType("application/json")
                .body("{\"id\":\"POS-1\",\"plaza\":\"PLZ-001\",\"tienda\":\"TND-001\",\"caja\":\"CAJA-1\",\"transaccion\":\"TRX-1\",\"servicio\":\"SERV-TAE-ATT\"}")
                .when().post("/comision")
                .then()
                .statusCode(200)
                .body("moneda", equalTo("MXN"))
                .body("montoComision", is(12.75f));
    }
}
