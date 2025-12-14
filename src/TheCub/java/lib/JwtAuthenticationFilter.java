package lib; // Pacote de segurança/infra da aplicação

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Filtro responsável por:
 * - Ler o cabeçalho Authorization da requisição
 * - Extrair e validar o token JWT
 * - Carregar o usuário correspondente
 * - Registrar a autenticação no SecurityContext
 *
 * Extende OncePerRequestFilter: garante que o filtro roda uma vez por request.
 */
@Component // Registrado como bean e incluído na cadeia de filtros do Spring Security
@RequiredArgsConstructor // Gera construtor com os campos finais (injeção via construtor)
@Slf4j // Habilita logging com 'log.info', 'log.warn', etc.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Serviço responsável por operações com JWT (gerar, validar, extrair username)
    private final JwtService jwtService;

    // Serviço que carrega detalhes do usuário a partir do e-mail (username)
    private final CustomUserDetailsService userDetailsService;

    /**
     * Método principal do filtro. É executado em TODA requisição HTTP.
     *
     * Se o token for válido:
     *  - recupera o usuário
     *  - cria uma Authentication
     *  - coloca no SecurityContext
     *
     * Depois disso, a requisição segue normalmente para o próximo filtro/controller.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Lê o cabeçalho Authorization (onde deve vir: "Bearer <token>")
        final String authHeader = request.getHeader("Authorization");

        // Se não houver Authorization ou não começar com "Bearer ", apenas segue o fluxo
        // (requisição anônima ou endpoint público)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrai apenas o token, removendo o prefixo "Bearer "
        final String token = authHeader.substring(7);
        final String username;
        try {
            // Tenta extrair o "username" (no seu caso, email) de dentro do JWT
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            // Qualquer erro na extração indica token inválido ou expirado
            log.warn("Token inválido ou expirado: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Se conseguiu extrair username e ainda não há autenticação registrada no contexto
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Carrega o usuário a partir do e-mail
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Confere se o token é válido para esse usuário
            if (jwtService.isTokenValid(token, userDetails.getUsername())) {

                // Cria o objeto de autenticação com usuário e suas authorities (roles)
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,              // principal (usuário autenticado)
                                null,                     // credenciais (não precisamos da senha aqui)
                                userDetails.getAuthorities() // permissões (ROLE_ADMIN, etc.)
                        );

                // Adiciona detalhes da requisição (IP, sessão, etc.)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Registra a autenticação no contexto de segurança do Spring
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continua o fluxo da requisição (passa para o próximo filtro/controller)
        filterChain.doFilter(request, response);
    }
}
