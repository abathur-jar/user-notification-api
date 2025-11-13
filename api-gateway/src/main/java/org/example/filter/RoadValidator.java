package org.example.filter;


import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RoadValidator {
    public static final List<String> list = List.of(
            "auth/register",
            "auth/login",
            "auth/refresh"
    );

    public Predicate<ServerHttpRequest> predicate = serverHttpRequest ->
            list.stream().noneMatch(uri ->
                    serverHttpRequest.getURI().getPath().contains(uri));
}
