package dominio.exception; // Pacote específico para exceções de domínio/regra de negócio

/**
 * Exceção genérica para representar erros de regra de negócio
 * (por exemplo: email já cadastrado, operação não permitida, etc.).
 *
 * Ela é uma RuntimeException, então não precisa ser declarada com throws.
 */
public class BusinessException extends RuntimeException {

    // Construtor que recebe apenas a mensagem de erro
    public BusinessException(String message) {
        super(message);
    }

    // Construtor que recebe mensagem e causa original (encapsula outra exception)
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
