package edu.andrews.cas.physics.inventory.server.model;

import edu.andrews.cas.physics.inventory.server.exception.InvalidUserDocumentException;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private final Document userDocument;

    public User() {
        userDocument = new Document()
                .append("lastAttempt", null)
                .append("lastSuccess", null)
                .append("failedAttempts", 0)
                .append("emailVerified", false)
                .append("accessCode", null);
    }

    public User(Document userDocument) {
        this.userDocument = userDocument;
    }

    public User status(UserStatus status) {
        userDocument.append("status", status);
        return this;
    }

    public User username(String username) {
        userDocument.append("username", username);
        return this;
    }

    public User email(String email) {
        userDocument.append("email", email);
        return this;
    }

    public User password(String password) {
        userDocument.append("password", password);
        return this;
    }

    public User salt(String salt) {
        userDocument.append("salt", salt);
        return this;
    }

    public User roles(List<String> roles) {
        if (roles == null) roles = new ArrayList<>();
        userDocument.append("roles", roles);
        return this;
    }

    public User emailVerified(boolean isVerified) {
        userDocument.replace("emailVerified", isVerified);
        return this;
    }

    public User accessCode(String code) {
        userDocument.replace("accessCode", code);
        return this;
    }

    public Document build() throws Exception {
        if (!this.userDocument.containsKey("roles")) this.roles(null);
        if (this.userDocument.containsKey("status")
                && this.userDocument.containsKey("username")
                && this.userDocument.containsKey("email")
                && this.userDocument.containsKey("password")
                && this.userDocument.containsKey("salt"))
            return this.userDocument;
        throw new InvalidUserDocumentException();
    }

    public UserStatus getStatus() {
        return userDocument.get("status", UserStatus.class);
    }

    public String getUsername() {
        return userDocument.getString("username");
    }

    public String getEmail() {
        return userDocument.getString("email");
    }

    public String getPassword() {
        return userDocument.getString("password");
    }

    public String getSalt() {
        return userDocument.getString("salt");
    }

    public List<String> getRoles() {
        return userDocument.getList("roles", String.class);
    }

    public String getAccessCode() {
        return userDocument.getString("accessCode");
    }

    public boolean isEmailVerified() {
        return userDocument.getBoolean("emailVerified");
    }

    public LocalDateTime getLastAuthenticationAttempt() {
        return userDocument.get("lastAttempt", LocalDateTime.class);
    }

    public LocalDateTime getLastAuthenticationSuccess() {
        return userDocument.get("lastSuccess", LocalDateTime.class);
    }

    public int getNumFailedAuthenticationAttempts() {
        return userDocument.getInteger("failedAttempts");
    }
}

