package dominio.dto.response; // Pacote de DTOs de resposta (saída da API)

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

// Gera automaticamente getters, setters, equals, hashCode e toString
@Data
// Permite criar instâncias usando o padrão Builder
@Builder
public class EditalResponseDTO {

    // Identificador único do edital
    private Long id;

    // Título do edital
    private String titulo;

    // Descrição ou resumo do edital
    private String descricao;

    // URL onde o arquivo (PDF, DOC etc.) está disponível
    private String arquivoUrl;

    // Nome do arquivo (para exibição no frontend)
    private String arquivoNome;

    // Data em que o edital foi publicado
    private LocalDateTime dataPublicacao;

    // Data até a qual o edital é válido
    private LocalDateTime dataValidade;

    // Indica se o edital está ativo (visível) ou não
    private Boolean ativo;

    // Número de visualizações do edital
    private Integer visualizacoes;

    // Nome do autor/responsável pelo cadastro do edital
    private String autorNome;

    // Data de criação do registro no sistema
    private LocalDateTime dataCriacao;
}
