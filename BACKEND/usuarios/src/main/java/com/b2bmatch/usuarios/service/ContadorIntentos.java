package com.b2bmatch.usuarios.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Cuenta los intentos fallidos de login y frena los que se repiten de mas.
 *
 * Hay dos contadores porque los dos limites defienden cosas distintas y porque
 * un solo contador por email era un problema por partida doble: permitia
 * enumerar cuentas (bastaba con fijarse en si la respuesta cambiaba al quinto
 * fallo) y permitia tumbar cuentas (cinco intentos desde cualquier maquina
 * dejaban a la victima fuera quince minutos). El de cuenta va por el par IP y
 * email, de modo que atacar a alguien solo te frena a ti, y el de IP acota lo que
 * se puede sacar desde una sola maquina.
 *
 * El estado vive en memoria y por tanto en un solo proceso. Con varias replicas
 * del microservicio cada una lleva la suya y el limite se relaja en la practica
 * tantas veces como replicas haya. Queda anotado en el itinerario; resolverlo pide
 * un almacen compartido, que ya es otro bloque.
 */
public class ContadorIntentos {

    private final ConcurrentHashMap<String, Intentos> intentos = new ConcurrentHashMap<>();
    private final int maxPorCuenta;
    private final int maxPorIp;
    private final Duration duracionBloqueo;
    private final int maxEntradas;
    private final Clock reloj;

    public ContadorIntentos(int maxPorCuenta, int maxPorIp, Duration duracionBloqueo, int maxEntradas, Clock reloj) {
        this.maxPorCuenta = maxPorCuenta;
        this.maxPorIp = maxPorIp;
        this.duracionBloqueo = duracionBloqueo;
        this.maxEntradas = maxEntradas;
        this.reloj = reloj;
    }

    public boolean bloqueado(String clave) {
        Intentos intento = intentos.get(clave);
        return intento != null && intento.bloqueadoHasta != null
                && LocalDateTime.now(reloj).isBefore(intento.bloqueadoHasta);
    }

    public void falloDeCuenta(String clave) {
        registrar(clave, maxPorCuenta);
    }

    public void falloDeIp(String clave) {
        registrar(clave, maxPorIp);
    }

    /**
     * Un acierto borra los dos contadores del par. Para la IP tambien, porque una
     * oficina entera detras de una NAT la comparte y sin esto dos equivocos
     * dejarian fuera a todos los demas de esa IP.
     */
    public void exito(String claveCuenta, String claveIp) {
        intentos.remove(claveCuenta);
        intentos.remove(claveIp);
    }

    private void registrar(String clave, int maximo) {
        Intentos intento = intentos.computeIfAbsent(clave, k -> new Intentos(LocalDateTime.now(reloj)));
        synchronized (intento) {
            intento.cuenta++;
            intento.ultimoIntento = LocalDateTime.now(reloj);
            if (intento.cuenta >= maximo) {
                intento.bloqueadoHasta = LocalDateTime.now(reloj).plus(duracionBloqueo);
                intento.cuenta = 0;
            }
        }
        podar();
    }

    /**
     * Mantiene acotado el mapa sin dejarlo de llevar.
     *
     * Se evicts una entrada por cada una que se pasa del tope, y primero lo que
     * ya caducó, despues lo mas viejo dando margen a los bloqueos vivos. Se
     * evicta justo lo necesario, y no de golpe, porque la version anterior
     * borraba en un solo `removeIf` todo lo que no estuviera bloqueado en ese
     * instante, o sea todos los contadores a medias: con eso, en cuanto alguien
     * llenaba el mapa, un inundador ponia a cero el progreso de todo el mundo a
     * la vez y el limite se quedaba sin efecto.
     *
     * Que la cuenta siga cambiando aunque el mapa este lleno es lo importante.
     * Una version intermedia, mas cauta, se negaba a llevar la cuenta de las
     * claves nuevas cuando no cabian, y eso era un agujero escondido: bastaba
     * llenar el mapa para que el limite dejara de contar y el atacante pasara
     * ilimitado. Un tope que se ignora cuando aprieta no es un tope.
     *
     * Lo que no llega a cubrirse: si un atacante sostiene la inundacion el tiempo
     * suficiente, el contador de una cuenta que lleva rato sin intentar entra en
     * la parte vieja y se va. Cerrar eso de verdad pide el almacen compartido que
     * este bloque no incluye, y por lo pronto un mapa de 10 000 entradas solo se
     * llena si hay 10 000 cuentas distintas fallando, porque un email que no
     * existe ni llega a contar.
     */
    private void podar() {
        if (intentos.size() <= maxEntradas) {
            return;
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        intentos.entrySet().removeIf(e -> e.getValue().bloqueadoHasta != null
                && e.getValue().bloqueadoHasta.isBefore(ahora));

        if (intentos.size() <= maxEntradas) {
            return;
        }
        int sobrantes = intentos.size() - maxEntradas;
        intentos.entrySet().stream()
                .sorted(Comparator
                        .comparing((Map.Entry<String, Intentos> e) -> e.getValue().bloqueadoHasta == null ? 0 : 1)
                        .thenComparing(e -> e.getValue().ultimoIntento))
                .limit(sobrantes)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .forEach(intentos::remove);
    }

    /** Solo para diagnostico y tests. */
    public int entradas() {
        return intentos.size();
    }

    /**
     * Si queda contadores para esa clave. `bloqueado` no sirve para distinguir
     * "nunca fallo" de "se lo llevo la poda", asi que hace falta esto para poder
     * comprobar que la poda se lleva lo que toca y solo lo que toca.
     */
    public boolean siguiendo(String clave) {
        return intentos.containsKey(clave);
    }

    private static final class Intentos {
        int cuenta;
        LocalDateTime bloqueadoHasta;
        /** Se fija al crear y se refresca en cada fallo, para saber cuanto lleva
         *  sin tocarse una cuenta a medias. */
        LocalDateTime ultimoIntento;

        Intentos(LocalDateTime ultimoIntento) {
            this.ultimoIntento = ultimoIntento;
        }
    }
}
