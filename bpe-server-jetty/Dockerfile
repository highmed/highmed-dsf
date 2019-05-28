FROM openjdk:11-jre
MAINTAINER Hauke Hund <hauke.hund@hs-heilbronn.de>

EXPOSE 8080

RUN mkdir /opt/bpe
RUN mkdir /opt/bpe/conf

VOLUME /opt/bpe/conf/

WORKDIR /opt/bpe

COPY target/lib ./lib
COPY target/bpe_demo.jar ./bpe_demo.jar
RUN mkdir ./log
RUN mkdir ./last_event

RUN adduser --system --no-create-home --group java
RUN chown root:java ./ ./lib ./log ./last_event ./bpe_demo.jar ./lib/*.jar
RUN chmod 750 ./ ./lib
RUN chmod 640 ./bpe_demo.jar ./lib/*.jar
RUN chmod 1775 ./log ./last_event	

USER java
ENTRYPOINT ["/usr/bin/java", "-cp", "lib/*:bpe_demo.jar", "org.highmed.bpe.BpeJettyServer"]