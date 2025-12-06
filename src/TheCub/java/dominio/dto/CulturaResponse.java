package dominio.dto; // Pacote de DTOs de saída (responses) gerais

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// Gera automaticamente getters, setters, equals, hashCode e toString
@Data
// Permite criar instâncias usando o padrão Builder
@Builder
// Construtor sem argumentos (útil para frameworks)
@NoArgsConstructor
// Construtor com todos os argumentos
@AllArgsConstructor
public class CulturaResponse {

    // ID único do evento
    private Long id;

    // Título do evento (ex.: "Semana de Integração", "Mostra Científica")
    private String titulo;

    // Descrição detalhada do evento
    private String descricao;

    // Data e hora em que o evento vai acontecer
    private LocalDateTime dataEvento;

    // Local físico ou virtual do evento
    private String localEvento;

    // URL/caminho da imagem do evento (banner, capa, etc.)
    private String imagem;

    // Link para inscrição (formulário, SIGAA, etc.)
    private String linkInscricao;

    // Indica se o evento está ativo/visível para o público
    private Boolean ativo;

    // Nome do organizador responsável (professor, coordenação, etc.)
    private String organizadorNome;
}
