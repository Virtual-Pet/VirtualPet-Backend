package com.virtualpet.auth.service;

import com.virtualpet.auth.repository.UserRepository;
import com.virtualpet.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación de UserDetailsService usada por Spring Security
 * en el proceso de autenticación (AuthenticationManager).
 *
 * Carga al usuario por email y construye el UserPrincipal que
 * Spring inyecta en los controllers con @AuthenticationPrincipal.
 *
 * No contiene lógica de negocio — es infraestructura de seguridad.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
            .findByEmailIgnoreCase(username)
            .map(u -> new UserPrincipal(
                u.getId(),
                u.getEmail(),
                u.getPasswordHash(),
                u.getRole().name(),
                u.getForcePasswordChange()
            ))
            .orElseThrow(() -> {
                log.warn("Autenticación fallida — usuario no encontrado: {}", username);
                return new UsernameNotFoundException("Usuario no encontrado: " + username);
            });
    }
}