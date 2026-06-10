package com.oxxo.spin.spinmock.domain.port.out;

import com.oxxo.spin.spinmock.domain.model.Escenario;

import java.util.List;
import java.util.Optional;

/** Puerto de salida hacia el almacenamiento de escenarios (PostgreSQL). */
public interface EscenarioRepositoryPort {

    /** Escenarios activos ordenados por prioridad ascendente (para resolver). */
    List<Escenario> activosPorPrioridad();

    List<Escenario> todos();

    Optional<Escenario> porId(Long id);

    Escenario guardar(Escenario escenario);

    Optional<Escenario> actualizar(Long id, Escenario escenario);

    boolean borrar(Long id);
}
