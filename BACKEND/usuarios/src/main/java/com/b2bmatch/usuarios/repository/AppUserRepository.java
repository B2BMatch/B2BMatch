package com.b2bmatch.usuarios.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.b2bmatch.usuarios.model.AppUser;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long>{
    /**
     * Busca tambien entre las dadas de baja. Es lo que necesita el registro: el
     * email sigue reservado por `uk_app_user_email` para siempre, y consultarlo
     * sin filtro permite avisar de que ya esta ocupado en vez de dejar que reviente
     * la constraint con un 409 que no dice nada.
     */
    Optional<AppUser> findByEmail(String email);

    /**
     * El login va por aqui y no por la de arriba. Una cuenta dada de baja conserva
     * `status = 'ACTIVE'` (la baja vive solo en `deleted_at`), asi que filtrar por
     * status no la excluiria: el borrado tiene que comprobarlo el mismo SELECT.
     */
    @Query("SELECT u FROM AppUser u JOIN FETCH u.role "
            + "WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<AppUser> findVivaByEmail(@Param("email") String email);

    /**
     * Los cuatro de abajo traen el rol porque `toDto` lo lee para el nombre, y los
     * servicios que los consumen no son transaccionales. Sin `JOIN FETCH` el proxy
     * sale desligado del repositorio y revienta con LazyInitializationException.
     *
     * Esto no se nota en ejecucion normal porque Spring Boot activa
     * `spring.jpa.open-in-view` en true por defecto, que mantiene la sesion abierta
     * durante la peticion. Es un default, no una decision: en cuanto se ponga en
     * false (que es lo que recomienda el framework) fallan. Los tests de integracion
     * no tienen ese filtro, asi que lo detectan.
     *
     * En vez de marcar esos servicios de transaccionales, que abriria una conexion
     * durante toda la llamada, se paga el fetch en la consulta: una consulta en vez
     * de N, y la peticion termina sin sesion.
     *
     * El que escribe (`updateStatus`) lleva transaccion en vez de fetch, porque el
     * DTO se arma sobre lo que devuelve `save()`. Ahi el fetch no serviria.
     */
    @Query("SELECT u FROM AppUser u JOIN FETCH u.role WHERE u.id = :id")
    Optional<AppUser> findPorIdConRol(@Param("id") Long id);

    @Query("SELECT u FROM AppUser u JOIN FETCH u.role")
    List<AppUser> findTodosConRol();

    @Query("SELECT u FROM AppUser u JOIN FETCH u.role WHERE u.deletedAt IS NULL")
    List<AppUser> findVivosConRol();

    @Query("SELECT u FROM AppUser u JOIN FETCH u.role "
            + "WHERE u.role.name = :roleName AND u.deletedAt IS NULL")
    List<AppUser> findPorRolConRol(@Param("roleName") String roleName);
}
