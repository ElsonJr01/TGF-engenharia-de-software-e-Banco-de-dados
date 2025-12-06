package dominio.dto.response; // Pacote de DTOs de saída (responses) específicos

import dominio.enums.StatusArtigo;
import lombok.*;
import java.time.LocalDateTime;

// Gera getters, setters, equals, hashCode e toString
@Data
// Construtor sem argumentos
@NoArgsConstructor
// Construtor com todos os campos
@AllArgsConstructor
// Permite criação via padrão Builder
@Builder
public class ArtigoResponseDTO {

    // Dados principais do artigo
    private Long id;
    private String titulo;
    private String resumo;
    private String conteudo;
    private StatusArtigo status;
    private String imagemCapa;
    private Integer visualizacoes;
    private Boolean destaque;
    private LocalDateTime dataPublicacao;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    // Dados relacionados (autor, categoria, métricas)
    private String autorNome;
    private Long autorId;
    private String categoriaNome;
    private Long categoriaId;
    private String categoriaCor;
    private Long totalComentarios;
}
