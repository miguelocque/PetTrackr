package com.PetTrackr.PetTrackr.config;

import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.repository.OwnerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService that loads Owner by email for Spring Security authentication.
 * Uses the BCrypt password hash stored in the Owner entity.
 */
@Service
public class OwnerUserDetailsService implements UserDetailsService {

    private final OwnerRepository ownerRepository;

    public OwnerUserDetailsService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Owner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found with email: " + email));

        return User.builder()
                .username(owner.getEmail())
                .password(owner.getPasswordHash())
                .roles("USER")
                .build();
    }
}
