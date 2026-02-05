package br.gov.mt.seplag.presentation.dto.common;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * DTO generico para resposta paginada.
 * Segue o padrao do Spring Data mas implementado manualmente pro Quarkus.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Resposta paginada")
public class PageResponse<T> {

    @Schema(description = "Lista de itens da pagina")
    private List<T> content;

    @Schema(description = "Numero da pagina atual (0-based)", example = "0")
    private int page;

    @Schema(description = "Tamanho da pagina", example = "10")
    private int size;

    @Schema(description = "Total de elementos", example = "100")
    private long totalElements;

    @Schema(description = "Total de paginas", example = "10")
    private int totalPages;

    @Schema(description = "Indica se e a primeira pagina", example = "true")
    private boolean first;

    @Schema(description = "Indica se e a ultima pagina", example = "false")
    private boolean last;

    public PageResponse() {
    }

    public PageResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        this.first = page == 0;
        this.last = page >= totalPages - 1;
    }

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        return new PageResponse<>(content, page, size, totalElements);
    }

    // Getters e Setters
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}
