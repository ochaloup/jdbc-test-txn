#!/bin/bash

# starting postgres now
set -x
run-postgresql "$@" &

# can't redirect error output to somewhere in docker probaly... 2&>1
function test {
  psql -l | grep -q $POSTGRESQL_DATABASE
}
while ! test; do sleep 1; done

# iterating over all sql scripts
find /docker-entrypoint-initdb.d -type f -name '*.sql' -exec sh -c '
  file="$0"
  echo "$file"
  PGPASSWORD=$POSTGRESQL_PASSWORD psql -h localhost $POSTGRESQL_DATABASE $POSTGRESQL_USER -a -f "$file"
' {} ';'

kill %1
while [ -e '/var/lib/pgsql/data/userdata/postmaster.pid' ]; do sleep 1; done

exec run-postgresql "$@"
