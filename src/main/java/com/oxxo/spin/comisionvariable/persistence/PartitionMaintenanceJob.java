package com.oxxo.spin.comisionvariable.persistence;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Mantenimiento programado de la bitacora particionada:
 *  - asegura particiones futuras (default 12 meses)
 *  - purga particiones antiguas (default retencion 6 meses)
 *
 * Se ejecuta una vez al dia (configurable). Desactivable con
 * bitacora.mantenimiento.enabled=false.
 */
@ApplicationScoped
public class PartitionMaintenanceJob {

    private static final Logger LOG = Logger.getLogger(PartitionMaintenanceJob.class);

    @Inject
    EntityManager em;

    @ConfigProperty(name = "bitacora.particiones-futuras", defaultValue = "12")
    int particionesFuturas;

    @ConfigProperty(name = "bitacora.retencion-meses", defaultValue = "6")
    int retencionMeses;

    @Scheduled(cron = "{bitacora.mantenimiento.cron:0 30 2 * * ?}",
            skipExecutionIf = MantenimientoDeshabilitado.class)
    @Transactional
    void ejecutar() {
        try {
            em.createNativeQuery("SELECT crear_particiones_futuras(:m)")
                    .setParameter("m", particionesFuturas).getSingleResult();
            em.createNativeQuery("SELECT purgar_particiones_antiguas(:m)")
                    .setParameter("m", retencionMeses).getSingleResult();
            LOG.infof("Mantenimiento de particiones OK (futuras=%d, retencion=%d meses)",
                    particionesFuturas, retencionMeses);
        } catch (Exception e) {
            LOG.errorf(e, "Fallo en mantenimiento de particiones");
        }
    }

    /** Permite desactivar el job via config. */
    @ApplicationScoped
    static class MantenimientoDeshabilitado implements Scheduled.SkipPredicate {
        @ConfigProperty(name = "bitacora.mantenimiento.enabled", defaultValue = "true")
        boolean enabled;

        @Override
        public boolean test(io.quarkus.scheduler.ScheduledExecution execution) {
            return !enabled;
        }
    }
}
