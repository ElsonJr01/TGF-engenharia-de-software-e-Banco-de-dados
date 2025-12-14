package dominio.dto.request; // Pacote dos DTOs de entrada (requests)

import dominio.enums.TipoUsuario;
import jakarta.validation.constraints.*;
import lombok.*;

// Gera getters, setters, equals, hashCode e toString
@Data
// Construtor sem argumentos (necessário para desserialização JSON)
@NoArgsConstructor
// Construtor com todos os argumentos
@AllArgsConstructor
// Permite criação via padrão Builder
@Builder
public class UsuarioRequestDTO {

    // Nome obrigatório, com limite mínimo e máximo
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    // Email obrigatório e deve ter formato válido
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    // Senha obrigatória, com tamanho mínimo
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;

    // Tipo de usuário (ADMIN, EDITOR, REDATOR, LEITOR) é obrigatório
    @NotNull(message = "Tipo de usuário é obrigatório")
    private TipoUsuario tipo;

    // Campos opcionais para perfil
    private String foto; // URL/identificador da foto do usuário
    private String bio;  // Pequena biografia/apresentação
}
