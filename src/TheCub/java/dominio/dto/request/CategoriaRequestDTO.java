package dominio.dto.request; // Pacote de DTOs de entrada (requests)

import jakarta.validation.constraints.*;
import lombok.*;

// Gera getters, setters, equals, hashCode e toString
@Data
// Construtor sem argumentos (usado pelo framework na desserialização JSON)
@NoArgsConstructor
// Construtor com todos os campos
@AllArgsConstructor
// Permite criar objetos usando o padrão Builder
@Builder
public class CategoriaRequestDTO {

    // Nome obrigatório, com tamanho mínimo e máximo
    @NotBlank(message = "Nome da categoria é obrigatório")
    @Size(min = 3, max = 50, message = "Nome deve ter entre 3 e 50 caracteres")
    private String nome;

    // Descrição opcional, mas limitada em tamanho
    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    private String descricao;

    // Aceita apenas cores no formato hexadecimal #RRGGBB
    @Pattern(
            regexp = "^#([A-Fa-f0-9]{6})$",
            message = "Cor deve estar no formato hexadecimal (#RRGGBB)"
    )
    private String cor;

    // Código do ícone usado no frontend (ex.: fa-newspaper, fa-calendar)
    private String icone;

    // Indica se a categoria está ativa ou não
    private Boolean ativa;
}
