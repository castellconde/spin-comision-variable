package com.oxxo.spin.spinmock.application;

import com.oxxo.spin.spinmock.domain.model.Escenario;
import com.oxxo.spin.spinmock.domain.port.in.AdministrarEscenariosUseCase;
import com.oxxo.spin.spinmock.domain.port.out.EscenarioRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

/** CRUD de escenarios: orquesta el puerto de salida. */
@ApplicationScoped
public class AdministrarEscenariosService implements AdministrarEscenariosUseCase {

    private final EscenarioRepositoryPort repositorio;

    @Inject
    public AdministrarEscenariosService(EscenarioRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public List<Escenario> listar() {
        return repositorio.todos();
    }

    @Override
    public Optional<Escenario> porId(Long id) {
        return repositorio.porId(id);
    }

    @Override
    public Escenario crear(Escenario escenario) {
        return repositorio.guardar(escenario);
    }

    @Override
    public Optional<Escenario> actualizar(Long id, Escenario escenario) {
        return repositorio.actualizar(id, escenario);
    }

    @Override
    public boolean borrar(Long id) {
        return repositorio.borrar(id);
    }
}
