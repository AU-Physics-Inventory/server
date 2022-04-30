package edu.andrews.cas.physics.inventory.server.request.user;

import java.util.List;

public record UserInvitation(String email, List<String> roles) {}
