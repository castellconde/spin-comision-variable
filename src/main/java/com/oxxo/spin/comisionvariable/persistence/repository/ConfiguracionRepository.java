package com.oxxo.spin.comisionvariable.persistence.repository;

import com.oxxo.spin.comisionvariable.persistence.entity.ConfiguracionServicio;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class ConfiguracionRepository implements PanacheRepositoryBase<ConfiguracionServicio, String> {

    public Optional<String> valor(String clave) {
        return find("clave", clave).firstResultOptional().map(c -> c.valor);
    }
}
