FROM httpd:2.4
MAINTAINER Hauke Hund <hauke.hund@hs-heilbronn.de>

COPY ./conf/ /usr/local/apache2/conf/
RUN mkdir /usr/local/apache2/ssl/ && chown daemon:daemon /usr/local/apache2/ssl/ && chmod 440 /usr/local/apache2/ssl/
VOLUME /usr/local/apache2/ssl/