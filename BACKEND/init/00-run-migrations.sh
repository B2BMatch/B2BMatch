set -e

for dir in usuarios perfiles catalogo ofertas resenias notificaciones; do
  for f in /sql/$dir/*.sql; do
    echo "Ejecutando $f"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f "$f"
  done
done