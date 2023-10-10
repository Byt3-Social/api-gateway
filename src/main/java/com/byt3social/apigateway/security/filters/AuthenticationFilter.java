package com.byt3social.apigateway.security.filters;

import com.byt3social.apigateway.dto.ColaboradorDTO;
import com.byt3social.apigateway.security.RouteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    @Autowired
    private RouteValidator routeValidator;
    @Value("${com.byt3social.auth.token.validation.url}")
    private String tokenValidationUrl;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if(routeValidator.isSecured.test(exchange.getRequest())) {
                if(!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

                    return exchange.getResponse().setComplete();
                }

                String authenticationHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

                if(authenticationHeader != null && authenticationHeader.startsWith("Bearer ")) {
                    authenticationHeader = authenticationHeader.replace("Bearer ", "");

                    RestTemplate restTemplate = new RestTemplate();

                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(authenticationHeader);

                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(headers);

                    try {
                        ResponseEntity<ColaboradorDTO> responseEntity = restTemplate.exchange(tokenValidationUrl, HttpMethod.POST, request, ColaboradorDTO.class);

                        ServerHttpRequest forwardRequest = exchange.getRequest()
                                .mutate()
                                .header("B3Social-Colaborador", responseEntity.getBody().id())
                                .build();

                        exchange = exchange.mutate().request(forwardRequest).build();
                    } catch(HttpClientErrorException e) {
                        if(e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

                            return exchange.getResponse().setComplete();
                        }
                    }
                } else {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

                    return exchange.getResponse().setComplete();
                }
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {

    }
}
