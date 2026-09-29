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
     *
     * El JOIN FETCH del rol no es una optimizacion: el login lee el rol para meterlo
     * en el token, y el metodo no es transaccional porque comparar una clave BCrypt
     * cuesta ~100 ms y no debe hacerse con una conexion abierta. Sin traerse el rol
     * en la consulta, el proxy sale desligado y revienta con LazyInitializationException
     * en cuanto `open-in-view` deje de estar activo.
     */
    @Query("SELECT u FROM AppUser u JOIN FETCH u.role "
            + "WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<AppUser> findVivaByEmail(@Param("email") String email);

    List<AppUser> findByDeletedAtIsNull();
    List<AppUser> findByRole_NameAndDeletedAtIsNull(String roleName);
}
