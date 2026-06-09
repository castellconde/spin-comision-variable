# Integración con CyberArk Conjur (vía variables de ambiente)

El microservicio **no contiene secretos**. Todos los valores sensibles se leen
de variables de ambiente (ver `application.properties`, sintaxis `${VAR:default}`):

| Variable                | Secreto en Conjur                          |
|-------------------------|--------------------------------------------|
| `DB_USERNAME`           | `secrets/comision-variable/db-username`    |
| `DB_PASSWORD`           | `secrets/comision-variable/db-password`    |
| `KEYCLOAK_CLIENT_SECRET`| `secrets/comision-variable/kc-secret`      |
| `SPIN_CLIENT_ID`        | `secrets/comision-variable/spin-client-id` |
| `SPIN_CLIENT_SECRET`    | `secrets/comision-variable/spin-secret`    |
| `SPIN_API_KEY`          | `secrets/comision-variable/spin-api-key`   |

## Mecanismo recomendado en OpenShift (ROSA / ARO)

**CyberArk Secrets Provider for OpenShift** (sidecar o job init) materializa los
secretos de Conjur en un `Secret` de Kubernetes (`comision-variable-secrets`),
que el Deployment consume con `envFrom.secretRef`. El pod nunca ve el master key
de Conjur: se autentica vía **Conjur Kubernetes Authenticator (authn-k8s)** con
su ServiceAccount.

Flujo:

1. El `ServiceAccount` del pod se registra como host en Conjur (`authn-k8s`).
2. Secrets Provider arranca, se autentica y descarga los secretos de la política
   `secrets/comision-variable`.
3. Escribe/actualiza el `Secret` `comision-variable-secrets`.
4. El contenedor de la app levanta con las variables ya inyectadas.

> En el piloto local, estas variables se definen en `docker-compose.yml`
> (perfil `full`) o en el `.env` del dev-container; en producción las provee
> Conjur. El código es idéntico en ambos casos: solo cambian las variables.
