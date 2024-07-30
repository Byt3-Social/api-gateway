package com.byt3social.apigateway.security.filters;

import com.byt3social.apigateway.dto.LogDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class LoggingFilter implements GlobalFilter {
    private static Map<String, Map<String, List<String>>> endpoints = new HashMap<>();
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public LoggingFilter() {
        /* Prospecção */

        endpoints.put("/organizacoes", Map.of(
                "GET", List.of(
                        "CONSULTAR_ORGANIZACOES_CADASTRADAS", "Consultou todas as organizações cadastradas"
                ),
                "POST", List.of(
                        "CADASTRAR_ORGANIZACAO", "Cadastrou uma organização"
                )
        ));

        endpoints.put("/organizacoes/*", Map.of(
                "GET", List.of(
                        "CONSULTAR_ORGANIZACAO", "Consultou uma organização específica cadastrada"
                ),
                "PUT", List.of(
                        "ATUALIZAR_ORGANIZACAO", "Atualizou uma organização cadastrada"
                )
        ));

        endpoints.put("/indicacoes", Map.of(
                "GET", List.of(
                        "CONSULTAR_INDICACOES", "Consultou indicações"
                ),
                "POST", List.of(
                        "INDICAR_ORGANIZACAO", "Indicou uma organização"
                )
        ));

        endpoints.put("/indicacoes/*", Map.of(
                "GET", List.of(
                        "CONSULTAR_INDICACAO", "Consultou uma indicação específica"
                )
        ));

        /* Análise documental */

        endpoints.put("/processos", Map.of(
                        "GET", List.of(
                                "CONSULTAR_PROCESSOS", "Consultou todos os processos disponíveis")
                )
        );

        endpoints.put("/processos/*", Map.of(
                        "GET", List.of(
                                "CONSULTAR_PROCESSO", "Consultou um processo específico pelo ID")
                )
        );

        endpoints.put("/processos/*/atualizar", Map.of(
                        "PUT", List.of(
                                "SALVAR_PROCESSO", "Salvou um processo")
                )
        );

        endpoints.put("/processos/*/finalizar", Map.of(
                        "PUT", List.of(
                                "FINALIZAR_PROCESSO", "Finalizou um processo")
                )
        );

        endpoints.put("/processos/*/documentos/*", Map.of(
                        "POST", List.of(
                                "SOLICITAR_DOCUMENTO", "Solicitou um documento ao processo")
                )
        );

        endpoints.put("/processos/*/documentos-solicitados/*", Map.of(
                        "DELETE", List.of(
                                "EXCLUIR_DOCUMENTO_SOLICITADO", "Excluiu um documento solicitado do processo")
                )
        );

        endpoints.put("/processos/*/dados/*", Map.of(
                        "POST", List.of(
                                "SOLICITAR_DADO", "Solicitou um dado ao processo")
                )
        );

        endpoints.put("/processos/*/dados-solicitados/*", Map.of(
                        "DELETE", List.of(
                                "EXCLUIR_DADO_SOLICITADO", "Excluiu um dado solicitado do processo")
                )
        );

        endpoints.put("/processos/*/status", Map.of(
                        "PUT", List.of(
                                "ATUALIZAR_STATUS_PROCESSO", "Atualizou o status do processo")
                )
        );

        endpoints.put("/documentos-solicitados/*/reenvio", Map.of(
                        "POST", List.of(
                                "SOLICITAR_REENVIO_DOCUMENTO_SOLICITADO", "Solicitou reenvio de um documento solicitado")
                )
        );

        endpoints.put("/documentos-solicitados/*/aws", Map.of(
                        "POST", List.of(
                                "ENVIAR_DOCUMENTO_SOLICITADO", "Enviou documento solicitado no processo"),
                        "GET", List.of(
                                "RECUPERAR_DOCUMENTO_SOLICITADO", "Recuperou documento solicitado no processo"),
                        "DELETE", List.of(
                                "REMOVER_DOCUMENTO_ENVIADO", "Removeu documento solicitado enviado")
                )
        );

        /* Ações sociais */

        endpoints.put("/acoes/*/arquivos", Map.of(
                        "POST", List.of(
                                "SALVAR_ARQUIVO_ACAO", "Salvou arquivo para uma ação")
                )
        );

        endpoints.put("/acoes/arquivos/*", Map.of(
                        "GET", List.of(
                                "RECUPERAR_ARQUIVO_ACAO", "Recuperou arquivo de uma ação"),
                        "DELETE", List.of(
                                "EXCLUIR_ARQUIVO_ACAO", "Excluiu arquivo de uma ação")
                )
        );

        endpoints.put("/acoes-isp", Map.of(
                        "GET", List.of(
                                "CONSULTAR_ACOES_ISP", "Consultou todas as ações ISP"),
                        "POST", List.of(
                                "CADASTRAR_ACAO_ISP", "Cadastrou uma nova ação ISP")
                )
        );

        endpoints.put("/acoes-isp/*", Map.of(
                        "GET", List.of(
                                "CONSULTAR_ACAO_ISP", "Consultou uma ação ISP por ID"),
                        "PUT", List.of(
                                "ATUALIZAR_ACAO_ISP", "Atualizou uma ação ISP por ID"),
                        "DELETE", List.of(
                                "EXCLUIR_ACAO_ISP", "Excluiu uma ação ISP por ID")
                )
        );

        endpoints.put("/acoes-voluntariado", Map.of(
                        "POST", List.of(
                                "CADASTROU_ACAO_VOLUNTARIADO", "Cadastrou uma ação de voluntariado"),
                        "GET", List.of(
                                "CONSULTOU_ACOES_VOLUNTARIADO", "Consultou todas as ações de voluntariado")
                )
        );

        endpoints.put("/acoes-voluntariado/*", Map.of(
                        "PUT", List.of(
                                "ATUALIZOU_ACAO_VOLUNTARIADO", "Atualizou uma ação de voluntariado"),
                        "GET", List.of(
                                "CONSULTOU_ACAO_VOLUNTARIADO", "Consultou uma ação de voluntariado específica"),
                        "DELETE", List.of(
                                "EXCLUIU_ACAO_VOLUNTARIADO", "Excluiu uma ação de voluntariado")
                )
        );

        endpoints.put("/acoes-voluntariado/*/imagens", Map.of(
                        "POST", List.of(
                                "SALVOU_IMAGEM_ACAO_VOLUNTARIADO", "Salvou uma imagem em uma ação de voluntariado"),
                        "DELETE", List.of(
                                "EXCLUIU_IMAGEM_ACAO_VOLUNTARIADO", "Excluiu uma imagem de uma ação de voluntariado")
                )
        );

        endpoints.put("/acoes-voluntariado/*/opcoes-contribuicao", Map.of(
                        "POST", List.of(
                                "ADICIONOU_OPCAO_CONTRIBUICAO", "Adicionou opção de contribuição a uma ação de voluntariado")
                )
        );

        endpoints.put("/acoes-voluntariado/opcoes-contribuicao/*", Map.of(
                        "DELETE", List.of(
                                "EXCLUIU_OPCAO_CONTRIBUICAO", "Excluiu opção de contribuição de uma ação de voluntariado")
                )
        );

        endpoints.put("/doacoes", Map.of(
                        "POST", List.of(
                                "REALIZOU_DOACAO", "Realizou uma doação")
                )
        );

        endpoints.put("/doacoes/*", Map.of(
                        "GET", List.of(
                                "CONSULTOU_DOACAO", "Consultou uma doação específica"),
                        "POST", List.of(
                                "CANCELOU_DOACAO", "Cancelou uma doação")
                )
        );

        endpoints.put("/inscricoes", Map.of(
                        "POST", List.of(
                                "REALIZAR_INSCRICAO", "Realizou uma inscrição")
                )
        );

        endpoints.put("/inscricoes/*/cancelar", Map.of(
                        "DELETE", List.of(
                                "CANCELAR_INSCRICAO", "Cancelou uma inscrição")
                )
        );

        /* Acompanhamento */

        endpoints.put("/acompanhamentos", Map.of(
                "GET", List.of(
                        "CONSULTOU_ACOMPANHAMENTOS", "Consultou todos os acompanhamentos"
                ),
                "POST", List.of(
                        "CADASTROU_ACOMPANHAMENTO", "Cadastrou um acompanhamento"
                )
        ));

        endpoints.put("/acompanhamentos/*", Map.of(
                "GET", List.of(
                        "CONSULTOU_ACOMPANHAMENTO", "Consultou um acompanhamento específico cadastrado"
                ),
                "PUT", List.of(
                        "ATUALIZOU_ACOMPANHAMENTO", "Atualizou um acompanhamento cadastrado"
                ),
                "DELETE", List.of(
                        "EXCLUIU_ACOMPANHAMENTO", "Deletou um acompanhamento cadastrado"
                )
        ));

        endpoints.put("/acompanhamentos/*/arquivos", Map.of(
                "POST", List.of(
                        "SALVOU_ARQUIVO_ACOMPANHAMENTO", "Salvou um arquivo em um acompanhamento"
                )
        ));

        endpoints.put("/acompanhamentos/arquivos/*", Map.of(
                "GET", List.of(
                        "RECUPEROU_ARQUIVO_ACOMPANHAMENTO", "Recuperou um arquivo de um acompanhamento"
                ),
                "DELETE", List.of(
                        "EXCLUIU_ARQUIVO_ACOMPANHAMENTO", "Deletou um arquivo de um acompanhamento"
                )
        ));

        endpoints.put("/reunioes", Map.of(
                "POST", List.of(
                        "SOLICITOU_REUNIAO", "Solicitou uma reunião"
                )
        ));

        endpoints.put("/reunioes/*", Map.of(
                "PUT", List.of(
                        "AGENDOU_REUNIAO", "Agendou uma reunião"
                )
        ));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(()->{
            AtomicReference<String> action = new AtomicReference<>("NONE");
            AtomicReference<String> actionDescription = new AtomicReference<>("NONE");

            Boolean validRoute = endpoints.keySet().stream().anyMatch((e) -> {
                    if(new AntPathMatcher().match(e, exchange.getRequest().getPath().toString())) {

                        if(endpoints.get(e).get(exchange.getRequest().getMethod().name()) != null) {
                            action.set(endpoints.get(e).get(exchange.getRequest().getMethod().name()).get(0));
                            actionDescription.set(endpoints.get(e).get(exchange.getRequest().getMethod().name()).get(1));

                            return true;
                        }

                    }

                    return false;
            });

            if(validRoute) {
                LogDTO logDTO = new LogDTO(
                        action.toString(),
                        actionDescription.toString(),
                        exchange.getRequest().getRemoteAddress().getAddress().toString(),
                        exchange.getRequest().getHeaders().get("B3Social-Logging").toString(),
                        exchange.getRequest().getPath().toString(),
                        exchange.getRequest().getMethod().toString(),
                        exchange.getRequest().getHeaders().getOrigin(),
                        LocalDateTime.now().toString(),
                        exchange.getResponse().getStatusCode().toString(),
                        exchange.getRequest().getHeaders().get("B3Social-User").toString(),
                        exchange.getRequest().getHeaders().get("B3Social-User-Name").toString()
                );

                rabbitTemplate.convertAndSend("api-gateway.ex","", logDTO);
            }
        }));
    }
}
