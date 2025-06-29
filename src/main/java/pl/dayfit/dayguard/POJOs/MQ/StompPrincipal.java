package pl.dayfit.dayguard.POJOs.MQ;

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
