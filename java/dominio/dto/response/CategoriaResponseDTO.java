package dominio.dto.response; // Pacote de DTOs de resposta específicos (lado admin/público)

import lombok.*;

// Gera getters, setters, equals, hashCode e toString
@Data
// Construtor padrão (sem argumentos)
@NoArgsConstructor
// Construtor com todos os campos
@AllArgsConstructor
// Permite construção via padrão Builder
@Builder
public class CategoriaResponseDTO {

    // ID único da categoria
    private Long id;

    // Nome da categoria (ex.: "Notícias", "Eventos", "Esportes")
    private String nome;

    // Descrição opcional da categoria
    private String descricao;

    // Cor associada para estilizar no frontend (ex.: "#FF0000")
    private String cor;

    // Ícone usado no frontend (ex.: "fa-newspaper")
    private String icone;

    // Indica se a categoria está ativa (visível para uso)
    private Boolean ativa;

    // Quantidade de artigos vinculados a essa categoria
    private Long totalArtigos;
}
