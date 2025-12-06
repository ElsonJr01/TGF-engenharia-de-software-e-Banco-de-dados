package dominio.dto; // Pacote de DTOs de saída (responses) gerais

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Gera automaticamente getters, setters, equals, hashCode e toString
@Data
// Permite construção via padrão Builder
@Builder
// Construtor sem argumentos (usado por frameworks)
@NoArgsConstructor
// Construtor com todos os campos
@AllArgsConstructor
public class UsuarioResponse {

    // ID único do usuário
    private Long id;

    // Nome completo que será exibido no frontend
    private String nome;

    // E-mail do usuário
    private String email;

    // Tipo/papel do usuário (ex.: ADMIN, EDITOR, REDATOR, LEITOR) em formato String
    private String tipo;

    // Indica se o usuário está ativo (true) ou desativado/bloqueado (false)
    private Boolean ativo;

    // URL/caminho da foto de perfil (opcional)
    private String foto;

    // Biografia ou descrição breve do usuário
    private String bio;
}
