package edu.andrews.cas.physics.inventory.server.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RegistrationResponse(@JsonProperty boolean validFirstName,
                                   @JsonProperty boolean validLastName,
                                   @JsonProperty boolean validUsername,
                                   @JsonProperty boolean validEmail,
                                   @JsonProperty boolean validPassword,
                                   @JsonProperty boolean uniqueUsername,
                                   @JsonProperty boolean uniqueEmail) {
    @JsonIgnore
    public boolean isValid() {
        return validFirstName() && validLastName() && validUsername() && validEmail() && validPassword();
    }

    @JsonIgnore
    public boolean isUnique() {
        return uniqueUsername() && uniqueEmail();
    }
}
