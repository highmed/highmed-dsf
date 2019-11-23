FROM openjdk:11-slim
MAINTAINER Hauke Hund <hauke.hund@hs-heilbronn.de>

EXPOSE 8080

RUN adduser --system --no-create-home --group --uid 2101 java
WORKDIR /opt/fhir
COPY --chown=root:java ./ ./
RUN chown root:java ./ && chmod 750 ./ ./lib ./dsf_fhir_start.sh && chmod 640 ./dsf_fhir.jar ./lib/*.jar && chmod 1775 ./log

USER java
ENTRYPOINT ["./dsf_fhir_start.sh"]
