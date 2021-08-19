FROM httpd:2.4-alpine
LABEL maintainer="hauke.hund@hs-heilbronn.de"

COPY ./conf/ /usr/local/apache2/conf/
RUN mkdir /usr/local/apache2/ssl/ && \
    chown daemon:daemon /usr/local/apache2/ssl/ && \
    chmod 440 /usr/local/apache2/ssl/ && \
    chmod 644 /usr/local/apache2/conf/httpd.conf /usr/local/apache2/conf/extra/host.conf /usr/local/apache2/conf/extra/host-ssl.conf /usr/local/apache2/conf/extra/httpd-ssl.conf