package dominio.entidades; // Pacote das entidades JPA

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa um edital (ex.: seleção, monitoria, bolsas).
 */
@Entity
@Table(name = "editais")
@Data // Gera getters, setters, equals, hashCode, toString
@Builder // Permite criação via padrão Builder
@NoArgsConstructor // Construtor sem argumentos
@AllArgsConstructor // Construtor com todos os campos
public class Edital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID auto-increment
    private Long id;

    // Título do edital
    @Column(nullable = false, length = 200)
    private String titulo;

    // Descrição/resumo (campo TEXT)
    @Column(columnDefinition = "TEXT")
    private String descricao;

    // URL do arquivo do edital (PDF etc.)
    @Column(name = "arquivo_url", nullable = false, length = 500)
    private String arquivoUrl;

    // Nome do arquivo (para exibição)
    @Column(name = "arquivo_nome", nullable = false, length = 255)
    private String arquivoNome;

    // Data em que o edital foi publicado
    @Column(name = "data_publicacao", nullable = false)
    private LocalDateTime dataPublicacao;

    // Data limite de validade do edital (pode ser nula)
    @Column(name = "data_validade")
    private LocalDateTime dataValidade;

    // Flag indicando se o edital está ativo/visível
    @Column(nullable = false)
    private Boolean ativo = true;

    // Número de visualizações do edital
    @Column(nullable = false)
    private Integer visualizacoes = 0;

    // Usuário autor/responsável pelo cadastro do edital
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    // Datas de auditoria
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Callback executado antes de inserir (persist) o registro
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (dataPublicacao == null) {
            dataPublicacao = LocalDateTime.now();
        }
    }

    // Callback executado antes de atualizar o registro
    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    // Incrementa contador de visualizações
    public void incrementarVisualizacoes() {
        this.visualizacoes++;
    }

    // Verifica se o edital ainda é válido (ativo e dentro da data de validade)
    public boolean isValido() {
        return ativo && (dataValidade == null || dataValidade.isAfter(LocalDateTime.now()));
    }
}
