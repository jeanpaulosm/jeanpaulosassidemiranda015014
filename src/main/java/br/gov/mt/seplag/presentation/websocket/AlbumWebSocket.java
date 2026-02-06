package br.gov.mt.seplag.presentation.websocket;

import br.gov.mt.seplag.domain.model.WebSocketTicket;
import br.gov.mt.seplag.infrastructure.security.WebSocketTicketService;
import br.gov.mt.seplag.presentation.dto.album.AlbumResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.Arc;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket para notificacao de novos albuns em tempo real.
 *
 * AUTENTICACAO VIA TICKET SYSTEM:
 * 1. Cliente obtem ticket via POST /api/v1/ws/ticket (com JWT)
 * 2. Cliente conecta: ws://host/ws/albuns?ticket=xxx
 * 3. Ticket e validado e consumido (single-use, 30s TTL)
 *
 * Seguranca:
 * - Conexoes sem ticket ou com ticket invalido sao rejeitadas
 * - Tickets sao UUIDs com 122 bits de entropia
 * - Single-use: nao podem ser reutilizados
 * - Curta duracao: expiram em 30 segundos
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ServerEndpoint(value = "/ws/albuns", configurator = AlbumWebSocket.TicketConfigurator.class)
@ApplicationScoped
public class AlbumWebSocket {

    private static final Logger LOG = Logger.getLogger(AlbumWebSocket.class);

    // Chaves para propriedades da sessao
    private static final String USER_PROPERTY_USERNAME = "username";
    private static final String USER_PROPERTY_ROLES = "roles";
    private static final String USER_PROPERTY_AUTHENTICATED = "authenticated";

    /**
     * Sessoes ativas indexadas por ID.
     * Armazena apenas conexoes autenticadas com sucesso.
     */
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * Mapa de sessoes por username para rastreabilidade.
     */
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    @Inject
    ObjectMapper objectMapper;

    @Inject
    WebSocketTicketService ticketService;

    // =========================================================================
    // Lifecycle Handlers
    // =========================================================================

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String sessionId = session.getId();

        // Extrai ticket da query string
        String ticket = extractTicketFromQuery(session);

        if (ticket == null || ticket.isBlank()) {
            LOG.warnf("Conexao WebSocket rejeitada - ticket ausente. Session: %s", sessionId);
            closeWithReason(session, CloseReason.CloseCodes.VIOLATED_POLICY, "Ticket ausente. Use POST /api/v1/ws/ticket para obter um ticket.");
            return;
        }

        // Valida e consome o ticket
        Optional<WebSocketTicket> validTicket = ticketService.validateAndConsume(ticket);

        if (validTicket.isEmpty()) {
            LOG.warnf("Conexao WebSocket rejeitada - ticket invalido. Session: %s, Ticket: %s...",
                sessionId, ticket.substring(0, Math.min(8, ticket.length())));
            closeWithReason(session, CloseReason.CloseCodes.VIOLATED_POLICY, "Ticket invalido, expirado ou ja utilizado.");
            return;
        }

        // Ticket valido - armazena informacoes do usuario na sessao
        WebSocketTicket wsTicket = validTicket.get();
        session.getUserProperties().put(USER_PROPERTY_USERNAME, wsTicket.getUsername());
        session.getUserProperties().put(USER_PROPERTY_ROLES, wsTicket.getRoles());
        session.getUserProperties().put(USER_PROPERTY_AUTHENTICATED, true);

        // Registra sessao
        sessions.put(sessionId, session);
        userSessions.computeIfAbsent(wsTicket.getUsername(), k -> ConcurrentHashMap.newKeySet()).add(sessionId);

        LOG.infof("WebSocket conectado - User: %s, Session: %s, Total conexoes: %d",
            wsTicket.getUsername(), sessionId, sessions.size());

        // Envia mensagem de boas-vindas com info do usuario
        sendMessage(session, createWelcomeMessage(wsTicket.getUsername(), wsTicket.getRoles()));
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        String sessionId = session.getId();
        String username = getUsername(session);

        sessions.remove(sessionId);

        // Remove da lista de sessoes do usuario
        if (username != null) {
            Set<String> userSessionIds = userSessions.get(username);
            if (userSessionIds != null) {
                userSessionIds.remove(sessionId);
                if (userSessionIds.isEmpty()) {
                    userSessions.remove(username);
                }
            }
        }

        LOG.infof("WebSocket desconectado - User: %s, Session: %s, Reason: %s, Total conexoes: %d",
            username != null ? username : "N/A",
            sessionId,
            closeReason.getReasonPhrase(),
            sessions.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        String sessionId = session != null ? session.getId() : "unknown";
        String username = session != null ? getUsername(session) : "unknown";

        LOG.errorf("Erro no WebSocket - User: %s, Session: %s, Erro: %s",
            username, sessionId, throwable.getMessage());

        if (session != null) {
            sessions.remove(sessionId);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        String username = getUsername(session);
        LOG.debugf("Mensagem recebida - User: %s, Mensagem: %s", username, message);

        // Heartbeat
        if ("ping".equalsIgnoreCase(message)) {
            sendMessage(session, "{\"type\":\"pong\"}");
        }
    }

    // =========================================================================
    // Notificacoes
    // =========================================================================

    /**
     * Notifica todos os clientes autenticados sobre um novo album.
     *
     * @param album dados do novo album
     */
    public void notifyNewAlbum(AlbumResponse album) {
        if (sessions.isEmpty()) {
            LOG.debug("Nenhum cliente WebSocket conectado para notificar");
            return;
        }

        try {
            WebSocketMessage message = new WebSocketMessage("NEW_ALBUM", album);
            String jsonMessage = objectMapper.writeValueAsString(message);

            LOG.infof("Notificando %d clientes sobre novo album: %s", sessions.size(), album.getTitulo());

            sessions.values().forEach(session -> {
                if (session.isOpen()) {
                    sendMessage(session, jsonMessage);
                }
            });
        } catch (Exception e) {
            LOG.error("Erro ao serializar mensagem WebSocket", e);
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Extrai o ticket da query string da sessao.
     */
    private String extractTicketFromQuery(Session session) {
        Map<String, List<String>> params = session.getRequestParameterMap();
        List<String> ticketParams = params.get("ticket");
        if (ticketParams != null && !ticketParams.isEmpty()) {
            return ticketParams.get(0);
        }
        return null;
    }

    /**
     * Fecha a sessao com uma razao especifica.
     */
    private void closeWithReason(Session session, CloseReason.CloseCode code, String reason) {
        try {
            session.close(new CloseReason(code, reason));
        } catch (IOException e) {
            LOG.errorf("Erro ao fechar sessao WebSocket: %s", e.getMessage());
        }
    }

    /**
     * Envia mensagem para um cliente especifico.
     */
    private void sendMessage(Session session, String message) {
        try {
            session.getAsyncRemote().sendText(message, result -> {
                if (result.getException() != null) {
                    LOG.errorf("Erro ao enviar mensagem WebSocket: %s", result.getException().getMessage());
                }
            });
        } catch (Exception e) {
            LOG.errorf("Erro ao enviar mensagem WebSocket: %s", e.getMessage());
        }
    }

    /**
     * Cria mensagem de boas-vindas com informacoes do usuario.
     */
    private String createWelcomeMessage(String username, Set<String> roles) {
        try {
            Map<String, Object> data = Map.of(
                "username", username,
                "roles", roles,
                "message", "Conectado ao WebSocket de albuns. Voce sera notificado sobre novos albuns cadastrados."
            );
            WebSocketMessage message = new WebSocketMessage("CONNECTED", data);
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            return "{\"type\":\"CONNECTED\",\"data\":{\"username\":\"" + username + "\",\"message\":\"Conectado com sucesso.\"}}";
        }
    }

    /**
     * Obtem o username da sessao.
     */
    private String getUsername(Session session) {
        Object username = session.getUserProperties().get(USER_PROPERTY_USERNAME);
        return username != null ? username.toString() : null;
    }

    /**
     * Retorna o numero de clientes conectados.
     */
    public int getConnectedClientsCount() {
        return sessions.size();
    }

    /**
     * Retorna o numero de usuarios unicos conectados.
     */
    public int getUniqueUsersCount() {
        return userSessions.size();
    }

    // =========================================================================
    // Configurator para extrair parametros da requisicao
    // =========================================================================

    /**
     * Configurator customizado para habilitar acesso aos parametros da query.
     * Necessario para extrair o ticket da URL de conexao.
     */
    public static class TicketConfigurator extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig sec, jakarta.websocket.server.HandshakeRequest request, HandshakeResponse response) {
            // Parametros da query ja sao acessiveis via session.getRequestParameterMap()
            // Este configurator garante que o handshake seja processado corretamente
            super.modifyHandshake(sec, request, response);
        }

        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            // Usa CDI para obter a instancia do endpoint (necessario para @Inject funcionar)
            return Arc.container().instance(endpointClass).get();
        }
    }

    // =========================================================================
    // Message Class
    // =========================================================================

    /**
     * Classe para mensagens WebSocket.
     */
    public static class WebSocketMessage {
        private String type;
        private Object data;

        public WebSocketMessage() {
        }

        public WebSocketMessage(String type, Object data) {
            this.type = type;
            this.data = data;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
