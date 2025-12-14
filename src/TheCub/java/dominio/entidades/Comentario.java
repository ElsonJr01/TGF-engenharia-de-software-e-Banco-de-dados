package dominio.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios", indexes = {
        @Index(name = "idx_artigo", columnList = "artigo_id"),
        @Index(name = "idx_aprovado", columnList = "aprovado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 1000)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comentario;

    @Builder.Default
    @Column(nullable = false)
    private Boolean aprovado = false;

    @CreationTimestamp
    @Column(name = "data_comentario", nullable = false, updatable = false)
    private LocalDateTime dataComentario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artigo_id", nullable = false)
    private Noticia artigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // ====== MÉTODOS AUXILIARES ======
    public void aprovar() {
        this.aprovado = true;
    }

    public void reprovar() {
        this.aprovado = false;
    }

    public boolean isAprovado() {
        return Boolean.TRUE.equals(this.aprovado);
    }

    @Override
    public String toString() {
        return "Comentário de " + (usuario != null ? usuario.getNome() : "Usuário") +
                (Boolean.TRUE.equals(aprovado) ? " (Aprovado)" : " (Pendente)");
    }
}
