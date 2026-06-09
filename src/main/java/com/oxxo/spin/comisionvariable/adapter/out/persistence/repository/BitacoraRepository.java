package com.oxxo.spin.comisionvariable.adapter.out.persistence.repository;

import com.oxxo.spin.comisionvariable.adapter.out.persistence.entity.BitacoraConsumo;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BitacoraRepository implements PanacheRepository<BitacoraConsumo> {
}
