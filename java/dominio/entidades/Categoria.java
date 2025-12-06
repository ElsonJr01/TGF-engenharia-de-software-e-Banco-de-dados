package dominio.entidades; // Pacote das entidades JPA

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa uma categoria de artigos (ex.: Notícias, Eventos, Pesquisa).
 */
@Entity
@Table(name = "categorias")
@Getter // Lombok: gera getters
@Setter // Lombok: gera setters
@NoArgsConstructor // Construtor sem argumentos
@AllArgsConstructor // Construtor com todos os argumentos
@Builder // Permite construção via padrão Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID auto-increment
    private Long id;

    // Nome da categoria, obrigatório e único
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    // Descrição opcional (usando TEXT no banco)
    @Size(max = 255)
    @Column(columnDefinition = "TEXT")
    private String descricao;

    // Cor em formato hexadecimal, com valor padrão
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$")
    @Builder.Default
    private String cor = "#007bff";

    // Código/nome do ícone usado no frontend
    @Column(length = 100)
    private String icone;

    // Flag indicando se a categoria está ativa
    @Builder.Default
    private Boolean ativa = true;

    // Relação 1:N – uma categoria possui vários artigos
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Artigo> artigos = new ArrayList<>();

    /**
     * Conta quantos artigos desta categoria estão com status PUBLICADO.
     */
    public long contarArtigosPublicados() {
        return artigos.stream()
                .filter(a -> a.getStatus() != null
                        && a.getStatus().name().equalsIgnoreCase("PUBLICADO"))
                .count();
    }

    @Override
    public String toString() {
        return nome;
    }
}
