package edu.andrews.cas.physics.inventory.server.repository.model;

import edu.andrews.cas.physics.inventory.server.exception.InvalidUserDocumentException;
import edu.andrews.cas.physics.inventory.server.model.UserStatus;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User implements RepositoryModel {
    private final Document userDocument;

    public User() {
        userDocument = new Document()
                .append("lastAttempt", null)
                .append("lastSuccess", null)
                .append("failedAttempts", 0)
                .append("emailVerified", false)
                .append("accessCode", null)
                .append("registrationDate", LocalDateTime.now());
    }

    public User(Document userDocument) {
        this.userDocument = userDocument;
    }

    public User status(UserStatus status) {
        userDocument.append("status", status.name());
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

    public User firstName(String firstName) {
        userDocument.append("firstName", firstName);
        return this;
    }

    public User lastName(String lastName) {
        userDocument.append("lastName", lastName);
        return this;
    }

    public Document build() throws Exception {
        if (!this.userDocument.containsKey("roles")) this.roles(null);
        if (this.userDocument.containsKey("status")
                && this.userDocument.containsKey("username")
                && this.userDocument.containsKey("email")
                && this.userDocument.containsKey("password")
                && this.userDocument.containsKey("salt")
                && this.userDocument.containsKey("firstName")
                && this.userDocument.containsKey("lastName"))
            return this.userDocument;
        throw new InvalidUserDocumentException();
    }

    public String getFirstName() {
        return userDocument.getString("firstName");
    }

    public String getLastName() {
        return userDocument.getString("lastName");
    }

    public UserStatus getStatus() {
        return UserStatus.valueOf(userDocument.getString("status"));
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

    public String getLastAuthenticationAttempt() {
        var date =  userDocument.getDate("lastAttempt");
        return date == null ? null : date.toString();
    }

    public String getLastAuthenticationSuccess() {
        var date = userDocument.getDate("lastSuccess");
        return date == null ? null : date.toString();
    }

    public int getNumFailedAuthenticationAttempts() {
        return userDocument.getInteger("failedAttempts");
    }

    @Override
    public String toString() {
        return userDocument.toString();
    }
}

