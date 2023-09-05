package edu.andrews.cas.physics.inventory.server.auth;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
public class LoggedInUsers {
    private final HashSet<String> users = new HashSet<>();

    public boolean contains(String user) {
        return users.contains(user);
    }

    public boolean remove(String user) {
        return users.remove(user);
    }

    public boolean add(String user) {
        return users.add(user);
    }

    public List<String> getUsers() {
        return users.stream().toList();
    }
}
