package dominio.dto.auth; // Pacote dos DTOs relacionados à autenticação

import lombok.*; // Importa anotações do Lombok

// Gera getters, setters, equals, hashCode e toString automaticamente
@Data
// Construtor sem argumentos (útil para frameworks)
@NoArgsConstructor
// Construtor com todos os campos
@AllArgsConstructor
// Habilita o padrão de construção via builder
@Builder
public class AuthResponse {
    // Token JWT que o cliente usará nas próximas requisições
    private String token;
    // Tipo de usuário (papel): ADMIN, EDITOR, REDATOR, LEITOR
    private String tipo;
    // Nome completo do usuário autenticado
    private String nome;
    // E-mail do usuário autenticado
    private String email;
    // ID do usuário no banco de dados
    private Long id;
}
