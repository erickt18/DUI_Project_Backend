package com.rfidcampus.rfid_campus.security;

import com.rfidcampus.rfid_campus.model.Estudiante;
import com.rfidcampus.rfid_campus.repository.EstudianteRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EstudianteRepository estudianteRepository;

    public CustomUserDetailsService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Estudiante est = estudianteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No existe usuario con email: " + email));

        // Rol desde BD (ADMIN o STUDENT)
        String rol = est.getRol().getNombre().toUpperCase();

        // Creamos autoridades mixtas: ROLE_ + nombre limpio
        return User.withUsername(est.getEmail())
                .password(est.getPasswordHash())
                .authorities(
                        "ROLE_" + rol, // para endpoints que usan hasRole()
                        rol // para endpoints que usan hasAuthority()
                )
                .accountLocked(Boolean.FALSE.equals(est.getActivo()))
                .build();
    }

}
