package com.byt3social.apigateway.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

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
            "/acoes-sociais/doacoes/pagseguro",
            "/eureka",
            "/prospeccao/indicacoes/*/cadastros",
            "/prospeccao/indicacoes/*/cadastros/verificacoes"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openAPIEndpoints.stream().noneMatch(e -> new AntPathMatcher().match(e.toString(), request.getPath().toString()));
}
