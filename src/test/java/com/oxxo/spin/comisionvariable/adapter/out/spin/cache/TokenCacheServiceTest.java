package com.oxxo.spin.comisionvariable.adapter.out.spin.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba del cache en MEMORIA (Redis deshabilitado): el servicio debe degradar
 * de forma elegante sin Redis.
 */
class TokenCacheServiceTest {

    @Test
    void getVacioInicialmente() {
        TokenCacheService cache = new TokenCacheService();
        assertTrue(cache.get().isEmpty());
    }

    @Test
    void putYGet_devuelveTokenVigente() {
        TokenCacheService cache = new TokenCacheService();
        cache.put("tok", Duration.ofSeconds(60));
        assertTrue(cache.get().isPresent());
        assertEquals("tok", cache.get().get());
    }

    @Test
    void tokenExpirado_noSeDevuelve() {
        TokenCacheService cache = new TokenCacheService();
        cache.put("tok", Duration.ZERO);
        assertFalse(cache.get().isPresent());
    }

    @Test
    void invalidate_limpiaToken() {
        TokenCacheService cache = new TokenCacheService();
        cache.put("tok", Duration.ofSeconds(60));
        cache.invalidate();
        assertTrue(cache.get().isEmpty());
    }
}
