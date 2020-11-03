#!/bin/bash

echo starting postgres docker container at 127.0.0.1:54321 ...
docker run -it --rm -e POSTGRES_PASSWORD=password -e TZ=Europe/Berlin -e POSTGRES_DB=db -p 127.0.0.1:54321:5432 postgres:13 postgres -c log_statement=all
