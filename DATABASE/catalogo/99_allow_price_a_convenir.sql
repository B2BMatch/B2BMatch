SET search_path TO catalogo, public;

/*
====================================================
 Proyecto : b2bmatch
 Migración: servicios "a convenir" (price opcional)

 Aplica la opción A: price y description pasan a ser
 opcionales en professional_service y company_service,
 para que el catálogo pueda publicar servicios sin
 precio definido ("A convenir") quedando el precio
 para negociarse en la cotización.

 Idempotente: se puede ejecutar más de una vez.

 Reversa:
   ALTER TABLE catalogo.professional_service ALTER COLUMN price SET NOT NULL;
   ALTER TABLE catalogo.professional_service ALTER COLUMN description SET NOT NULL;
   ALTER TABLE catalogo.company_service ALTER COLUMN price SET NOT NULL;
   ALTER TABLE catalogo.company_service ALTER COLUMN description SET NOT NULL;
====================================================
*/

ALTER TABLE catalogo.professional_service ALTER COLUMN price DROP NOT NULL;
ALTER TABLE catalogo.professional_service ALTER COLUMN description DROP NOT NULL;

ALTER TABLE catalogo.company_service ALTER COLUMN price DROP NOT NULL;
ALTER TABLE catalogo.company_service ALTER COLUMN description DROP NOT NULL;