package dominio.dto.request; // Pacote dos DTOs de entrada (requests)

import dominio.enums.StatusNoticia;
import jakarta.validation.constraints.*;
import lombok.*;

// Gera getters, setters, equals, hashCode e toString
@Data
// Construtor sem argumentos (usado pela desserialização JSON)
@NoArgsConstructor
// Construtor com todos os campos
@AllArgsConstructor
// Habilita construção via padrão Builder
@Builder
public class NoticiaRequestDTO {

    // Título obrigatório, com tamanho mínimo e máximo
    @NotBlank(message = "Título é obrigatório")
    @Size(min = 10, max = 200, message = "Título deve ter entre 10 e 200 caracteres")
    private String titulo;

    // Resumo obrigatório, com limites de tamanho
    @NotBlank(message = "Resumo é obrigatório")
    @Size(min = 20, max = 500, message = "Resumo deve ter entre 20 e 500 caracteres")
    private String resumo;

    // Conteúdo completo obrigatório, com tamanho mínimo razoável
    @NotBlank(message = "Conteúdo é obrigatório")
    @Size(min = 100, message = "Conteúdo deve ter no mínimo 100 caracteres")
    private String conteudo;

    // ID da categoria é obrigatório (relacionamento com Categoria)
    @NotNull(message = "Categoria é obrigatória")
    private Long categoriaId;

    // ID do autor é obrigatório (relacionamento com Usuario)
    @NotNull(message = "Autor é obrigatório")
    private Long autorId;

    // Status opcional ao criar/atualizar (RASCUNHO, PUBLICADO, etc.)
    private StatusNoticia status;

    // URL ou caminho da imagem de capa (opcional)
    private String imagemCapa;

    // Indica se o artigo é destaque na home ou seções específicas
    private Boolean destaque;
}
