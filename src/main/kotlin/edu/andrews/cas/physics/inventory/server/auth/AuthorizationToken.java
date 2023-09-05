package edu.andrews.cas.physics.inventory.server.auth;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;

public record AuthorizationToken(String token) {
    public Jws<Claims> getClaims(SecretKey secretKey) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(this.token);
    }
}