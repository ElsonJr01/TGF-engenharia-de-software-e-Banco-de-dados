package dominio.dto; // Pacote dos DTOs de saída (responses)

import dominio.enums.StatusNoticia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Gera getters, setters, equals, hashCode e toString automaticamente
@Data
// Permite construção via padrão Builder
@Builder
// Construtor sem argumentos
@NoArgsConstructor
// Construtor com todos os argumentos
@AllArgsConstructor
public class NoticiaResponse {

    // Identificador único do artigo
    private Long id;

    // Título exibido no frontend
    private String titulo;

    // Resumo/linha fina do artigo
    private String resumo;

    // Conteúdo completo
    private String conteudo;

    // Status atual (RASCUNHO, PUBLICADO, ARQUIVADO, etc.)
    private StatusNoticia status;

    // URL/caminho da imagem de capa
    private String imagemCapa;

    // Número total de visualizações
    private Integer visualizacoes;

    // Contagem de reações "gostei"
    private Integer gostei;

    // Contagem de reações "neutro"
    private Integer neutro;

    // Contagem de reações "não gostei"
    private Integer naoGostei;

    // Indica se o artigo é destaque na home/áreas especiais
    private Boolean destaque;

    // Data em que o artigo foi publicado
    private LocalDateTime dataPublicacao;

    // Data de criação do artigo
    private LocalDateTime dataCriacao;

    // Data da última atualização
    private LocalDateTime dataAtualizacao;

    // Nome do autor (para exibição direta no frontend)
    private String autorNome;

    // ID do autor (para vínculos/links)
    private Long autorId;

    // Nome da categoria do artigo
    private String categoriaNome;

    // ID da categoria
    private Long categoriaId;
}
