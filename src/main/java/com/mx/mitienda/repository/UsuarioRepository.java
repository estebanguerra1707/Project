package com.mx.mitienda.repository;


import com.mx.mitienda.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    //Esto crea CRUD automático (findAll, save, deleteById, etc.) para la entidad Usuario.
    List<Usuario> findByActiveTrue();
    Optional<Usuario> findByIdAndActiveTrue(Long id);
    Optional<Usuario> findByUsername(String userName);
    Optional<Usuario> findByUsernameAndActiveTrue(String name);
    Optional<Usuario> findByEmail(String password);
    List<Usuario>findByActiveFalse();
    Optional<Usuario> findByEmailAndActiveTrue(String email);
    List<Usuario> findByBranchId(Long branchId);
}
