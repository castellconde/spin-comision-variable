package com.oxxo.spin.comisionvariable.adapter.out.spin;

import com.oxxo.spin.comisionvariable.adapter.out.spin.cache.TokenCacheService;
import com.oxxo.spin.comisionvariable.adapter.out.spin.client.SpinAuthClient;
import com.oxxo.spin.comisionvariable.adapter.out.spin.config.SpinConfig;
import com.oxxo.spin.comisionvariable.adapter.out.spin.dto.SpinTokenResponse;
import com.oxxo.spin.comisionvariable.domain.exception.ComisionProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpinAuthServiceTest {

    private SpinAuthClient authClient;
    private SpinConfig spinConfig;
    private TokenCacheService tokenCache;
    private SpinAuthService service;

    @BeforeEach
    void setUp() {
        authClient = mock(SpinAuthClient.class);
        spinConfig = mock(SpinConfig.class);
        tokenCache = mock(TokenCacheService.class);
        when(spinConfig.tokenGrantType()).thenReturn("client_credentials");
        when(spinConfig.clientId()).thenReturn("cid");
        when(spinConfig.clientSecret()).thenReturn("secret");

        service = new SpinAuthService();
        service.authClient = authClient;
        service.spinConfig = spinConfig;
        service.tokenCache = tokenCache;
    }

    @Test
    void bearerToken_usaCacheSiExiste() {
        when(tokenCache.get()).thenReturn(Optional.of("cached"));
        assertEquals("Bearer cached", service.bearerToken());
    }

    @Test
    void bearerToken_renuevaYCacheaSiNoExiste() {
        when(tokenCache.get()).thenReturn(Optional.empty());
        when(authClient.token("client_credentials", "cid", "secret"))
                .thenReturn(new SpinTokenResponse("nuevo", "Bearer", 300));

        assertEquals("Bearer nuevo", service.bearerToken());
        verify(tokenCache).put(eq("nuevo"), any(Duration.class));
    }

    @Test
    void renew_falla_lanzaComisionProviderException() {
        when(tokenCache.get()).thenReturn(Optional.empty());
        when(authClient.token(any(), any(), any())).thenThrow(new RuntimeException("down"));

        ComisionProviderException ex = assertThrows(ComisionProviderException.class, service::bearerToken);
        assertEquals(502, ex.getStatus());
    }
}
