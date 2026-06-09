package com.oxxo.spin.comisionvariable.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PartitionMaintenanceJobTest {

    @Test
    void ejecutar_invocaCrearYPurgarParticiones() {
        EntityManager em = mock(EntityManager.class);
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), anyInt())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(0);

        PartitionMaintenanceJob job = new PartitionMaintenanceJob();
        job.em = em;
        job.particionesFuturas = 12;
        job.retencionMeses = 6;

        job.ejecutar();

        verify(em).createNativeQuery("SELECT crear_particiones_futuras(:m)");
        verify(em).createNativeQuery("SELECT purgar_particiones_antiguas(:m)");
    }

    @Test
    void ejecutar_nuncaPropagaErrores() {
        EntityManager em = mock(EntityManager.class);
        when(em.createNativeQuery(anyString())).thenThrow(new RuntimeException("db down"));
        PartitionMaintenanceJob job = new PartitionMaintenanceJob();
        job.em = em;
        assertDoesNotThrow(job::ejecutar);
    }

    @Test
    void skipPredicate_respetaFlagHabilitado() {
        PartitionMaintenanceJob.MantenimientoDeshabilitado pred = new PartitionMaintenanceJob.MantenimientoDeshabilitado();
        pred.enabled = true;
        assertFalse(pred.test(null));   // habilitado -> NO se salta
        pred.enabled = false;
        assertTrue(pred.test(null));    // deshabilitado -> se salta
    }
}
