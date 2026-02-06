package br.gov.mt.seplag.infrastructure.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Encoder de senhas usando BCrypt.
 *
 * BCrypt eh considerado o padrao para hashing de senhas porque:
 * - Tem custo computacional configuravel (work factor)
 * - Gera salt automaticamente e o inclui no hash
 * - Eh resistente a ataques de forca bruta e rainbow tables
 *
 * @author Jean Paulo Sassi de Miranda
 */
@ApplicationScoped
public class PasswordEncoder {

    /**
     * Work factor do BCrypt.
     * Valor 12 oferece bom equilibrio entre seguranca e performance.
     * Cada incremento dobra o tempo de calculo.
     */
    private static final int BCRYPT_WORK_FACTOR = 12;

    /**
     * Codifica uma senha usando BCrypt.
     *
     * @param rawPassword senha em texto plano
     * @return hash BCrypt da senha (inclui salt e work factor)
     */
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("A senha nao pode ser nula ou vazia");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
    }

    /**
     * Verifica se uma senha corresponde ao hash BCrypt.
     *
     * @param rawPassword     senha em texto plano
     * @param encodedPassword hash BCrypt armazenado
     * @return true se a senha corresponde ao hash
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Verifica se um hash precisa ser atualizado (ex: work factor diferente).
     *
     * @param encodedPassword hash BCrypt atual
     * @return true se o hash deve ser regenerado
     */
    public boolean needsRehash(String encodedPassword) {
        if (encodedPassword == null || !encodedPassword.startsWith("$2")) {
            return true;
        }

        try {
            String[] parts = encodedPassword.split("\\$");
            if (parts.length >= 3) {
                int currentWorkFactor = Integer.parseInt(parts[2]);
                return currentWorkFactor < BCRYPT_WORK_FACTOR;
            }
        } catch (NumberFormatException e) {
            return true;
        }

        return false;
    }
}
