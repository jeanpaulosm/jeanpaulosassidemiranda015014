package br.gov.mt.seplag.application.service;

import br.gov.mt.seplag.domain.exception.BusinessException;
import br.gov.mt.seplag.domain.model.Regional;
import br.gov.mt.seplag.domain.repository.RegionalRepository;
import br.gov.mt.seplag.infrastructure.client.RegionaisClient;
import br.gov.mt.seplag.infrastructure.client.RegionaisClient.RegionalExterna;
import br.gov.mt.seplag.presentation.dto.regional.RegionalResponse;
import br.gov.mt.seplag.presentation.dto.regional.SincronizacaoResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servico de regionais com sincronizacao otimizada.
 *
 * Logica de sincronizacao conforme especificado no edital:
 * 1. Novo no endpoint     -> INSERT na tabela local
 * 2. Ausente no endpoint  -> UPDATE ativo = false na tabela local
 * 3. Atributo alterado    -> UPDATE com novo valor na tabela local
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class RegionalService {

    private static final Logger LOG = Logger.getLogger(RegionalService.class);

    @Inject
    RegionalRepository regionalRepository;

    @Inject
    @RestClient
    RegionaisClient regionaisClient;

    /**
     * Lista todas as regionais.
     *
     * @param apenasAtivas Se true, retorna apenas regionais ativas
     */
    public List<RegionalResponse> listar(Boolean apenasAtivas) {
        LOG.debugf("Listando regionais - apenasAtivas: %s", apenasAtivas);

        List<Regional> regionais = Boolean.TRUE.equals(apenasAtivas)
            ? regionalRepository.findAllAtivas()
            : regionalRepository.findAllOrdenadas();

        return regionais.stream()
            .map(RegionalResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Busca uma regional por ID.
     */
    public RegionalResponse buscarPorId(Integer id) {
        LOG.debugf("Buscando regional por ID: %d", id);

        Regional regional = regionalRepository.findByIdOptional(id)
            .orElseThrow(() -> new BusinessException("Regional nao encontrada: " + id));

        return RegionalResponse.fromEntity(regional);
    }

    /**
     * Sincroniza regionais com a API externa.
     *
     * Algoritmo com complexidade O(n + m):
     * - Carrega dados em memoria e indexa em HashMap
     * - Processa cada regional externa com lookup O(1)
     * - Inativa ausentes via query unica
     */
    @Transactional
    public SincronizacaoResponse sincronizar() {
        LOG.info("=== Iniciando sincronizacao de regionais ===");
        long startTime = System.currentTimeMillis();

        try {
            // Busca regionais da API externa
            List<RegionalExterna> regionaisExternas = regionaisClient.getRegionais();
            LOG.infof("Regionais externas recebidas: %d", regionaisExternas.size());

            // Busca todas as regionais locais indexadas por ID
            Map<Integer, Regional> regionaisLocais = regionalRepository.findAllAsMap();
            LOG.infof("Regionais locais encontradas: %d", regionaisLocais.size());

            int inseridas = 0;
            int atualizadas = 0;
            int reativadas = 0;
            int semAlteracao = 0;

            Set<Integer> idsExternos = new HashSet<>();

            for (RegionalExterna externa : regionaisExternas) {
                idsExternos.add(externa.getId());
                Regional local = regionaisLocais.get(externa.getId());

                if (local == null) {
                    // CASO 1: Nova regional -> INSERT
                    Regional nova = Regional.fromExternal(externa.getId(), externa.getNome());
                    regionalRepository.persist(nova);
                    inseridas++;
                    LOG.debugf("[INSERT] Nova regional: ID=%d, Nome='%s'",
                        externa.getId(), externa.getNome());

                } else if (!local.getAtivo()) {
                    // Regional existe mas esta inativa - reativar
                    if (local.nomeAlterado(externa.getNome())) {
                        local.atualizarNome(externa.getNome());
                        local.reativar();
                        regionalRepository.persist(local);
                        atualizadas++;
                        LOG.debugf("[UPDATE+REATIVAR] Regional reativada com nome alterado: ID=%d", local.getId());
                    } else {
                        local.reativar();
                        regionalRepository.persist(local);
                        reativadas++;
                        LOG.debugf("[REATIVAR] Regional reativada: ID=%d", local.getId());
                    }

                } else if (local.nomeAlterado(externa.getNome())) {
                    // CASO 3: Atributo alterado -> UPDATE
                    String nomeAntigo = local.getNome();
                    local.atualizarNome(externa.getNome());
                    regionalRepository.persist(local);
                    atualizadas++;
                    LOG.debugf("[UPDATE] Regional atualizada: ID=%d, '%s' -> '%s'",
                        local.getId(), nomeAntigo, externa.getNome());

                } else {
                    semAlteracao++;
                }
            }

            // CASO 2: Ausente no endpoint -> INATIVAR
            int inativadas = regionalRepository.inativarAusentes(idsExternos);
            if (inativadas > 0) {
                LOG.infof("[INATIVAR] %d regionais inativadas (ausentes no endpoint)", inativadas);
            }

            long duration = System.currentTimeMillis() - startTime;

            SincronizacaoResponse response = new SincronizacaoResponse(
                regionaisExternas.size(),
                inseridas,
                atualizadas,
                inativadas,
                semAlteracao
            );

            LOG.infof("=== Sincronizacao concluida em %dms ===", duration);
            LOG.infof("Resultado: Total=%d, Inseridas=%d, Atualizadas=%d, Reativadas=%d, Inativadas=%d, SemAlteracao=%d",
                regionaisExternas.size(), inseridas, atualizadas, reativadas, inativadas, semAlteracao);

            return response;

        } catch (Exception e) {
            LOG.errorf(e, "Erro na sincronizacao de regionais: %s", e.getMessage());
            throw new BusinessException("Erro ao sincronizar regionais: " + e.getMessage(), e);
        }
    }

    /**
     * Retorna estatisticas das regionais.
     */
    public Map<String, Long> getEstatisticas() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", regionalRepository.count());
        stats.put("ativas", regionalRepository.countAtivas());
        stats.put("inativas", regionalRepository.countInativas());
        return stats;
    }
}
