# HiGHmed Data Sharing Framework (HiGHmed DSF)

Documentation on how to setup the development and testing enviornment will be added shortly.

## Building
```
mvn install
```

## Manual Integration Testing (without Docker)
* Build Project
* Installl PostgreSQL 11
* Add DB User *liquibase_user* SQL: *CREATE USER liquibase_user WITH LOGIN NOSUPERUSER INHERIT CREATEDB CREATEROLE NOREPLICATION;* Password: fLp6ZSd5QrMAkGZMjxqXjmcWrTfa3Dn8fA57h92Y
* Create Databases *fhir* and *bpe* owner *liquibase_user*
* Start *org.highmed.dsf.fhir.FhirJettyServerHttps* from your IDE with execution folder: *.../highmed-dsf/dsf-fhir/dsf-fhir-server-jetty*
* Start *org.highmed.dsf.bpe.BpeJettyServerHttps* from your IDE with execition folder: *.../highmed-dsf/dsf-bpe/dsf-bpe-server-jetty*
* To access the FHIR endpoint (https://localhost:8001/fhir) and BPE rest interface (https://localhost:8002/bpe) via WebBrowser install *.../highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12* (Password: *password*) in your browsers certifiate store. The p12 file includes a Client Certificate for "Webbrowser Test User" and the "Test CA" Certificate. All private-keys and certificates including the Test CA are generated during the maven build and are private to your machine. Make shure to protect the CA private-key at *.../highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/ca/testca_private-key.pem* from third-party access if you have installed the Test CA Certificate in your certificate store.

## Manual Integration Testing (local with Docker)
* TODO

## Manual Integration Testing (VMs 3x MeDIC + TTP)
* TODO
