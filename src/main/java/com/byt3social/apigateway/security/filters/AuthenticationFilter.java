package com.byt3social.apigateway.security.filters;

import com.byt3social.apigateway.dto.ColaboradorDTO;
import com.byt3social.apigateway.dto.OrganizacaoDTO;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    @Autowired
    private RouteValidator routeValidator;
    @Value("${com.byt3social.auth.colaborador.token.validation.url}")
    private String colaboradorTokenValidationUrl;
    @Value("${com.byt3social.auth.organizacao.token.validation.url}")
    private String organizacaoTokenValidationUrl;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if(routeValidator.isSecured.test(exchange.getRequest())) {
                if(!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION) ||
                    !exchange.getRequest().getHeaders().containsKey("B3Social-User")) {
                    return bloquearRequisicao(exchange);
                }

                String userHeader = exchange.getRequest().getHeaders().get("B3Social-User").get(0);
                String authenticationHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

                if(userHeader != null && authenticationHeader != null && authenticationHeader.startsWith("Bearer ")) {
                    authenticationHeader = authenticationHeader.replace("Bearer ", "");

                    RestTemplate restTemplate = new RestTemplate();

                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(authenticationHeader);

                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(headers);

                    try {
                        ServerHttpRequest forwardRequest = null;

                        if(userHeader.equals("colaborador")) {
                            forwardRequest = validarTokenColaborador(exchange, restTemplate, request);
                        } else if(userHeader.equals("organizacao")) {
                            forwardRequest = validarTokenOrganizacao(exchange, restTemplate, request);
                        } else {
                            return bloquearRequisicao(exchange);
                        }

                        forwardRequest = forwardRequest
                                .mutate()
                                .header("B3Social-Logging", exchange.getRequest().getPath().toString().split("/")[1])
                                .build();

                        exchange = exchange.mutate().request(forwardRequest).build();
                    } catch(HttpClientErrorException e) {
                        return bloquearRequisicao(exchange);
                    }
                } else {
                    return bloquearRequisicao(exchange);
                }
            }

            return chain.filter(exchange);
        };
    }

    private ServerHttpRequest validarTokenOrganizacao(ServerWebExchange exchange, RestTemplate restTemplate, HttpEntity<Map<String, Object>> request) {
        ServerHttpRequest forwardRequest;

        ResponseEntity<OrganizacaoDTO> organizacaoDTOResponseEntity = restTemplate.exchange(organizacaoTokenValidationUrl, HttpMethod.POST, request, OrganizacaoDTO.class);

        forwardRequest = exchange.getRequest()
                .mutate()
                .header("B3Social-Organizacao", organizacaoDTOResponseEntity.getBody().organizacaoId().toString())
                .header("B3Social-User-Name", organizacaoDTOResponseEntity.getBody().nomeEmpresarial())
                .build();

        return forwardRequest;
    }

    private ServerHttpRequest validarTokenColaborador(ServerWebExchange exchange, RestTemplate restTemplate, HttpEntity<Map<String, Object>> request) {
        ServerHttpRequest forwardRequest;

        ResponseEntity<ColaboradorDTO> colaboradorDTOResponseEntity = restTemplate.exchange(colaboradorTokenValidationUrl, HttpMethod.POST, request, ColaboradorDTO.class);

        forwardRequest = exchange.getRequest()
                .mutate()
                .header("B3Social-Colaborador", colaboradorDTOResponseEntity.getBody().id().toString())
                .header("B3Social-User-Name", colaboradorDTOResponseEntity.getBody().nome())
                .build();

        return forwardRequest;
    }

    private static Mono<Void> bloquearRequisicao(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

        return exchange.getResponse().setComplete();
    }

    public static class Config {

    }
}
