# =====================================================================
#  Imagen del dev-container (desarrollo de Comision Variable).
#  Base UBI10-minimal + JDK 21 (alineado al estandar del proyecto).
# =====================================================================
FROM registry.access.redhat.com/ubi10/ubi-minimal:latest

# JDK 21 (devel para compilar) + herramientas de desarrollo
RUN microdnf install -y java-21-openjdk-devel git tar gzip findutils which shadow-utils \
    && microdnf clean all \
    && ln -sf "$(dirname $(dirname $(readlink -f $(which javac))))" /usr/lib/jvm/java-21

ENV JAVA_HOME=/usr/lib/jvm/java-21
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Usuario no root para el dev-container
ARG USERNAME=vscode
ARG USER_UID=1001
RUN useradd --uid ${USER_UID} -m ${USERNAME} 2>/dev/null || true
USER ${USERNAME}
WORKDIR /workspaces/spin-comision-variable
