FROM debian:buster-slim AS builder
RUN adduser --system --no-create-home --group --uid 2202 java
WORKDIR /opt/bpe
COPY --chown=root:java ./ ./
RUN chown root:java ./ && \
    chmod 750 ./ ./conf ./lib ./plugin ./process ./dsf_bpe_start.sh && \
	chmod 440 ./conf/jetty.properties ./conf/log4j2.xml ./dsf_bpe.jar ./lib/*.jar && \
	chmod 1775 ./log ./last_event


FROM openjdk:11-jre-slim
LABEL maintainer="hauke.hund@hs-heilbronn.de"

EXPOSE 8080

RUN adduser --system --no-create-home --group --uid 2202 java && \
    apt update && apt dist-upgrade -y

WORKDIR /opt/bpe
COPY --from=builder /opt/bpe ./

USER java
ENTRYPOINT ["./dsf_bpe_start.sh"]
