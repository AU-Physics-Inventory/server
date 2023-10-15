package edu.andrews.cas.physics.inventory.server.request.user;

public class UserRegistration {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private String accessCode;
    private boolean fromEmailLink;

    public UserRegistration() {

    }

    public UserRegistration(String firstName, String lastName, String email, String username, String password, String accessCode, boolean fromEmailLink) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.setEmail(email);
        this.setUsername(username);
        this.password = password;
        this.accessCode = accessCode;
        this.fromEmailLink = fromEmailLink;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase();
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
        this.email = email.toLowerCase();
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
