# HiGHmed Data Sharing Framework (HiGHmed DSF)

[![Build Status](https://travis-ci.org/highmed/highmed-dsf.svg?branch=master)](https://travis-ci.org/highmed/highmed-dsf)

The HiGHmed Data Sharing Framework (HiGHmed DSF) implements a distributed process engine based on the BPMN 2.0 and FHIR R4 standards.  Within the HiGHmed medical informatics consortium, the DSF is used to support biomedical research with routine data. Every participating site runs a FHIR endpoint (dsf-fhir) accessible by other sites and a business process engine (dsf-bpe) in the local secured network. Authentication between sites is handled using X.509 client/server certificates. The process engines execute BPMN processes in order to coordinate local and remote steps necessary to enable cross-site data sharing and feasibility analyses. This includes access to local data repositories, use-and-access-committee decision support, consent filtering, and privacy preserving record-linkage and pseudonymization.

## Introduction
An introduction to the HiGHmed data sharing architecture can be found on [YouTube](http://www.youtube.com/watch?v=YPcryul5occ) (German).

## Wiki
The Wiki with the full documentation can be found [here](https://github.com/highmed/highmed-dsf/wiki).

## Manual Integration Testing (local with Docker)
Prerequisite: Java 11, Maven 3.6, Docker 18

* Build the entire project from the root directory of this repository
  ```
  mvn clean install
  ```
* Build docker images
  * Windows: in the .../dsf-docker-test-setup folder execute
    ```
    docker-build.bat
    ```
  * Unix/Linux: in the .../dsf-docker-test-setup folder execute
    ```
    docker-build.sh
    ```
* Start docker containers
  * To start the FHIR server execute in the .../dsf-docker-test-setup/fhir folder
    ```
    docker-compose up
    ```
  * To start the BPE server execute in the .../dsf-docker-test-setup/bpe folder
    ```
    docker-compose up
    ```
* To access the FHIR endpoint (https://localhost/fhir/...) and BPE rest interface (https://localhost:8443/bpe/...) via WebBrowser install *.../highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12* (Password: *password*) in your browsers certifiate store. The p12 file includes a client certificate for "Webbrowser Test User" and the "Test CA" certificate. All private-keys and certificates including the Test CA are generated during the maven build and are private to your machine. Make sure to protect the CA private-key at *.../highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/ca/testca_private-key.pem* from third-party access if you have installed the Test CA certificate in your certificate store.
