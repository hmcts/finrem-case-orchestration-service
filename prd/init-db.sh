#!/usr/bin/env sh

set -e

if [ -z "$POSTGRES_PASSWORD" ]; then
  echo "ERROR: Missing environment variables. Set value for '$POSTGRES_PASSWORD'."
  exit 1
fi

echo "Creating dbrefdata database . . ."

psql -v ON_ERROR_STOP=1 --username postgres --dbname postgres <<-EOSQL
  CREATE ROLE dbrefdata WITH PASSWORD 'dbrefdata';
  CREATE DATABASE dbrefdata ENCODING = 'UTF-8' CONNECTION LIMIT = -1;
  GRANT ALL PRIVILEGES ON DATABASE dbrefdata TO dbrefdata;
  ALTER ROLE dbrefdata WITH LOGIN;
EOSQL

echo "Done creating database dbrefdata."
