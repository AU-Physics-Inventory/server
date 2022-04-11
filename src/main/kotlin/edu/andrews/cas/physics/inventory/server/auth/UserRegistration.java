package edu.andrews.cas.physics.inventory.server.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRegistration {
    @JsonProperty
    private String email;

    @JsonProperty
    private String username;

    @JsonProperty
    private String password;

    public UserRegistration() {

    }

    public UserRegistration(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
