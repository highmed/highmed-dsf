FROM openjdk:11-jre
MAINTAINER Hauke Hund <hauke.hund@hs-heilbronn.de>

EXPOSE 8080

RUN mkdir /opt/fhir
RUN mkdir /opt/fhir/conf

VOLUME /opt/fhir/conf/

WORKDIR /opt/fhir

COPY target/lib ./lib
COPY target/highmed_fhir.jar ./highmed_fhir.jar
RUN mkdir ./log

RUN adduser --system --no-create-home --group java
RUN chown root:java ./ ./lib ./log ./highmed_fhir.jar ./lib/*.jar
RUN chmod 750 ./ ./lib
RUN chmod 640 ./highmed_fhir.jar ./lib/*.jar
RUN chmod 1775 ./log	

USER java
ENTRYPOINT ["/usr/bin/java", "-cp", "lib/*:highmed_fhir.jar", "org.highmed.dsf.fhir.FhirJettyServer"]