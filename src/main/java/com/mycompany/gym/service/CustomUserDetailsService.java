package com.mycompany.gym.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.mycompany.gym.domain.User;
import com.mycompany.gym.repository.UserRepository;

/**
 * Carga los datos del usuario (username, password, roles) desde la BD.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> opt = userRepo.findByUsername(username);
        if (opt.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        User usuario = opt.get();

        // Convertimos nuestra entidad User (dominio) a Spring Security UserDetails:
        return org.springframework.security.core.userdetails.User
                .withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRole())   // si tu campo role es p.ej. "CLIENT" o "ADMIN"
                .build();
    }
}
