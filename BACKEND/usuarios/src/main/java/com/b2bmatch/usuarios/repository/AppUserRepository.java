package com.b2bmatch.usuarios.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.b2bmatch.usuarios.model.AppUser;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long>{
    Optional<AppUser> findByEmail(String email);
    List<AppUser> findByStatusNot(String status);
}
