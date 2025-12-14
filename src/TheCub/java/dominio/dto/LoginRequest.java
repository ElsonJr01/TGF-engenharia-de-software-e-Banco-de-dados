package dominio.dto; // Pacote dos DTOs de autenticação usados em outra parte do sistema

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Gera getters, setters, equals, hashCode, toString
@Data
// Construtor vazio (necessário para desserializar JSON)
@NoArgsConstructor
// Construtor com todos os campos
@AllArgsConstructor
public class LoginRequest {

    // Campo de e-mail obrigatório e com formato válido
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    // Campo de senha obrigatório
    @NotBlank(message = "Senha é obrigatória")
    private String senha;
}
