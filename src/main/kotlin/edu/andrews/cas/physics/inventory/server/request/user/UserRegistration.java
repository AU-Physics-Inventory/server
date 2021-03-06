package edu.andrews.cas.physics.inventory.server.request.user;

public class UserRegistration {
    private String email;
    private String username;
    private String password;
    private String accessCode;
    private boolean fromEmailLink;

    public UserRegistration() {

    }

    public UserRegistration(String email, String username, String password, String accessCode, boolean fromEmailLink) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.accessCode = accessCode;
        this.fromEmailLink = fromEmailLink;
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

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public boolean isFromEmailLink() {
        return fromEmailLink;
    }

    public void setFromEmailLink(boolean fromEmailLink) {
        this.fromEmailLink = fromEmailLink;
    }
}
