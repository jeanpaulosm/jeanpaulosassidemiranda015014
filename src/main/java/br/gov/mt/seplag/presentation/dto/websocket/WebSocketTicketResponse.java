package br.gov.mt.seplag.presentation.dto.websocket;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO de resposta para criacao de ticket WebSocket.
 *
 * @author Jean Paulo Sassi de Miranda
 */
@Schema(description = "Resposta com ticket para autenticacao WebSocket")
public class WebSocketTicketResponse {

    @Schema(
        description = "Ticket UUID para autenticacao WebSocket (single-use)",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String ticket;

    @Schema(
        description = "Tempo de vida do ticket em segundos",
        example = "30"
    )
    private int expiresIn;

    @Schema(
        description = "URL do WebSocket para conexao",
        example = "ws://localhost:8080/ws/albuns?ticket=550e8400-e29b-41d4-a716-446655440000"
    )
    private String websocketUrl;

    @Schema(
        description = "Username do usuario autenticado",
        example = "admin"
    )
    private String username;

    public WebSocketTicketResponse() {
    }

    public WebSocketTicketResponse(String ticket, int expiresIn, String websocketUrl, String username) {
        this.ticket = ticket;
        this.expiresIn = expiresIn;
        this.websocketUrl = websocketUrl;
        this.username = username;
    }

    // =========================================================================
    // Builder Pattern
    // =========================================================================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String ticket;
        private int expiresIn;
        private String websocketUrl;
        private String username;

        public Builder ticket(String ticket) {
            this.ticket = ticket;
            return this;
        }

        public Builder expiresIn(int expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public Builder websocketUrl(String websocketUrl) {
            this.websocketUrl = websocketUrl;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public WebSocketTicketResponse build() {
            return new WebSocketTicketResponse(ticket, expiresIn, websocketUrl, username);
        }
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getWebsocketUrl() {
        return websocketUrl;
    }

    public void setWebsocketUrl(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
