FROM debian:buster-slim AS builder
RUN adduser --system --no-create-home --group --uid 2101 java
WORKDIR /opt/fhir
COPY --chown=root:java ./ ./
RUN chown root:java ./ && \
    chmod 750 ./ ./conf ./lib ./dsf_fhir_start.sh && \
	chmod 440 ./conf/jetty.properties ./conf/log4j2.xml ./dsf_fhir.jar ./lib/*.jar && \
	chmod 1775 ./log


FROM openjdk:11-jre-slim
LABEL maintainer="hauke.hund@hs-heilbronn.de"

EXPOSE 8080

RUN adduser --system --no-create-home --group --uid 2101 java && apt update && \
    apt dist-upgrade -y

WORKDIR /opt/fhir
COPY --from=builder /opt/fhir ./

USER java
ENTRYPOINT ["./dsf_fhir_start.sh"]
