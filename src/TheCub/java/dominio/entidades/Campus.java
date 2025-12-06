package dominio.entidades; // Pacote das entidades JPA (mapeadas para o banco)

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Entidade que representa um campus universitário no sistema.
 */
@Entity
@Table(name = "campus")
@Getter // Lombok: gera getters
@Setter // Lombok: gera setters
@NoArgsConstructor // Construtor sem argumentos
@AllArgsConstructor // Construtor com todos os argumentos
@Builder // Permite construção via padrão Builder
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID auto-increment no banco
    private Long id;

    // Nome do campus, obrigatório e único
    @NotBlank(message = "Nome do campus é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    // Descrição de localização (endereço, cidade, etc.), obrigatória
    @NotBlank(message = "Localização é obrigatória")
    @Size(max = 255, message = "Localização deve ter no máximo 255 caracteres")
    @Column(nullable = false, length = 255)
    private String localizacao;

    // Telefone opcional, mas se informado deve seguir o padrão (XX) XXXXX-XXXX
    @Pattern(
            regexp = "^\\([0-9]{2}\\) [0-9]{4,5}-[0-9]{4}$",
            message = "Telefone deve estar no formato (XX) XXXXX-XXXX"
    )
    @Column(length = 20)
    private String telefone;

    // Email institucional opcional, validado quanto ao formato
    @Email(message = "Email inválido")
    @Column(length = 100)
    private String email;

    // Indica se o campus está ativo no sistema
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Override
    public String toString() {
        return nome;
    }
}
