package edu.andrews.cas.physics.inventory.server.request.user;

public record UserLogin(String username, String password) {
    @Override
    public String username() {
        return username.toLowerCase();
    }
}