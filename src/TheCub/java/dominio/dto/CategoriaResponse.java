package dominio.dto; // Pacote de DTOs de saída (responses) gerais

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Gera getters, setters, equals, hashCode e toString automaticamente
@Data
// Permite criar o objeto usando o padrão Builder
@Builder
// Construtor sem argumentos (necessário para algumas libs)
@NoArgsConstructor
// Construtor com todos os campos
@AllArgsConstructor
public class CategoriaResponse {

    // ID único da categoria
    private Long id;

    // Nome exibido no frontend (ex.: "Notícias", "Eventos", "Pesquisa")
    private String nome;

    // Descrição curta da categoria
    private String descricao;

    // Cor associada à categoria (ex.: "#FF0000")
    private String cor;

    // Ícone usado no frontend (ex.: fa-newspaper, fa-calendar)
    private String icone;

    // Indica se a categoria está ativa (true) ou desativada (false)
    private Boolean ativa;
}
