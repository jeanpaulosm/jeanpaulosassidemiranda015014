package br.gov.mt.seplag.presentation.dto.regional;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO para resposta de sincronizacao de regionais.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Resultado da sincronizacao de regionais")
public class SincronizacaoResponse {

    @Schema(description = "Total de regionais processadas", example = "10")
    private int totalProcessadas;

    @Schema(description = "Regionais inseridas", example = "2")
    private int inseridas;

    @Schema(description = "Regionais atualizadas", example = "1")
    private int atualizadas;

    @Schema(description = "Regionais inativadas", example = "3")
    private int inativadas;

    @Schema(description = "Regionais sem alteracao", example = "4")
    private int semAlteracao;

    @Schema(description = "Mensagem de status", example = "Sincronizacao concluida com sucesso")
    private String mensagem;

    public SincronizacaoResponse() {
    }

    public SincronizacaoResponse(int totalProcessadas, int inseridas, int atualizadas, int inativadas, int semAlteracao) {
        this.totalProcessadas = totalProcessadas;
        this.inseridas = inseridas;
        this.atualizadas = atualizadas;
        this.inativadas = inativadas;
        this.semAlteracao = semAlteracao;
        this.mensagem = "Sincronizacao concluida com sucesso";
    }

    public int getTotalProcessadas() {
        return totalProcessadas;
    }

    public void setTotalProcessadas(int totalProcessadas) {
        this.totalProcessadas = totalProcessadas;
    }

    public int getInseridas() {
        return inseridas;
    }

    public void setInseridas(int inseridas) {
        this.inseridas = inseridas;
    }

    public int getAtualizadas() {
        return atualizadas;
    }

    public void setAtualizadas(int atualizadas) {
        this.atualizadas = atualizadas;
    }

    public int getInativadas() {
        return inativadas;
    }

    public void setInativadas(int inativadas) {
        this.inativadas = inativadas;
    }

    public int getSemAlteracao() {
        return semAlteracao;
    }

    public void setSemAlteracao(int semAlteracao) {
        this.semAlteracao = semAlteracao;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
