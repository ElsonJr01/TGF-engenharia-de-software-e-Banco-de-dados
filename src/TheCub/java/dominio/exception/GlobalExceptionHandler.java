package dominio.exception; // Pacote onde ficam as exceções e handlers globais da aplicação

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler
 *
 * Classe responsável por capturar e tratar, de forma padronizada,
 * as exceções lançadas pelos controllers da aplicação.
 *
 * A anotação @RestControllerAdvice faz com que o Spring "escute"
 * todas as exceções disparadas em classes anotadas com @RestController
 * e redirecione para os métodos marcados com @ExceptionHandler abaixo.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata exceções do tipo ResourceNotFoundException.
     *
     * Essa exceção normalmente é lançada quando um recurso (usuário, artigo,
     * categoria, etc.) não é encontrado no banco a partir de um ID ou filtro.
     *
     * Exemplo de uso:
     *  - usuarioService.buscarPorId(999) e não existe esse usuário -> lança ResourceNotFoundException.
     *  - Aqui, ela é convertida em uma resposta HTTP 404 (NOT_FOUND) com um corpo padronizado.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        // Corpo da resposta que será enviado como JSON
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());              // Momento em que o erro ocorreu
        body.put("status", HttpStatus.NOT_FOUND.value());        // Código HTTP numérico (404)
        body.put("error", "Recurso não encontrado");             // Mensagem genérica de erro
        body.put("message", ex.getMessage());                    // Mensagem específica da exceção

        // Retorna HTTP 404 com o corpo definido acima
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Trata exceções de regra de negócio (BusinessException).
     *
     * Exemplos de BusinessException:
     *  - Tentar cadastrar e-mail já existente.
     *  - Tentar publicar artigo sem permissão.
     *
     * Aqui, qualquer BusinessException é devolvida como HTTP 400 (BAD_REQUEST),
     * indicando que a requisição é inválida dentro das regras do domínio.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());               // Momento do erro
        body.put("status", HttpStatus.BAD_REQUEST.value());       // 400
        body.put("error", "Erro de negócio");                     // Tipo genérico de erro
        body.put("message", ex.getMessage());                     // Detalhe vindo da exceção

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Trata erros de validação de argumentos anotados com @Valid/@Validated.
     *
     * O Spring lança MethodArgumentNotValidException quando:
     *  - Um DTO de entrada possui anotações de validação (ex.: @NotBlank, @Email)
     *  - E os dados recebidos na requisição não passam nessas regras.
     *
     * Exemplo:
     *  - Enviar um JSON de cadastro de usuário sem "email" -> cai aqui.
     *
     * A ideia é montar um mapa campo -> mensagem de erro, para o frontend
     * conseguir exibir erros específicos em cada input.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        // Mapa com erros específicos por campo: "campo" -> "mensagem de validação"
        Map<String, String> errors = new HashMap<>();

        // Percorre todos os erros de validação encontrados no bindingResult
        ex.getBindingResult().getAllErrors().forEach(error -> {
            // Cast para FieldError para conseguir o nome do campo
            String fieldName = ((FieldError) error).getField();     // Ex.: "email", "senha"
            String errorMessage = error.getDefaultMessage();        // Mensagem definida na anotação
            errors.put(fieldName, errorMessage);
        });

        // Corpo de resposta com metadados + mapa de erros
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());                 // Momento do erro
        body.put("status", HttpStatus.BAD_REQUEST.value());         // 400
        body.put("error", "Erro de validação");                     // Tipo genérico
        body.put("errors", errors);                                 // Detalhes por campo

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Tratador genérico para qualquer exceção não mapeada especificamente acima.
     *
     * Serve como "fallback" para erros inesperados:
     *  - NullPointerException
     *  - IllegalArgumentException
     *  - Erros de infra, etc.
     *
     * Retorna HTTP 500 (INTERNAL_SERVER_ERROR), indicando falha no servidor.
     * Em produção, normalmente se evita devolver detalhes sensíveis da exceção.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());                        // Momento do erro
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());      // 500
        body.put("error", "Erro interno do servidor");                     // Mensagem genérica
        body.put("message", ex.getMessage());                              // Mensagem bruta da exception

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
