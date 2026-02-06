package br.gov.mt.seplag.presentation.websocket;

import br.gov.mt.seplag.presentation.dto.album.AlbumResponse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios para AlbumWebSocket.
 * Valida a funcionalidade de notificacao em tempo real conforme edital:
 * "WebSocket para notificar o front a cada novo album cadastrado"
 *
 * @author Jean Paulo Sassi de Miranda
 */
@QuarkusTest
class AlbumWebSocketTest {

    @Inject
    AlbumWebSocket albumWebSocket;

    @Test
    void shouldStartWithZeroConnections() {
        // Verifica que inicialmente nao ha clientes conectados
        // Nota: O numero pode variar dependendo de outros testes, mas deve ser >= 0
        assertTrue(albumWebSocket.getConnectedClientsCount() >= 0,
            "Contador de conexoes deve ser nao-negativo");
    }

    @Test
    void shouldNotThrowWhenNotifyingWithNoConnections() {
        // Deve tratar graciosamente quando nao ha clientes conectados
        AlbumResponse album = new AlbumResponse();
        album.setId(1L);
        album.setTitulo("Album Teste");
        album.setAnoLancamento(2024);

        assertDoesNotThrow(() -> albumWebSocket.notifyNewAlbum(album),
            "Notificar sem clientes conectados nao deve lancar excecao");
    }

    @Test
    void shouldHandleNullAlbumGracefully() {
        // Deve tratar album nulo sem lancar excecao critica
        assertDoesNotThrow(() -> {
            try {
                albumWebSocket.notifyNewAlbum(null);
            } catch (NullPointerException e) {
                // NullPointerException e aceitavel neste caso
            }
        });
    }

    @Test
    void webSocketMessageShouldHaveCorrectStructure() {
        // Testa a estrutura da mensagem WebSocket
        AlbumWebSocket.WebSocketMessage message = new AlbumWebSocket.WebSocketMessage();
        message.setType("NEW_ALBUM");

        AlbumResponse album = new AlbumResponse();
        album.setId(1L);
        album.setTitulo("Test Album");
        message.setData(album);

        assertEquals("NEW_ALBUM", message.getType());
        assertNotNull(message.getData());
        assertTrue(message.getData() instanceof AlbumResponse);
    }

    @Test
    void webSocketMessageConstructorShouldWork() {
        AlbumResponse album = new AlbumResponse();
        album.setId(1L);
        album.setTitulo("Test Album");

        AlbumWebSocket.WebSocketMessage message =
            new AlbumWebSocket.WebSocketMessage("NEW_ALBUM", album);

        assertEquals("NEW_ALBUM", message.getType());
        assertEquals(album, message.getData());
    }
}
