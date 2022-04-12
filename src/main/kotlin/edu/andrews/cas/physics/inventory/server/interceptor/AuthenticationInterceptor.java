package edu.andrews.cas.physics.inventory.server.interceptor;

import edu.andrews.cas.physics.inventory.server.auth.LoggedInUsers;
import edu.andrews.cas.physics.inventory.server.service.AuthenticationService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import kotlin.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private static final Logger logger = LogManager.getLogger();

    private final SecretKey secretKey;
    private final LoggedInUsers loggedInUsers;

    @Autowired
    public AuthenticationInterceptor(@Lazy SecretKey secretKey, LoggedInUsers loggedInUsers) {
        this.secretKey = secretKey;
        this.loggedInUsers = loggedInUsers;
    }

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        logger.info("[Auth Interceptor -- Pre-Handle] Checking request authentication at endpoint {}", request.getRequestURI());
        var requestURI = request.getRequestURI();
        if (requestURI.startsWith("/admin")) return isUserAdmin(request, response);
        return true;
    }

    @Override
    public void postHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    private boolean isUserAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var authenticated = isAuthenticated(request);
        var result = authenticated.getFirst();
        if (result) result = ((String) authenticated.getSecond().getBody().get("roles")).contains("admin");
        if (!result) response.sendError(401, "Unable to authenticate admin user.");
        return result;
    }

    private Pair<Boolean, Jws<Claims>> isAuthenticated(HttpServletRequest request) {
        try {
            var claims = getJwsClaimsFromRequest(request);
            if (claims == null || !loggedInUsers.contains(claims.getBody().getSubject())) throw new Exception();
            return new Pair<>(true, claims);
        } catch(Exception e) {
            return new Pair<>(false, null);
        }
    }

    private Jws<Claims> getJwsClaimsFromRequest(HttpServletRequest request) throws UnsupportedJwtException,
            MalformedJwtException, SignatureException, ExpiredJwtException, IllegalArgumentException {
        String jwtToken = request.getHeader("Authorization");
        if (jwtToken != null) {
            return Jwts.parserBuilder().setSigningKey(this.secretKey).build().parseClaimsJws(jwtToken);
        } else return null;
    }
}
