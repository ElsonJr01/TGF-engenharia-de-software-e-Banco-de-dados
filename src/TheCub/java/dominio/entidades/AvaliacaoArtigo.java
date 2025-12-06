package dominio.entidades; // Pacote das entidades JPA

import dominio.entidades.AvaliacaoArtigo;
import java.util.List;
import dominio.enums.TipoAvaliacao;
import jakarta.persistence.*;

/**
 * Entidade que representa a avaliação de um artigo feita por um usuário.
 * Cada usuário só pode ter UMA avaliação por artigo (unique constraint).
 */
@Entity
@Table(
        name = "avaliacoes_artigos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "artigo_id"})
)
public class AvaliacaoArtigo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID auto-increment
    private Long id;

    // Usuário que avaliou o artigo
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Artigo que foi avaliado
    @ManyToOne(optional = false)
    @JoinColumn(name = "artigo_id")
    private Artigo artigo;

    // Tipo de avaliação (GOSTEI, NEUTRO, NAO_GOSTEI, por exemplo)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAvaliacao avaliacao;

    // Construtor padrão exigido pelo JPA
    public AvaliacaoArtigo() {}

    // Construtor de conveniência para criar uma avaliação
    public AvaliacaoArtigo(Usuario usuario, Artigo artigo, TipoAvaliacao avaliacao) {
        this.usuario = usuario;
        this.artigo = artigo;
        this.avaliacao = avaliacao;
    }

    // Getters e setters básicos
    public Long getId() { return id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Artigo getArtigo() { return artigo; }
    public void setArtigo(Artigo artigo) { this.artigo = artigo; }

    public TipoAvaliacao getAvaliacao() { return avaliacao; }
    public void setAvaliacao(TipoAvaliacao avaliacao) { this.avaliacao = avaliacao; }
}
