# Mock del API Spin (WireMock)

Simula el proveedor Spin para el piloto local.

- `POST /oauth/token` -> devuelve un access_token mock (expires_in=300).
- `POST /v1/comisiones` -> devuelve una comision calculada (requiere Authorization + x-api-key).
- Enviar `transaccion: "FORCE-ERROR"` -> responde HTTP 500 para probar reintentos y circuit breaker.

Levantar solo el mock:
    docker compose up -d spin-mock
