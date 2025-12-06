package dominio.dto.response; // Pacote de DTOs de resposta específicos

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
public class CulturaResponseDTO {

    // Dados principais do evento
    private Long id;
    private String titulo;
    private String descricao;
    private LocalDateTime dataEvento;
    private String localEvento;
    private String imagem;
    private String linkInscricao;
    private Boolean ativo;
    private LocalDateTime dataCriacao;

    // Dados relacionados ao organizador
    private String organizadorNome;
    private Long organizadorId;
}
