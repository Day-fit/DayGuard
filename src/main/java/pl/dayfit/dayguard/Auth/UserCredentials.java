package pl.dayfit.dayguard.Auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.dayfit.dayguard.Entities.User;

import java.security.Principal;
import java.util.Collection;

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

    public Long getId()
    {
        return user.getId();
    }

    @Override
    public String getName() {
        return user.getUsername();
    }
}