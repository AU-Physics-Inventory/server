package edu.andrews.cas.physics.inventory.server.model;

import java.util.List;

public class User {
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String email;
    private final boolean emailVerified;
    private final int failedAttempts;
    private final String lastAttempt;
    private final String lastSuccess;
    private final List<String> roles;
    private final UserStatus status;

    public User(edu.andrews.cas.physics.inventory.server.repository.model.User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.emailVerified = user.isEmailVerified();
        this.failedAttempts = user.getNumFailedAuthenticationAttempts();
        this.lastAttempt = user.getLastAuthenticationAttempt();
        this.lastSuccess = user.getLastAuthenticationSuccess();
        this.roles = user.getRoles();
        this.status = user.getStatus();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public String getLastAttempt() {
        return lastAttempt;
    }

    public String getLastSuccess() {
        return lastSuccess;
    }

    public List<String> getRoles() {
        return roles;
    }

    public UserStatus getStatus() {
        return status;
    }
}
