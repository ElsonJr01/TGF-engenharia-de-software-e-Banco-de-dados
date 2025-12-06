package dominio.dto.auth; // Pacote dos DTOs relacionados à autenticação

import lombok.*; // Importa anotações do Lombok (Data, Builder, etc.)

// Gera automaticamente getters, setters, equals, hashCode e toString
@Data
// Construtor sem argumentos (necessário para desserialização JSON)
@NoArgsConstructor
// Construtor com todos os argumentos (email, senha)
@AllArgsConstructor
// Gera um builder para construir o objeto de forma fluente
@Builder
public class AuthRequest {
    // E-mail usado como credencial de login
    private String email;
    // Senha em texto puro que virá do frontend (antes de ser validada/hash)
    private String senha;
}
