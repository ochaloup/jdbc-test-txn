# FROM postgres: does not work on OpenShift
# see: https://github.com/docker-library/postgres/issues/359
FROM centos/postgresql-96-centos7

ENV POSTGRESQL_DATABASE crashrec
ENV POSTGRESQL_USER crashrec
ENV POSTGRESQL_PASSWORD crashrec
ENV POSTGRESQL_MAX_PREPARED_TRANSACTIONS 50

# * Red Hat OpenShift command does not have any way to declare
#   sql script to run after DB starts. Probably could be done
#   on the level of OpenShift with some postInit script... not sure what is the best practice
#   to get psql command
USER root
RUN yum install -y postgresql-contrib \
    && yum clean all && \
    rpm -q postgresql-contrib

RUN mkdir /docker-entrypoint-initdb.d
RUN chmod uog+rwx -R /docker-entrypoint-initdb.d

COPY psql-runner.sh /opt
RUN chmod uog+x /opt/psql-runner.sh

RUN chown -R postgres /docker-entrypoint-initdb.d

USER postgres

# script with sql commands
COPY psql_init.sql  /docker-entrypoint-initdb.d/

CMD ["/opt/psql-runner.sh"]

