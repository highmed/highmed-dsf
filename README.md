## Deprecation Notice

This repository is no longer maintained. Development of the Data Sharing Framework (DSF) has been moved to [datasharingframework/dsf](https://github.com/datasharingframework/dsf)

Further information, including instructions on how to update to the latest version, can be found on the new project website: https://dsf.dev

We would like to thank everyone who has contributed to this archived version with code contributions, issues or comments.

---

# HiGHmed Data Sharing Framework (HiGHmed DSF)

[![Java CI with Maven status](https://github.com/highmed/highmed-dsf/workflows/Java%20CI%20Build%20with%20Maven/badge.svg)](https://github.com/highmed/highmed-dsf/actions?query=workflow%3A"Java+CI+Build+with+Maven")

The HiGHmed Data Sharing Framework (HiGHmed DSF) implements a distributed process engine based on the BPMN 2.0 and FHIR R4 standards.  Within the HiGHmed medical informatics consortium, the DSF is used to support biomedical research with routine data. Every participating site runs a FHIR endpoint (dsf-fhir) accessible by other sites and a business process engine (dsf-bpe) in the local secured network. Authentication between sites is handled using X.509 client/server certificates. The process engines execute BPMN processes in order to coordinate local and remote steps necessary to enable cross-site data sharing and feasibility analyses. This includes access to local data repositories, use-and-access-committee decision support, consent filtering, and privacy preserving record-linkage and pseudonymization.

## Development
Branching follows the git-flow model, for the latest development version see branch [develop](https://github.com/highmed/highmed-dsf/tree/develop).

## License
All code from the HiGHmed Data Sharing Framework is published under the [Apache-2.0 License](LICENSE).

## Wiki
A full documentation can be found in the [Wiki](https://github.com/highmed/highmed-dsf/wiki).

## Manual Integration Testing (local with Docker)
Prerequisite: Java 11, Maven >= 3.6, Docker >= 18

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
* To access the FHIR endpoint (https://fhir/fhir/...) add a `127.0.0.1 fhir` entry to your local `hosts` file and install *.../highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12* (Password: *password*) in your web browsers certifiate store. The p12 file includes a client certificate for "Webbrowser Test User" and the "Test CA" certificate. All private-keys and certificates including the Test CA are generated during the maven build and are private to your machine. Make sure to protect the CA private-key at *.../highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/ca/testca_private-key.pem* from third-party access if you have installed the Test CA certificate in your certificate store.
