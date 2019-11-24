FROM openjdk:11-slim
MAINTAINER Hauke Hund <hauke.hund@hs-heilbronn.de>

EXPOSE 8080

RUN adduser --system --no-create-home --group --uid 2202 java
WORKDIR /opt/bpe
COPY --chown=root:java ./ ./
RUN chown root:java ./ && chmod 750 ./ ./lib ./plugin ./dsf_bpe_start.sh && chmod 640 ./dsf_bpe.jar ./lib/*.jar && chmod 1775 ./log ./last_event

USER java
ENTRYPOINT ["./dsf_bpe_start.sh"]
