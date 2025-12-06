package dominio.dto.auth; // Pacote dos DTOs de autenticação

import lombok.*; // Importa anotações do Lombok

// Gera automaticamente getters, setters, equals, hashCode e toString
@Data
// Construtor sem argumentos (necessário para desserialização JSON)
@NoArgsConstructor
// Construtor com todos os argumentos (apenas token)
@AllArgsConstructor
// Permite construir o objeto via padrão Builder
@Builder
public class RefreshTokenRequest {
    // Token JWT atual enviado pelo cliente para ser renovado
    private String token;
}
