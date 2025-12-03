package org.example.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class GatewayAuthFilter implements GlobalFilter, Ordered {

    private final WebClient webClient;

    public GatewayAuthFilter(WebClient webClient) {
        this.webClient = webClient;
    }

    // "Когда придет запрос, GatewayFilterChain цепочка фильтров"
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // путь
        final String path = exchange.getRequest()
                .getURI()
                .getPath();

        // заголовок
        final String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        // публичные endpoints
        if (path.startsWith("/auth/register") || path.startsWith("/auth/login")) {
            System.out.println("Публичный endpoint: " + path);
            return chain.filter(exchange);
        }

        // все остальные endpoints
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Нет Authorization header: " + path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // "Проверим токен через другой сервис..."
        final String token = authHeader.substring(7);
        System.out.println("Проверка токена для: " + path);

        return webClient.post()
                .uri("http://localhost:8089/auth/validate?token=" + token)
                .retrieve()// "Выполни запрос"
                .bodyToMono(Boolean.class)// "Преобразуй ответ в true/false"
                .flatMap(isValid -> {
                    if (isValid) {
                        System.out.println("Успех валидации: " + path);
                        return chain.filter(exchange);
                    } else {
                        System.out.println("Провал валидации: " + path);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                });
    }

    // "Выполняй меня первым!"
    @Override
    public int getOrder() {
        return -1;
    }
}
