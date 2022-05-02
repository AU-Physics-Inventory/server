package edu.andrews.cas.physics.inventory.server.request.user;

import edu.andrews.cas.physics.inventory.server.model.UserStatus;

public record ChangeUserStatusRequest(String username, UserStatus status) {}
