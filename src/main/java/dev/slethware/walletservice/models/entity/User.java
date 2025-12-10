package dev.slethware.walletservice.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_google_id", columnList = "google_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends Auditable implements UserDetails {

    @Column(nullable = false)
    private String email;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Grant all permissions to standard users.
        return List.of(
                new SimpleGrantedAuthority("PERMISSION_DEPOSIT"),
                new SimpleGrantedAuthority("PERMISSION_TRANSFER"),
                new SimpleGrantedAuthority("PERMISSION_READ"),
                new SimpleGrantedAuthority("PERMISSION_WITHDRAW") // Withdraw permission exclusive only to Users
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}