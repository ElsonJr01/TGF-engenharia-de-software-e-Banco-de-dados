package dominio.dto.request; // Pacote dos DTOs de entrada (requests)

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

// Gera getters, setters, equals, hashCode e toString automaticamente
@Data
// Construtor padrão sem argumentos (necessário para desserialização JSON)
@NoArgsConstructor
// Construtor com todos os argumentos
@AllArgsConstructor
// Permite construir objetos usando o padrão Builder
@Builder
public class CulturaRequestDTO {

    // Título obrigatório, com tamanho mínimo e máximo
    @NotBlank(message = "Título do evento é obrigatório")
    @Size(min = 5, max = 150, message = "Título deve ter entre 5 e 150 caracteres")
    private String titulo;

    // Descrição obrigatória, com limites de tamanho (mais longa, por ser texto de evento)
    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 20, max = 2000, message = "Descrição deve ter entre 20 e 2000 caracteres")
    private String descricao;

    // Data do evento obrigatória e precisa ser futura
    @NotNull(message = "Data do evento é obrigatória")
    @Future(message = "A data do evento deve ser futura")
    private LocalDateTime dataEvento;

    // Local do evento obrigatório, com limite máximo de caracteres
    @NotBlank(message = "Local do evento é obrigatório")
    @Size(max = 200, message = "Local deve ter no máximo 200 caracteres")
    private String localEvento;

    // ID do organizador (relacionamento com entidade Usuario/Organizador)
    @NotNull(message = "Organizador é obrigatório")
    private Long organizadorId;

    // Campos opcionais

    // URL/caminho da imagem do evento (banner, capa etc.)
    private String imagem;

    // Link para inscrição (Google Forms, SIGAA, site, etc.)
    private String linkInscricao;
}
