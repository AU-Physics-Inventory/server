package edu.andrews.cas.physics.inventory.server.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RegistrationResponse(@JsonProperty boolean username, @JsonProperty boolean email) {
    @JsonIgnore
    public boolean isSuccess() {
        return !(username() || email());
    }
}
