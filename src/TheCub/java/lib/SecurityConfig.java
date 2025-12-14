package lib; // Pacote de segurança/infraestrutura da aplicação

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Classe principal de configuração de segurança (Spring Security).
 *
 * Responsável por:
 * - definir quais endpoints são públicos ou protegidos
 * - configurar CORS
 * - registrar o filtro de JWT
 * - definir política de sessão (stateless)
 * - expor beans de AuthenticationManager e PasswordEncoder
 */
@Configuration
@EnableWebSecurity               // Ativa recursos de segurança web
@EnableMethodSecurity            // Permite usar @PreAuthorize, @Secured, etc.
@RequiredArgsConstructor         // Injeta dependências finais via construtor
public class SecurityConfig {

    // Filtro que extrai e valida o JWT em cada requisição
    private final JwtAuthenticationFilter jwtAuthFilter;

    // Serviço que carrega usuários (implementação de UserDetailsService)
    private final UserDetailsService userDetailsService;

    /**
     * Define a SecurityFilterChain, ou seja,
     * toda a configuração de segurança HTTP da aplicação.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Desabilita CSRF (ok para APIs stateless com JWT)
                .csrf(csrf -> csrf.disable())

                // Pluga a configuração de CORS customizada
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Define que a sessão será STATELESS (nenhum estado no servidor)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configura quais requisições são liberadas ou exigem autenticação
                .authorizeHttpRequests(auth -> auth
                        // Raiz liberada (pode ser usada para healthcheck)
                        .requestMatchers("/").permitAll()

                        // Endpoints de autenticação (login/registro) são públicos
                        .requestMatchers("/api/auth/**").permitAll()

                        // Endpoints explicitamente públicos
                        .requestMatchers("/api/public/**").permitAll()

                        // Upload de arquivos (se você quiser público)
                        .requestMatchers("/api/upload/**").permitAll()

                        // Servir arquivos estáticos (imagens, etc.)
                        .requestMatchers("/uploads/**").permitAll()

                        // Swagger / OpenAPI
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()

                        // Actuator (monitoramento / health checks)
                        .requestMatchers("/actuator/**").permitAll()

                        // ARTIGOS
                        // GET de artigos é público (listagem/leitura)
                        .requestMatchers(HttpMethod.GET, "/api/artigos/**").permitAll()
                        // Criação, atualização e remoção exigem autenticação
                        .requestMatchers(HttpMethod.POST, "/api/artigos/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/artigos/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/artigos/**").authenticated()

                        // CATEGORIAS – leitura pública
                        .requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()

                        // EDITAIS
                        // Endpoints públicos de editais (provavelmente listagem/leitura)
                        .requestMatchers(HttpMethod.GET, "/api/public/editais/**").permitAll()
                        // Operações que alteram editais exigem autenticação
                        .requestMatchers(HttpMethod.POST, "/api/editais/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/editais/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/editais/**").authenticated()

                        // Qualquer outra requisição não mapeada acima exige autenticação
                        .anyRequest().authenticated()
                )

                // Define o UserDetailsService que o Spring Security deve usar internamente
                .userDetailsService(userDetailsService)

                // Adiciona o filtro de JWT ANTES do filtro padrão de login por formulário
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Constrói a cadeia de filtros configurada
                .build();
    }

    /**
     * Configuração CORS global para a API.
     *
     * - allowedOriginPatterns("*"): permite qualquer origem (melhor restringir em produção)
     * - allowedMethods: métodos HTTP autorizados
     * - allowedHeaders("*"): permite qualquer header
     * - allowCredentials(true): permite envio de cookies/Authorization
     * - maxAge: tempo em segundos em que o resultado do preflight (OPTIONS) pode ser cacheado
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Aceita qualquer origem (padrão com pattern, não só lista fixa)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        );

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Permite uso de credenciais (cookies, Authorization header, etc.)
        configuration.setAllowCredentials(true);

        // Cache do preflight (OPTIONS) por 1 hora
        configuration.setMaxAge(3600L);

        // Registra a configuração para todos os caminhos da API
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * PasswordEncoder usado para codificar/verificar senhas.
     *
     * ATENÇÃO: aqui está usando um "encoder" que NÃO faz hash.
     * - encode: apenas retorna a senha em texto puro
     * - matches: compara string direta
     *
     * Isso é aceitável só em ambiente de estudo/dev.
     * Em produção o ideal é usar BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            // "Codifica" a senha – aqui, apenas a retorna sem alterações
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            // Compara senha crua com senha "codificada"
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword != null
                        && rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    /**
     * Expõe o AuthenticationManager como bean para ser usado
     * em outros lugares (ex.: AuthService na hora de autenticar login).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
