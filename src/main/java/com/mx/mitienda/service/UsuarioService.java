package com.mx.mitienda.service;


import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    public UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public List<Usuario> getAll(){
        return usuarioRepository.findAll();
    }

    Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario getById(Long id){
        return usuarioRepository.findByIdAndActiveTrue(id).orElseThrow(()->new NotFoundException("Usuario no encontrado con id::: "+ id +" no encontrado"));
    }

    public Usuario save(Usuario usuario){
            usuario.setActive(true);
        return usuarioRepository.save(usuario);
    }
    public Usuario registerUser(Usuario usuario){
        if(usuarioRepository.findByEmail(usuario.getEmail()).isPresent()){
            throw new IllegalArgumentException("Email de usuario ya existe");
        }
        if(usuarioRepository.findByUsername(usuario.getUsername()).isPresent()){
            throw new IllegalArgumentException("Nombre usuario ya existe");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActive(true);
        return usuarioRepository.save(usuario);
    }

    public Usuario findByUsernameActive(String username){
        return usuarioRepository.findByUsernameAndActiveTrue(username).orElseThrow(()-> new UsernameNotFoundException("Usuario no encontrado::" + username));
    }

    public List<Usuario> listActive(String username){
        return usuarioRepository.findByActiveTrue();
    }

    public List<Usuario> listInactive(String username){
        return usuarioRepository.findByActiveFalse();
    }

    public void logicUserErase(Long id){
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(()-> new UsernameNotFoundException("Usuario con id no encontrado::" + id));
        usuario.setActive(false);
        usuarioRepository.save(usuario);
    }

    public Usuario findByEmailUser(String email){
        return usuarioRepository.findByEmailAndActiveTrue(email);
    }

    public Usuario updateUser(Usuario updatedUser){
        Usuario oldUser = usuarioRepository.findById(updatedUser.getId()).orElseThrow(()-> new UsernameNotFoundException("El usuario no se ha encontrado::" + updatedUser.getUsername()));
        oldUser.setUsername(updatedUser.getUsername());
        oldUser.setEmail(updatedUser.getEmail());
        oldUser.setRol(updatedUser.getRol());
        if(updatedUser.getPassword()!=null || !updatedUser.getPassword().isBlank()){
            oldUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        return usuarioRepository.save(oldUser);
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

}
