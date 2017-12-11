# CHANGE_CONF_FILE=${1:-/var/lib/postgresql/data/postgresql.conf}
CHANGE_CONF_FILE='/var/lib/postgresql/data/postgresql.conf'

echo "Changing $CHANGE_CONF_FILE"

sed -i 's/.*max_prepared_transactions.*/max_prepared_transactions = 50/' "$CHANGE_CONF_FILE"

