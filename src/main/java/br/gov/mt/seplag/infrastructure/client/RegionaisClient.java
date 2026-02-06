package br.gov.mt.seplag.infrastructure.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * Cliente REST para a API de regionais da Policia Civil.
 * Implementa padroes de resiliencia: Circuit Breaker, Retry e Timeout.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Path("/v1")
@RegisterRestClient(configKey = "regionais-api")
public interface RegionaisClient {

    /**
     * Busca lista de regionais da API externa.
     *
     * Configuracoes de resiliencia:
     * - Timeout: 10 segundos
     * - Retry: 3 tentativas com delay de 1 segundo
     * - Circuit Breaker: Abre apos 50% de falhas em 10 requisicoes, espera 30s
     */
    @GET
    @Path("/regionais")
    @Produces(MediaType.APPLICATION_JSON)
    @Timeout(10000)
    @Retry(maxRetries = 3, delay = 1000)
    @CircuitBreaker(
        requestVolumeThreshold = 10,
        failureRatio = 0.5,
        delay = 30000,
        successThreshold = 3
    )
    List<RegionalExterna> getRegionais();

    /**
     * DTO para representar a regional da API externa.
     */
    class RegionalExterna {
        private Integer id;
        private String nome;

        public RegionalExterna() {
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }
    }
}
