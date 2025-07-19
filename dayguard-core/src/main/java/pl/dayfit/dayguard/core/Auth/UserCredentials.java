package pl.dayfit.dayguard.core.Auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.dayfit.dayguard.core.Entities.User;

import java.util.Collection;

@RequiredArgsConstructor
public class UserCredentials implements UserDetails {
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
}