# =====================================================================
#  Dockerfile MULTI-STAGE para build EN EL CLUSTER (OpenShift Docker
#  strategy). Compila con Maven/JDK21 y ejecuta el fast-jar sobre
#  UBI10-minimal + Java 21. No requiere artefactos previos en git.
#
#  Build en OpenShift (el Dockerfile está en la raíz del repo):
#    oc new-build --strategy=docker --name=comision-variable \
#       https://github.com/castellconde/spin-comision-variable.git
# =====================================================================

# ---- Etapa 1: build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
# Cache de dependencias
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline || true
# Código y empaquetado (fast-jar, sin tests para acelerar el build en cluster)
COPY src ./src
RUN mvn -B -DskipTests clean package

# ---- Etapa 2: runtime ----
FROM registry.access.redhat.com/ubi10/ubi-minimal:latest
ENV LANGUAGE='en_US:en'
RUN microdnf install -y java-21-openjdk-headless tzdata && microdnf clean all

WORKDIR /deployments
# Estructura fast-jar de Quarkus, con permisos de grupo root (OpenShift usa UID aleatorio)
COPY --from=build --chown=185:0 /build/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185:0 /build/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185:0 /build/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185:0 /build/target/quarkus-app/quarkus/ /deployments/quarkus/
RUN chmod -R g+rwX /deployments

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENTRYPOINT ["java", "-Dquarkus.http.host=0.0.0.0", "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", "-jar", "/deployments/quarkus-run.jar"]
