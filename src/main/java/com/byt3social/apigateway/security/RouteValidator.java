package com.byt3social.apigateway.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    public static final List<String> openAPIEndpoints = List.of(
            "/auth/colaborador/login",
            "/auth/colaborador/code",
            "/auth/colaborador/validar",
            "/auth/organizacao/login",
            "/auth/organizacao/validar",
            "/eureka"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openAPIEndpoints.stream().noneMatch(uri -> request.getURI().getPath().contains(uri));
}
