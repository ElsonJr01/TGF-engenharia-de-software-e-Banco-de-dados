package dominio.entidades; // Pacote das entidades JPA

import dominio.enums.TipoUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um usuário do sistema (THE CLUB).
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID auto-incremento no banco
    private Long id;

    // Nome completo do usuário, obrigatório
    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;

    // E-mail único e obrigatório, usado para login
    @Email(message = "E-mail inválido")
    @NotBlank(message = "O e-mail é obrigatório")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Senha armazenada (normalmente já com hash)
    @NotBlank(message = "A senha é obrigatória")
    @Column(nullable = false)
    private String senha;

    // Tipo de usuário (ADMIN, EDITOR, REDATOR, LEITOR), com padrão LEITOR
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TipoUsuario tipo = TipoUsuario.LEITOR;

    // URL/caminho da foto de perfil
    @Column(length = 255)
    private String foto;

    // Biografia/apresentação do usuário
    @Column(length = 500)
    private String bio;

    // Flag indicando se o usuário está ativo
    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;

    // Data de criação do registro (setada automaticamente)
    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    // Data da última atualização do registro
    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Relação 1:N – um usuário pode ser autor de vários artigos
    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Artigo> artigos = new ArrayList<>();

    // Relação 1:N – um usuário pode ter vários comentários
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
     private List<Comentario> comentarios = new ArrayList<>();

    // ======= MÉTODOS DE PERMISSÃO =======

    // Verifica se o usuário é ADMIN
    public boolean isAdmin() {
        return this.tipo == TipoUsuario.ADMIN;
    }

    // Verifica se é EDITOR ou ADMIN
    public boolean isEditorOuAdmin() {
        return this.tipo == TipoUsuario.ADMIN || this.tipo == TipoUsuario.EDITOR;
    }

    // Verifica se tem permissão para publicar artigos (ADMIN, EDITOR ou REDATOR)
    public boolean podePublicar() {
        return this.tipo == TipoUsuario.ADMIN
                || this.tipo == TipoUsuario.EDITOR
                || this.tipo == TipoUsuario.REDATOR;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", tipo=" + tipo +
                ", ativo=" + ativo +
                '}';
    }
}
