For executing DAO tests from your IDE without Maven, execute

`docker run -it --rm -e POSTGRES_PASSWORD=password -e TZ=Europe/Berlin -e POSTGRES_DB=db -p 127.0.0.1:54321:5432 postgres:13 postgres -c log_statement=all`

to start a PostgreSQL 13 docker container. Press Ctrl-C to stop and cleanup the docker container after testing.