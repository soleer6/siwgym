package com.mycompany.gym.controller;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.gym.domain.User;
import com.mycompany.gym.service.UserService;

/**
 * Controlador simplificado para login y registro:
 *  - POST /api/auth/register  → registra un usuario (igual que antes)
 *  - POST /api/auth/login     → compara user/password de forma manual,
 *    y si es válido guarda userId en la sesión HTTP.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User newUser) {

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        User created = userService.save(newUser);
        created.setPassword(null);
        return ResponseEntity.ok(created);
    }

     @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest, HttpServletRequest request) {
        Optional<User> optUser = userService.findByUsername(loginRequest.getUsername());
        if (optUser.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        User found = optUser.get();


        if (!passwordEncoder.matches(loginRequest.getPassword(), found.getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("userId", found.getId());
        session.setAttribute("role", found.getRole());

        found.setPassword(null);
        return ResponseEntity.ok(found);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }
}
