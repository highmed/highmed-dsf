@echo off

echo highmed/bpe ...
docker build -t highmed/bpe ..\dsf-bpe\dsf-bpe-server-jetty
docker tag highmed/bpe:latest registry:5000/highmed/bpe:latest
docker push registry:5000/highmed/bpe

echo highmed/bpe_proxy ...
docker build -t highmed/bpe_proxy ..\dsf-docker\bpe_proxy
docker tag highmed/bpe_proxy:latest registry:5000/highmed/bpe_proxy:latest
docker push registry:5000/highmed/bpe_proxy

echo highmed/fhir ...
docker build -t highmed/fhir ..\dsf-fhir\dsf-fhir-server-jetty
docker tag highmed/fhir:latest registry:5000/highmed/fhir:latest
docker push registry:5000/highmed/fhir

echo highmed/fhir_proxy ...
docker build -t highmed/fhir_proxy ..\dsf-docker\fhir_proxy
docker tag highmed/fhir_proxy:latest registry:5000/highmed/fhir_proxy:latest
docker push registry:5000/highmed/fhir_proxy