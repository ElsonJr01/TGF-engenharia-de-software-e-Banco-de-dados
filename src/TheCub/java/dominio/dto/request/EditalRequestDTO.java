package dominio.dto.request; // Pacote dos DTOs de entrada (requests)

import lombok.Data;
import java.time.LocalDateTime;

// Gera automaticamente getters, setters, equals, hashCode e toString
@Data
public class EditalRequestDTO {

    // Título do edital (ex.: "Edital de Monitoria 2025.1")
    private String titulo;

    // Descrição/resumo do edital
    private String descricao;

    // URL onde o arquivo do edital está armazenado (PDF, por exemplo)
    private String arquivoUrl;

    // Nome do arquivo original (para exibir no frontend)
    private String arquivoNome;

    // Data de validade do edital (até quando ele é considerado ativo)
    private LocalDateTime dataValidade;
}
