# HiGHmed Data Sharing Framework (HiGHmed DSF)

[![Build Status](https://travis-ci.org/highmed/highmed-dsf.svg?branch=master)](https://travis-ci.org/highmed/highmed-dsf)

A more detailed documentation on how to start developing and setting-up the testing environment will be added shortly.

## Introduction
An introduction to the HiGHmed data sharing architecture can be found on [YouTube](http://www.youtube.com/watch?v=YPcryul5occ) (German).

## Building
Prerequisite: Java 11, Maven 3.6

```
mvn install
```

## Manual Integration Testing (without Docker)
Prerequisite: Java 11, Maven 3.6, PostgreSQL 11

* Build the entire project from the root directory of the repository
  ```
  mvn install
  ```
* Install PostgreSQL 11 (localhost:5432)
* Add DB User *liquibase_user*
  ``` SQL
  CREATE USER liquibase_user WITH LOGIN NOSUPERUSER INHERIT CREATEDB CREATEROLE NOREPLICATION
  PASSWORD 'fLp6ZSd5QrMAkGZMjxqXjmcWrTfa3Dn8fA57h92Y';
  ```
* Create Databases *fhir* and *bpe* with owner *liquibase_user*
  ``` SQL
  CREATE DATABASE bpe OWNER liquibase_user;
  CREATE DATABASE fhir OWNER liquibase_user;
  ```
* Start *org.highmed.dsf.fhir.FhirJettyServerHttps* from your IDE with execution folder: *.../highmed-dsf/dsf-fhir/dsf-fhir-server-jetty*
* Start *org.highmed.dsf.bpe.BpeJettyServerHttps* from your IDE with execition folder: *.../highmed-dsf/dsf-bpe/dsf-bpe-server-jetty*
* To access the FHIR endpoint (https://localhost:8001/fhir/...) and BPE rest interface (https://localhost:8002/bpe/...) via WebBrowser install *.../highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12* (Password: *password*) in your browsers certifiate store. The p12 file includes a client certificate for "Webbrowser Test User" and the "Test CA" certificate. All private-keys and certificates including the Test CA are generated during the maven build and are private to your machine. Make sure to protect the CA private-key at *.../highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/ca/testca_private-key.pem* from third-party access if you have installed the Test CA certificate in your certificate store.

## Manual Integration Testing (local with Docker)
Prerequisite: Java 11, Maven 3.6, Docker 18

* Build the entire project from the root directory of this repository
  ```
  mvn install
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
    docker-compose (-f docker-compose.yml -f docker-compose.prod.yml) up
    ```
  * To start the BPE server execute in the .../dsf-docker-test-setup/bpe folder
    ```
    docker-compose (-f docker-compose.yml -f docker-compose.prod.yml) up
    ```
* To access the FHIR endpoint (https://localhost/fhir/...) and BPE rest interface (https://localhost:8443/bpe/...) via WebBrowser install *.../highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12* (Password: *password*) in your browsers certifiate store. The p12 file includes a client certificate for "Webbrowser Test User" and the "Test CA" certificate. All private-keys and certificates including the Test CA are generated during the maven build and are private to your machine. Make sure to protect the CA private-key at *.../highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/ca/testca_private-key.pem* from third-party access if you have installed the Test CA certificate in your certificate store.

### Troubleshooting
* If you run in docker network troubles, clean up your network using ``docker network ls -q | xargs docker network rm``
* In production: if the warning _Service "db" is using volume "/var/lib/postgresql/data" from the previous container_ appears, run the command 
  ``` 
  docker-compose down -v 
  ```
  before starting the containers.
  
  
## Manual Integration Testing (VMs for Docker-Registry, 3 MeDICs, TTP)
* For hints on setting up 5 Ubuntu VMs with Docker running on a Windows 10 host with Hyper-V and Ansible see [test_setup.txt](dsf-docker-test-setup-3medic-ttp/test_setup.txt)
