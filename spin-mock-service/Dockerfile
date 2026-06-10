# Multi-stage para build en cluster (OpenShift Docker strategy).
#   oc new-build --strategy=docker --name=spin-mock-service \
#      --context-dir=spin-mock-service \
#      https://github.com/castellconde/spin-comision-variable.git
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q -Dmaven.test.skip=true dependency:go-offline || true
COPY src ./src
RUN mvn -B -Dmaven.test.skip=true clean package

FROM registry.access.redhat.com/ubi10/ubi-minimal:latest
RUN microdnf install -y java-21-openjdk-headless tzdata && microdnf clean all
WORKDIR /deployments
COPY --from=build --chown=185:0 /build/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185:0 /build/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185:0 /build/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185:0 /build/target/quarkus-app/quarkus/ /deployments/quarkus/
RUN chmod -R g+rwX /deployments
EXPOSE 8080
USER 185
ENTRYPOINT ["java","-Dquarkus.http.host=0.0.0.0","-jar","/deployments/quarkus-run.jar"]
