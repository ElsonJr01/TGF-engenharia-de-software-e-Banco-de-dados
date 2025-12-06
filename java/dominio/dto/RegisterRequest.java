package dominio.dto; // Pacote de DTOs gerais (neste caso, para registro público de usuário)

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;

// Gera automaticamente getters, setters, equals, hashCode e toString
@Data
public class RegisterRequest {

    // Nome obrigatório do usuário que está se cadastrando
    @NotBlank
    private String nome;

    // E-mail obrigatório e deve estar em formato válido
    @NotBlank
    @Email
    private String email;

    // Senha obrigatória (regra de tamanho mínimo normalmente tratada aqui ou no serviço)
    @NotBlank
    private String senha;
}
