package org.example.filter;

import org.example.util.JwtService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {
    private final RoadValidator roadValidator;
    private final JwtService jwtService;

    // rest template какие есть аналоги, как работают
    // создать бд для токенов в security-service

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (roadValidator.predicate.test(exchange.getRequest())) {
             if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                 throw new RuntimeException("Missing AuthHeader");
             }
             String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
             if (auth != null && auth.isEmpty()) {
                 auth = auth.substring(7);
             }
             try {
                 jwtService.validateToken(auth);
             } catch (Exception e) {
                 throw new IllegalArgumentException("Невалидный токен!");
             }
            }
            return chain.filter(exchange);
        });
    }

    public AuthFilter(RoadValidator roadValidator, JwtService jwtService) {
        super(Config.class);
        this.roadValidator = roadValidator;
        this.jwtService = jwtService;
    }

    public static class Config {

    }
}
