package dominio.exception; // Pacote central para exceções de domínio da aplicação

/**
 * ResourceNotFoundException
 *
 * Exceção específica para representar a situação em que
 * um recurso de domínio (Usuário, Artigo, Categoria, etc.)
 * não é encontrado no sistema/banco de dados.
 *
 * Ela estende RuntimeException:
 * - não precisa ser declarada em "throws"
 * - pode ser lançada em qualquer camada (service, repository adaptado, etc.)
 * - é interceptada pelo GlobalExceptionHandler e convertida em HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Construtor simples que recebe apenas uma mensagem.
     *
     * Exemplo de uso:
     *  throw new ResourceNotFoundException("Artigo não encontrado");
     *
     * Útil quando você já montou a mensagem completa em outro lugar.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Construtor mais rico, que recebe:
     *  - resourceName: nome do recurso (ex.: "Usuário", "Artigo", "Categoria")
     *  - fieldName: campo usado na busca (ex.: "id", "email")
     *  - fieldValue: valor procurado (ex.: 10, "teste@teste.com")
     *
     * Ele monta automaticamente uma mensagem padrão e legível:
     *  "%s não encontrado(a) com %s: '%s'"
     *
     * Exemplo:
     *  new ResourceNotFoundException("Usuário", "id", 10)
     *  => "Usuário não encontrado(a) com id: '10'"
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s não encontrado(a) com %s: '%s'",
                resourceName, fieldName, fieldValue));
    }
}
