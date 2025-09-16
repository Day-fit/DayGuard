package pl.dayfit.dayguard.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.dayfit.dayguard.entity.User;

import java.security.Principal;
import java.util.Collection;
import java.util.UUID;

@RequiredArgsConstructor
public class UserCredentials implements UserDetails, Principal {
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public UUID getId()
    {
        return user.getId();
    }

    @Override
    public String getName() {
        return user.getUsername();
    }
}