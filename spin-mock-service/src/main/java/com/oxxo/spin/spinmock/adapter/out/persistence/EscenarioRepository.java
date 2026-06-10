package com.oxxo.spin.spinmock.adapter.out.persistence;

import com.oxxo.spin.spinmock.domain.model.Escenario;
import com.oxxo.spin.spinmock.domain.port.out.EscenarioRepositoryPort;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/** Adaptador de salida: persistencia de escenarios con Panache. */
@ApplicationScoped
public class EscenarioRepository implements PanacheRepository<EscenarioEntity>, EscenarioRepositoryPort {

    @Override
    public List<Escenario> activosPorPrioridad() {
        return find("activo = true order by prioridad asc, id asc").list()
                .stream().map(EscenarioMapper::toDomain).toList();
    }

    @Override
    public List<Escenario> todos() {
        return findAll().list().stream().map(EscenarioMapper::toDomain).toList();
    }

    @Override
    public Optional<Escenario> porId(Long id) {
        return findByIdOptional(id).map(EscenarioMapper::toDomain);
    }

    @Override
    @Transactional
    public Escenario guardar(Escenario escenario) {
        EscenarioEntity e = new EscenarioEntity();
        EscenarioMapper.copyToEntity(escenario, e);
        e.creadoEn = OffsetDateTime.now();
        e.actualizadoEn = e.creadoEn;
        persist(e);
        return EscenarioMapper.toDomain(e);
    }

    @Override
    @Transactional
    public Optional<Escenario> actualizar(Long id, Escenario escenario) {
        EscenarioEntity e = findById(id);
        if (e == null) {
            return Optional.empty();
        }
        EscenarioMapper.copyToEntity(escenario, e);
        e.actualizadoEn = OffsetDateTime.now();
        return Optional.of(EscenarioMapper.toDomain(e));
    }

    @Override
    @Transactional
    public boolean borrar(Long id) {
        return deleteById(id);
    }
}
