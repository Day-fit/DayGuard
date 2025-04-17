package io.dayfit.github.dayguard.POJOs;

import lombok.AllArgsConstructor;

import java.security.Principal;

@AllArgsConstructor
public class StompPrincipal implements Principal {
    private  String name;

    @Override
    public String getName() {
        return this.name;
    }
}
