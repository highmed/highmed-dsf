#!/bin/bash

echo highmed/bpe ...
docker build --pull -t highmed/bpe ../dsf-bpe/dsf-bpe-server-jetty/docker

echo highmed/fhir ...
docker build --pull -t highmed/fhir ../dsf-fhir/dsf-fhir-server-jetty/docker

echo highmed/fhir_proxy ...
docker build --pull -t highmed/fhir_proxy ../dsf-docker/fhir_proxy
