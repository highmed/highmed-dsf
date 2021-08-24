#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE medic1_fhir;
    GRANT ALL PRIVILEGES ON DATABASE medic1_fhir TO liquibase_user;
    CREATE DATABASE medic1_bpe;
    GRANT ALL PRIVILEGES ON DATABASE medic1_bpe TO liquibase_user;
    CREATE DATABASE medic2_fhir;
    GRANT ALL PRIVILEGES ON DATABASE medic2_fhir TO liquibase_user;
    CREATE DATABASE medic2_bpe;
    GRANT ALL PRIVILEGES ON DATABASE medic2_bpe TO liquibase_user;
    CREATE DATABASE medic3_fhir;
    GRANT ALL PRIVILEGES ON DATABASE medic3_fhir TO liquibase_user;
    CREATE DATABASE medic3_bpe;
    GRANT ALL PRIVILEGES ON DATABASE medic3_bpe TO liquibase_user;
    CREATE DATABASE ttp_fhir;
    GRANT ALL PRIVILEGES ON DATABASE ttp_fhir TO liquibase_user;
    CREATE DATABASE ttp_bpe;
    GRANT ALL PRIVILEGES ON DATABASE ttp_bpe TO liquibase_user;
EOSQL