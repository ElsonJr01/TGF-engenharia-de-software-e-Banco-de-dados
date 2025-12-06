package lib; // Pacote de classes de infraestrutura/segurança

import dominio.entidades.Usuario;
import dominio.model.CustomUserDetails;
import lib.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementação de UserDetailsService.
 *
 * O Spring Security chama esse serviço sempre que precisa
 * carregar um usuário para autenticação, usando o "username"
 * (no seu caso, o e-mail).
 */
@Service
@RequiredArgsConstructor // Gera construtor com o final usuarioRepository
public class CustomUserDetailsService implements UserDetailsService {

    // Repositório que acessa a tabela de usuários no banco
    private final UsuarioRepository usuarioRepository;

    /**
     * Carrega um usuário pelo "username" (aqui você usa e-mail).
     *
     * Esse método é chamado automaticamente pelo Spring Security
     * durante o processo de login (autenticação).
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca usuário ativo pelo e-mail
        Usuario usuario = usuarioRepository.findByEmailAndAtivoTrue(email)
                // Se não encontrar, lança exceção padrão do Spring Security
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado ou inativo: " + email)
                );

        // Adapta a entidade Usuario para o modelo UserDetails esperado pelo Spring Security
        return new CustomUserDetails(usuario);
    }
}
