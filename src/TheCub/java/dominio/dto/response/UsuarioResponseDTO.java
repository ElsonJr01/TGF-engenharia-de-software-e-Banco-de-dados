package dominio.dto.response; // Pacote de DTOs de saída (lado admin/painéis)

import dominio.enums.TipoUsuario;
import lombok.*;
import java.time.LocalDateTime;

// Gera getters, setters, equals, hashCode e toString
@Data
// Construtor sem argumentos
@NoArgsConstructor
// Construtor com todos os campos
@AllArgsConstructor
// Permite construção via padrão Builder
@Builder
public class UsuarioResponseDTO {

    // Dados básicos do usuário
    private Long id;
    private String nome;
    private String email;
    private TipoUsuario tipo;     // ADMIN, EDITOR, REDATOR, LEITOR
    private Boolean ativo;
    private String foto;
    private String bio;
    private LocalDateTime dataCriacao;

    // Métricas relacionadas ao usuário
    private Long totalArtigos;     // Quantidade de artigos produzidos
    private Long totalComentarios; // Quantidade de comentários feitos
}
