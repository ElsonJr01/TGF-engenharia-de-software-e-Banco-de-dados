package lib; // Pacote genérico (menos específico que dominio.model)

import dominio.entidades.Usuario;
import dominio.enums.TipoUsuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

/**
 * Implementação de UserDetails que adapta a entidade Usuario
 * para o modelo usado pelo Spring Security.
 *
 * Parece ser um duplicado do CustomUserDetails em dominio.model.
 */
public class CustomUserDetails implements UserDetails {

    // Usuário de domínio que está autenticado
    private final Usuario usuario;

    // Construtor recebe a entidade Usuario já buscada do banco
    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Retorna as authorities (perfis) do usuário.
     * Formato: ROLE_ADMIN, ROLE_EDITOR, ROLE_REDATOR, ROLE_LEITOR.
     * É isso que o Spring usa em hasRole("ADMIN") etc.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + usuario.getTipo().name())
        );
    }

    /**
     * Senha do usuário (normalmente já com hash).
     */
    @Override
    public String getPassword() {
        return usuario.getSenha();
    }

    /**
     * Username usado para login. Aqui, é o e-mail.
     */
    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    /**
     * Indica se a conta NÃO está expirada.
     * Você não controla expiração de conta, então retorna sempre true.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica se a conta NÃO está bloqueada.
     * Usa o campo ativo da entidade:
     *  - ativo = true  -> não bloqueada
     *  - ativo = false -> bloqueada
     */
    @Override
    public boolean isAccountNonLocked() {
        return usuario.getAtivo();
    }

    /**
     * Indica se as credenciais NÃO estão expiradas.
     * Você não expira senha, então retorna sempre true.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica se o usuário está habilitado no sistema.
     * Também baseado no campo ativo.
     */
    @Override
    public boolean isEnabled() {
        return usuario.getAtivo();
    }

    // Métodos auxiliares para acessar dados do usuário

    public Long getId() {
        return usuario.getId();
    }

    public TipoUsuario getTipo() {
        return usuario.getTipo();
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
