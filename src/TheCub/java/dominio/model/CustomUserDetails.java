package dominio.model; // Pacote de modelos de segurança / integração com Spring Security

import dominio.entidades.Usuario;
import dominio.enums.TipoUsuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementação de UserDetails que adapta a entidade Usuario
 * para o modelo esperado pelo Spring Security.
 *
 * Essa classe é usada pelo Spring Security na autenticação/autorização
 * para saber:
 *  - login (username)
 *  - senha
 *  - roles/perfis
 *  - se a conta está ativa/bloqueada
 */
public class CustomUserDetails implements UserDetails {

    // Referência para a entidade de domínio Usuario
    private final Usuario usuario;

    // Construtor recebe a entidade já carregada do banco
    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Retorna as autoridades (roles/perfis) do usuário.
     *
     * Aqui, cada usuário tem uma única role no formato:
     *  "ROLE_ADMIN", "ROLE_EDITOR", "ROLE_REDATOR", "ROLE_LEITOR"
     *
     * Isso é o que o Spring Security usa para avaliar @PreAuthorize,
     * hasRole, hasAnyRole etc.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + usuario.getTipo().name())
        );
    }

    /**
     * Retorna a senha que o Spring Security vai usar para conferir o login.
     * Normalmente já vem em formato de hash (BCrypt, por exemplo).
     */
    @Override
    public String getPassword() {
        return usuario.getSenha();
    }

    /**
     * Retorna o "username" usado para login.
     * No seu caso, o e-mail é o identificador de login.
     */
    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    /**
     * Indica se a conta está expirada ou não.
     * Aqui você sempre considera que NUNCA expira (true fixo).
     * Poderia ser estendido no futuro com um campo dataExpiracao na entidade.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica se a conta está bloqueada.
     * Você usa o campo ativo do Usuario para isso:
     *  - ativo = true  -> conta não bloqueada
     *  - ativo = false -> conta bloqueada
     */
    @Override
    public boolean isAccountNonLocked() {
        return usuario.getAtivo();
    }

    /**
     * Indica se as credenciais (senha) estão expiradas.
     * Aqui sempre retorna true (não expira).
     * Poderia evoluir para forçar troca de senha periódica.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica se o usuário está habilitado no sistema.
     * Você também usa o campo ativo aqui:
     *  - ativo = true  -> enabled
     *  - ativo = false -> disabled
     */
    @Override
    public boolean isEnabled() {
        return usuario.getAtivo();
    }

    // Métodos auxiliares extras para facilitar acesso à entidade original

    // Retorna o ID do usuário, útil em contextos onde você só tem o principal
    public Long getId() {
        return usuario.getId();
    }

    // Retorna o tipo (enum) do usuário (ADMIN, EDITOR, etc.)
    public TipoUsuario getTipo() {
        return usuario.getTipo();
    }

    // Expõe a entidade completa, se você precisar de mais detalhes
    public Usuario getUsuario() {
        return usuario;
    }
}
