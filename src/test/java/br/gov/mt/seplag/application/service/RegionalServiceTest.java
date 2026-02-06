package br.gov.mt.seplag.application.service;

import br.gov.mt.seplag.domain.exception.BusinessException;
import br.gov.mt.seplag.domain.model.Regional;
import br.gov.mt.seplag.domain.repository.RegionalRepository;
import br.gov.mt.seplag.infrastructure.client.RegionaisClient;
import br.gov.mt.seplag.infrastructure.client.RegionaisClient.RegionalExterna;
import br.gov.mt.seplag.infrastructure.metrics.MetricsService;
import br.gov.mt.seplag.presentation.dto.regional.RegionalResponse;
import br.gov.mt.seplag.presentation.dto.regional.SincronizacaoResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios para RegionalService.
 * Cobertura da logica de sincronizacao conforme edital:
 * 1. Novo no endpoint -> INSERT
 * 2. Ausente no endpoint -> UPDATE ativo=false
 * 3. Atributo alterado -> UPDATE nome
 *
 * Estrutura da tabela conforme edital:
 * regional (id integer, nome varchar(200), ativo boolean)
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
@DisplayName("RegionalService - Testes Unitarios")
class RegionalServiceTest {

    @Inject
    RegionalService regionalService;

    @InjectMock
    RegionalRepository regionalRepository;

    @InjectMock
    @RestClient
    RegionaisClient regionaisClient;

    @InjectMock
    MetricsService metricsService;

    private Regional criarRegional(Integer id, String nome, boolean ativo) {
        Regional r = new Regional();
        r.setId(id);
        r.setNome(nome);
        r.setAtivo(ativo);
        return r;
    }

    private RegionalExterna criarRegionalExterna(Integer id, String nome) {
        RegionalExterna r = new RegionalExterna();
        r.setId(id);
        r.setNome(nome);
        return r;
    }

    // ====================
    // TESTES DE LISTAGEM
    // ====================

    @Nested
    @DisplayName("Listagem de Regionais")
    class ListagemTests {

        @Test
        @DisplayName("Deve listar apenas regionais ativas quando filtro ativado")
        void shouldListOnlyActiveRegionaisWhenFilterEnabled() {
            // Arrange
            Regional ativa = criarRegional(100, "Regional Ativa", true);
            when(regionalRepository.findAllAtivas()).thenReturn(List.of(ativa));

            // Act
            List<RegionalResponse> result = regionalService.listar(true);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getNome()).isEqualTo("Regional Ativa");
            assertThat(result.get(0).getId()).isEqualTo(100);
            verify(regionalRepository).findAllAtivas();
        }

        @Test
        @DisplayName("Deve listar todas regionais quando filtro desativado")
        void shouldListAllRegionaisWhenFilterDisabled() {
            // Arrange
            Regional ativa = criarRegional(100, "Regional Ativa", true);
            Regional inativa = criarRegional(101, "Regional Inativa", false);
            when(regionalRepository.findAllOrdenadas()).thenReturn(List.of(ativa, inativa));

            // Act
            List<RegionalResponse> result = regionalService.listar(false);

            // Assert
            assertThat(result).hasSize(2);
            verify(regionalRepository).findAllOrdenadas();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nao existem regionais")
        void shouldReturnEmptyListWhenNoRegionais() {
            // Arrange
            when(regionalRepository.findAllAtivas()).thenReturn(Collections.emptyList());

            // Act
            List<RegionalResponse> result = regionalService.listar(true);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    // ====================
    // TESTES DE SINCRONIZACAO - CASO 1: NOVA REGIONAL
    // ====================

    @Nested
    @DisplayName("Sincronizacao - Caso 1: Nova Regional (INSERT)")
    class SincronizacaoCaso1Tests {

        @Test
        @DisplayName("Deve inserir nova regional quando nao existe localmente")
        void shouldInsertNewRegionalWhenNotExistsLocally() {
            // Arrange
            RegionalExterna externa = criarRegionalExterna(100, "Nova Regional");
            when(regionaisClient.getRegionais()).thenReturn(List.of(externa));
            when(regionalRepository.findAllAsMap()).thenReturn(new HashMap<>());
            when(regionalRepository.inativarAusentes(anySet())).thenReturn(0);

            doNothing().when(regionalRepository).persist(any(Regional.class));

            // Act
            SincronizacaoResponse result = regionalService.sincronizar();

            // Assert
            assertThat(result.getInseridas()).isEqualTo(1);
            assertThat(result.getAtualizadas()).isEqualTo(0);
            assertThat(result.getInativadas()).isEqualTo(0);

            ArgumentCaptor<Regional> captor = ArgumentCaptor.forClass(Regional.class);
            verify(regionalRepository).persist(captor.capture());

            Regional inserted = captor.getValue();
            assertThat(inserted.getId()).isEqualTo(100);
            assertThat(inserted.getNome()).isEqualTo("Nova Regional");
            assertThat(inserted.getAtivo()).isTrue();
        }

        @Test
        @DisplayName("Deve inserir multiplas novas regionais")
        void shouldInsertMultipleNewRegionais() {
            // Arrange
            List<RegionalExterna> externas = List.of(
                criarRegionalExterna(100, "Regional 1"),
                criarRegionalExterna(101, "Regional 2"),
                criarRegionalExterna(102, "Regional 3")
            );
            when(regionaisClient.getRegionais()).thenReturn(externas);
            when(regionalRepository.findAllAsMap()).thenReturn(new HashMap<>());
            when(regionalRepository.inativarAusentes(anySet())).thenReturn(0);

            doNothing().when(regionalRepository).persist(any(Regional.class));

            // Act
            SincronizacaoResponse result = regionalService.sincronizar();

            // Assert
            assertThat(result.getInseridas()).isEqualTo(3);
            verify(regionalRepository, times(3)).persist(any(Regional.class));
        }
    }

    // ====================
    // TESTES DE SINCRONIZACAO - CASO 2: INATIVAR REGIONAL
    // ====================

    @Nested
    @DisplayName("Sincronizacao - Caso 2: Inativar Regional (UPDATE ativo=false)")
    class SincronizacaoCaso2Tests {

        @Test
        @DisplayName("Deve inativar regionais ausentes no endpoint externo via batch")
        void shouldInactivateRegionaisAbsentFromEndpoint() {
            // Arrange
            when(regionaisClient.getRegionais()).thenReturn(Collections.emptyList());

            Regional localAtiva = criarRegional(100, "Regional Local", true);
            Map<Integer, Regional> locaisMap = new HashMap<>();
            locaisMap.put(100, localAtiva);
            when(regionalRepository.findAllAsMap()).thenReturn(locaisMap);
            when(regionalRepository.inativarAusentes(anySet())).thenReturn(1);

            // Act
            SincronizacaoResponse result = regionalService.sincronizar();

            // Assert
            assertThat(result.getInativadas()).isEqualTo(1);
            assertThat(result.getInseridas()).isEqualTo(0);

            // Verifica que inativarAusentes foi chamado com Set vazio (nenhum ID externo)
            ArgumentCaptor<Set<Integer>> captor = ArgumentCaptor.forClass(Set.class);
            verify(regionalRepository).inativarAusentes(captor.capture());
            assertThat(captor.getValue()).isEmpty();
        }
    }

    // ====================
    // TESTES DE SINCRONIZACAO - CASO 3: ATRIBUTO ALTERADO
    // ====================

    @Nested
    @DisplayName("Sincronizacao - Caso 3: Atributo Alterado (UPDATE nome)")
    class SincronizacaoCaso3Tests {

        @Test
        @DisplayName("Deve atualizar nome quando alterado na API externa")
        void shouldUpdateNameWhenChangedInExternalApi() {
            // Arrange
            RegionalExterna externa = criarRegionalExterna(100, "Nome Atualizado");
            when(regionaisClient.getRegionais()).thenReturn(List.of(externa));

            Regional localAtiva = criarRegional(100, "Nome Antigo", true);
            Map<Integer, Regional> locaisMap = new HashMap<>();
            locaisMap.put(100, localAtiva);
            when(regionalRepository.findAllAsMap()).thenReturn(locaisMap);
            when(regionalRepository.inativarAusentes(anySet())).thenReturn(0);

            // Act
            SincronizacaoResponse result = regionalService.sincronizar();

            // Assert
            assertThat(result.getAtualizadas()).isEqualTo(1);
            assertThat(result.getInseridas()).isEqualTo(0);
            assertThat(result.getInativadas()).isEqualTo(0);

            // Verifica que o nome foi atualizado no objeto
            assertThat(localAtiva.getNome()).isEqualTo("Nome Atualizado");
        }
    }

    // ====================
    // TESTES DE SINCRONIZACAO - SEM ALTERACAO
    // ====================

    @Nested
    @DisplayName("Sincronizacao - Sem Alteracao")
    class SincronizacaoSemAlteracaoTests {

        @Test
        @DisplayName("Nao deve alterar regional quando dados sao identicos")
        void shouldNotChangeRegionalWhenDataIsIdentical() {
            // Arrange
            RegionalExterna externa = criarRegionalExterna(100, "Mesma Regional");
            when(regionaisClient.getRegionais()).thenReturn(List.of(externa));

            Regional localAtiva = criarRegional(100, "Mesma Regional", true);
            Map<Integer, Regional> locaisMap = new HashMap<>();
            locaisMap.put(100, localAtiva);
            when(regionalRepository.findAllAsMap()).thenReturn(locaisMap);
            when(regionalRepository.inativarAusentes(anySet())).thenReturn(0);

            // Act
            SincronizacaoResponse result = regionalService.sincronizar();

            // Assert
            assertThat(result.getSemAlteracao()).isEqualTo(1);
            assertThat(result.getInseridas()).isEqualTo(0);
            assertThat(result.getAtualizadas()).isEqualTo(0);
            assertThat(result.getInativadas()).isEqualTo(0);

            // Nao deve ter persistido nenhum registro
            verify(regionalRepository, never()).persist(any(Regional.class));
        }
    }

    // ====================
    // TESTES DE SINCRONIZACAO - REATIVACAO
    // ====================

    @Nested
    @DisplayName("Sincronizacao - Reativacao de Regional Inativa")
    class SincronizacaoReativacaoTests {

        @Test
        @DisplayName("Deve reativar regional inativa quando volta na API")
        void shouldReactivateInactiveRegionalWhenReturnsInApi() {
            // Arrange
            RegionalExterna externa = criarRegionalExterna(100, "Regional Reativada");
            when(regionaisClient.getRegionais()).thenReturn(List.of(externa));

            Regional localInativa = criarRegional(100, "Regional Reativada", false);
            Map<Integer, Regional> locaisMap = new HashMap<>();
            locaisMap.put(100, localInativa);
            when(regionalRepository.findAllAsMap()).thenReturn(locaisMap);
            when(regionalRepository.inativarAusentes(anySet())).thenReturn(0);

            doNothing().when(regionalRepository).persist(any(Regional.class));

            // Act
            SincronizacaoResponse result = regionalService.sincronizar();

            // Assert - Verifica que a regional foi reativada (o serviço contabiliza internamente)
            // Nota: reativadas nao e exposta no DTO, mas podemos verificar que a regional foi modificada
            assertThat(localInativa.getAtivo()).isTrue();
            verify(regionalRepository).persist(localInativa);
        }
    }

    // ====================
    // TESTES DE ERRO
    // ====================

    @Nested
    @DisplayName("Tratamento de Erros")
    class TratamentoErrosTests {

        @Test
        @DisplayName("Deve lancar BusinessException quando API externa falha")
        void shouldThrowBusinessExceptionWhenExternalApiFails() {
            // Arrange
            when(regionaisClient.getRegionais()).thenThrow(new RuntimeException("Erro de conexao"));

            // Act & Assert
            assertThatThrownBy(() -> regionalService.sincronizar())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Erro ao sincronizar regionais");
        }
    }

    // ====================
    // TESTES DE ESTATISTICAS
    // ====================

    @Nested
    @DisplayName("Estatisticas")
    class EstatisticasTests {

        @Test
        @DisplayName("Deve retornar estatisticas corretas")
        void shouldReturnCorrectStatistics() {
            // Arrange
            when(regionalRepository.count()).thenReturn(10L);
            when(regionalRepository.countAtivas()).thenReturn(8L);
            when(regionalRepository.countInativas()).thenReturn(2L);

            // Act
            Map<String, Long> stats = regionalService.getEstatisticas();

            // Assert
            assertThat(stats.get("total")).isEqualTo(10L);
            assertThat(stats.get("ativas")).isEqualTo(8L);
            assertThat(stats.get("inativas")).isEqualTo(2L);
        }
    }
}
