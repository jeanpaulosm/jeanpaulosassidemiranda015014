package br.gov.mt.seplag.domain.exception;

/**
 * Excecao para limite de requisicoes excedido.
 *
 * @author Jean Paulo Sassi de Miranda
 */
public class RateLimitExceededException extends RuntimeException {

    private final int maxRequests;
    private final int windowSeconds;

    public RateLimitExceededException(int maxRequests, int windowSeconds) {
        super(String.format("Limite de %d requisicoes por %d segundos excedido. Tente novamente mais tarde.",
            maxRequests, windowSeconds));
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }
}
