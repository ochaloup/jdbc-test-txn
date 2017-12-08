
# iterating over all sql scripts
find . -type f -name '/docker-entrypoint-initdb.d/*.sql' -exec sh -c '
  file="$0"
  echo "$file"
' {} ';'
# PGPASSWORD=$POSTGRESQL_PASSWORD psql -h localhost $POSTGRESQL_DATABASE $POSTGRESQL_USER -a -f ./psql_init.sql

exec run-postgresql "$@"
