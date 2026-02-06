package br.gov.mt.seplag.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

/**
 * Entidade que representa uma regional da Policia Civil.
 * Sincronizada com API externa (https://integrador-argus-api.geia.vip/v1/regionais).
 *
 * Estrutura conforme especificado no edital:
 * "regional (id integer, nome varchar(200), ativo boolean)"
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Entity
@Table(name = "regional", indexes = {
    @Index(name = "idx_regional_nome", columnList = "nome"),
    @Index(name = "idx_regional_ativo", columnList = "ativo")
})
public class Regional extends PanacheEntityBase {

    /**
     * ID da regional conforme API externa.
     * Nao e auto-gerado; recebe o valor diretamente da API.
     */
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    /**
     * Indica se a regional esta ativa.
     * false = inativada durante sincronizacao (ausente no endpoint).
     */
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    public Regional() {
    }

    public Regional(Integer id, String nome, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.ativo = ativo;
    }

    /**
     * Cria uma nova regional a partir dos dados da API externa.
     */
    public static Regional fromExternal(Integer idExterno, String nome) {
        Regional regional = new Regional();
        regional.setId(idExterno);
        regional.setNome(nome);
        regional.setAtivo(true);
        return regional;
    }

    /**
     * Inativa a regional.
     * Chamado quando a regional nao esta mais presente no endpoint externo.
     */
    public void inativar() {
        this.ativo = false;
    }

    /**
     * Reativa a regional.
     * Chamado quando uma regional inativa volta a aparecer no endpoint externo.
     */
    public void reativar() {
        this.ativo = true;
    }

    /**
     * Atualiza o nome da regional.
     */
    public void atualizarNome(String novoNome) {
        this.nome = novoNome;
    }

    /**
     * Verifica se o nome foi alterado.
     */
    public boolean nomeAlterado(String outroNome) {
        return !this.nome.equals(outroNome);
    }

    // Getters and Setters

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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Regional regional = (Regional) o;
        return id != null && id.equals(regional.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Regional{" +
            "id=" + id +
            ", nome='" + nome + '\'' +
            ", ativo=" + ativo +
            '}';
    }
}
