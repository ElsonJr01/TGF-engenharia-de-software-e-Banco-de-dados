package dominio.entidades; // Pacote das entidades JPA

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Entidade que representa um evento cultural/universitário.
 */
@Entity
@Table(name = "eventos")
@Getter // Gera getters
@Setter // Gera setters
@NoArgsConstructor // Construtor vazio
@AllArgsConstructor // Construtor com todos os campos
@Builder // Permite padrão Builder
public class Cultura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID auto-increment
    private Long id;

    // Título do evento (obrigatório, com limites de tamanho)
    @NotBlank
    @Size(min = 5, max = 150)
    @Column(nullable = false)
    private String titulo;

    // Descrição detalhada do evento
    @NotBlank
    @Size(min = 20, max = 2000)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    // Data/hora do evento: obrigatória e futura
    @NotNull
    @Future
    @Column(name = "data_evento")
    private LocalDateTime dataEvento;

    // Local onde o evento ocorrerá
    @NotBlank
    @Column(name = "local_evento", length = 200)
    private String localEvento;

    // URL/caminho da imagem do evento
    private String imagem;

    // Link para formulário/inscrição
    private String linkInscricao;

    // Flag indicando se o evento está ativo
    @Builder.Default
    private Boolean ativo = true;

    // Data de criação do registro, preenchida automaticamente
    @CreationTimestamp
    private LocalDateTime dataCriacao;

    // Organizador (usuário responsável)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id", nullable = false)
    private Usuario organizador;

    // Indica se o evento já ocorreu
    public boolean jaAconteceu() {
        return dataEvento.isBefore(LocalDateTime.now());
    }

    // Indica se o evento está dentro da janela dos próximos 7 dias
    public boolean isProximo() {
        LocalDateTime agora = LocalDateTime.now();
        return dataEvento.isAfter(agora) && dataEvento.isBefore(agora.plusDays(7));
    }

    @Override
    public String toString() {
        return titulo + " em " + localEvento;
    }
}
