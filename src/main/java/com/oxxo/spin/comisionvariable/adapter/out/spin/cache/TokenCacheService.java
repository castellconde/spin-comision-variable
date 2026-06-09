package com.oxxo.spin.comisionvariable.adapter.out.spin.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Cache OPCIONAL del token de Spin. Si Redis esta habilitado y disponible, el
 * token se comparte entre replicas; si no, degrada de forma elegante a cache en
 * memoria por instancia. Una falla de Redis nunca rompe el servicio.
 */
@ApplicationScoped
public class TokenCacheService {

    private static final Logger LOG = Logger.getLogger(TokenCacheService.class);
    private static final String KEY = "spin:oauth:token";

    @ConfigProperty(name = "cache.redis.enabled", defaultValue = "false")
    boolean redisEnabled;

    @Inject
    RedisDataSource redisDataSource;

    private ValueCommands<String, String> redis;

    private final AtomicReference<String> memToken = new AtomicReference<>();
    private volatile Instant memExpiry = Instant.EPOCH;

    @PostConstruct
    void init() {
        if (redisEnabled) {
            try {
                redis = redisDataSource.value(String.class);
                LOG.info("Cache de token: Redis habilitado");
            } catch (Exception e) {
                LOG.warnf("Redis no inicializable, se usara cache en memoria: %s", e.getMessage());
            }
        } else {
            LOG.info("Cache de token: en memoria (Redis deshabilitado)");
        }
    }

    public Optional<String> get() {
        if (redis != null) {
            try {
                return Optional.ofNullable(redis.get(KEY));
            } catch (Exception e) {
                LOG.warnf("Fallo lectura de Redis, fallback a memoria: %s", e.getMessage());
            }
        }
        if (memToken.get() != null && Instant.now().isBefore(memExpiry)) {
            return Optional.of(memToken.get());
        }
        return Optional.empty();
    }

    public void put(String token, Duration ttl) {
        if (redis != null) {
            try {
                redis.set(KEY, token, new SetArgs().ex(ttl.getSeconds()));
                return;
            } catch (Exception e) {
                LOG.warnf("Fallo escritura en Redis, fallback a memoria: %s", e.getMessage());
            }
        }
        memToken.set(token);
        memExpiry = Instant.now().plus(ttl);
    }

    public void invalidate() {
        memToken.set(null);
        memExpiry = Instant.EPOCH;
        if (redis != null) {
            try {
                redis.getdel(KEY);
            } catch (Exception e) {
                LOG.debugf("No se pudo invalidar token en Redis: %s", e.getMessage());
            }
        }
    }
}
