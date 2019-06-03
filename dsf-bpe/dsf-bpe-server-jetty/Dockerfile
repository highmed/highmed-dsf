FROM openjdk:11-jre
MAINTAINER Hauke Hund <hauke.hund@hs-heilbronn.de>

EXPOSE 8080

RUN mkdir /opt/bpe
RUN mkdir /opt/bpe/conf

VOLUME /opt/bpe/conf/

WORKDIR /opt/bpe

COPY target/lib ./lib
COPY target/highmed_bpe.jar ./highmed_bpe.jar
RUN mkdir ./log
RUN mkdir ./last_event

RUN adduser --system --no-create-home --group java
RUN chown root:java ./ ./lib ./log ./last_event ./highmed_bpe.jar ./lib/*.jar
RUN chmod 750 ./ ./lib
RUN chmod 640 ./highmed_bpe.jar ./lib/*.jar
RUN chmod 1775 ./log ./last_event	

USER java
ENTRYPOINT ["/usr/bin/java", "-cp", "lib/*:highmed_bpe.jar", "org.highmed.dsf.bpe.BpeJettyServer"]