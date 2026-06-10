package com.oxxo.spin.spinmock.domain.port.in;

import com.oxxo.spin.spinmock.domain.model.Escenario;

import java.util.List;
import java.util.Optional;

/** Caso de uso: CRUD de escenarios. */
public interface AdministrarEscenariosUseCase {
    List<Escenario> listar();

    Optional<Escenario> porId(Long id);

    Escenario crear(Escenario escenario);

    Optional<Escenario> actualizar(Long id, Escenario escenario);

    boolean borrar(Long id);
}
