package dominio.entidades; // Pacote das entidades JPA (mapeadas para o banco)

import dominio.enums.StatusNoticia;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity // Indica que esta classe é uma entidade JPA
@Table(
        name = "artigos",
        indexes = {
                // Índice para buscas por status
                @Index(name = "idx_status", columnList = "status"),
                // Índice para ordenação/busca por data de publicação
                @Index(name = "idx_data_publicacao", columnList = "data_publicacao")
        }
)
@Getter // Lombok: gera getters
@Setter // Lombok: gera setters
@NoArgsConstructor // Construtor sem argumentos
@AllArgsConstructor // Construtor com todos os argumentos
@Builder // Padrão Builder para construir instâncias
public class Noticia {

    @Id // Chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment no banco
    private Long id;

    @NotBlank
    @Size(min = 10, max = 200)
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank
    @Size(min = 20, max = 500)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String resumo;

    @NotBlank
    @Size(min = 100)
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String conteudo;

    @Enumerated(EnumType.STRING) // Salva o nome da constante enum (RASCUNHO, PUBLICADO...)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusNoticia status = StatusNoticia.RASCUNHO;

    @Column(name = "imagem_capa", length = 255)
    private String imagemCapa;

    @Builder.Default
    private Integer visualizacoes = 0;

    // NOVOS CAMPOS DE AVALIAÇÃO
    @Builder.Default
    private Integer gostei = 0;

    @Builder.Default
    private Integer neutro = 0;

    @Builder.Default
    private Integer naoGostei = 0;

    @Builder.Default
    private Boolean destaque = false;

    @Column(name = "data_publicacao")
    private LocalDateTime dataPublicacao;

    @CreationTimestamp // Preenchido automaticamente na inserção
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp // Atualizado automaticamente a cada update
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Muitos artigos para um usuário (autor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    @NotNull
    private Usuario autor;

    // Muitos artigos para uma categoria
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    @NotNull
    private Categoria categoria;

    // Um artigo tem vários comentários
    @OneToMany(mappedBy = "artigo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comentario> comentarios = new ArrayList<>();

    // ====== MÉTODOS ======

    // Incrementa contador de visualizações
    public void incrementarVisualizacoes() {
        this.visualizacoes++;
    }

    // Métodos de avaliação (reação do usuário)
    public void incrementarGostei() {
        this.gostei++;
    }

    public void incrementarNeutro() {
        this.neutro++;
    }

    public void incrementarNaoGostei() {
        this.naoGostei++;
    }

    // Publica o artigo, ajustando status e data de publicação se necessário
    public void publicar() {
        this.status = StatusNoticia.PUBLICADO;
        if (this.dataPublicacao == null) {
            this.dataPublicacao = LocalDateTime.now();
        }
    }

    // Verifica se o artigo está em status PUBLICADO
    public boolean isPublicado() {
        return this.status == StatusNoticia.PUBLICADO;
    }

    // Conta apenas comentários aprovados vinculados ao artigo
    public long contarComentariosAprovados() {
        return comentarios.stream()
                .filter(Comentario::isAprovado)
                .count();
    }

    @Override
    public String toString() {
        return titulo + " (" + status + ")";
    }
}
