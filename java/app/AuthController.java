package app; // Pacote onde o controller de autenticação está localizado

// DTOs relacionados à autenticação (login, resposta com token, refresh)
import dominio.dto.auth.AuthRequest;
import dominio.dto.auth.AuthResponse;
import dominio.dto.auth.RefreshTokenRequest;
// DTO para cadastro (registro) de novo usuário
import dominio.dto.RegisterRequest;
// Entidade de usuário persistida no banco
import dominio.entidades.Usuario;
// Serviço de domínio para regras de negócio de usuário
import servicos_tecnicos.UsuarioService;
// Serviço responsável por gerar e validar tokens JWT
import lib.JwtService;
// Implementação de UserDetailsService usada pelo Spring Security
import lib.CustomUserDetailsService;

// Anotações do Swagger/OpenAPI para documentar os endpoints
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// Validação de dados de entrada (@Valid)
import jakarta.validation.Valid;
// Lombok: gera construtor com campos final e logger (log)
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Classes do Spring Web/Security
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST responsável pelos endpoints de autenticação:
 * login, registro, refresh de token e logout.
 */
@RestController // Indica que é um controller REST (retorna JSON)
@RequestMapping("/api/auth") // Prefixo comum: todas as rotas começam com /api/auth
@RequiredArgsConstructor // Lombok: gera construtor com os campos final para injeção
@Tag(
        name = "Autenticação",
        description = "Endpoints de login, cadastro, refresh e logout para o sistema THE CLUB"
) // Grupo no Swagger
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Permite chamadas do frontend React
@Slf4j // Lombok: adiciona um logger 'log' para mensagens
public class AuthController {

    // Gerencia o processo de autenticação (verifica credenciais)
    private final AuthenticationManager authenticationManager;
    // Serviço para geração e validação de tokens JWT
    private final JwtService jwtService;
    // CustomUserDetailsService usado internamente pelo Spring Security
    private final CustomUserDetailsService userDetailsService;
    // Serviço de usuário (buscar por e-mail, criar novo, etc.)
    private final UsuarioService usuarioService;

    // ====== LOGIN ======

    /**
     * Endpoint de login.
     * Recebe e-mail e senha, autentica e devolve um JWT.
     */
    @Operation(
            summary = "Login de usuário",
            description = "Autentica o usuário e retorna um token JWT válido por 24 horas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida, token retornado."),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou usuário inativo.")
    })
    @PostMapping("/login") // POST /api/auth/login
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            // Log informando tentativa de login
            log.info("Tentando autenticar usuário com e-mail: {}", request.getEmail());

            // Autentica o usuário com base no e-mail e senha
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getSenha()
                    )
            );

            // Busca a entidade Usuario completa para retornar dados adicionais na resposta
            Usuario usuario = usuarioService.buscarPorEmailEntidade(request.getEmail());

            // Gera o token JWT usando o e-mail como "username"
            String token = jwtService.generateToken(usuario.getEmail());

            log.info("Login bem-sucedido para usuário: {}", request.getEmail());

            // Retorna AuthResponse com token e dados básicos do usuário
            return ResponseEntity.ok(
                    AuthResponse.builder()
                            .token(token)
                            .tipo(usuario.getTipo().name()) // ADMIN, EDITOR, REDATOR, LEITOR
                            .nome(usuario.getNome())
                            .email(usuario.getEmail())
                            .id(usuario.getId())
                            .build()
            );

        } catch (AuthenticationException ex) {
            // Caso as credenciais sejam inválidas ou usuário inativo
            log.warn("Falha de autenticação para o e-mail: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Credenciais inválidas ou conta inativa"));
        } catch (Exception e) {
            // Qualquer erro inesperado durante o processo de login
            log.error("Erro inesperado durante o login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno durante a autenticação"));
        }
    }

    // ====== REGISTRO (CADASTRO) ======

    /**
     * Endpoint para cadastro de um novo usuário com perfil padrão (LEITOR).
     */
    @Operation(summary = "Cadastro de novo usuário (LEITOR)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "E-mail já cadastrado ou dados inválidos.")
    })
    @PostMapping("/register") // POST /api/auth/register
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        // Verifica se o e-mail já está em uso
        if (usuarioService.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "E-mail já cadastrado."));
        }

        // Cria um novo usuário com base no DTO de registro
        usuarioService.criarNovoUsuario(req);

        // Retorna 201 (Created) com mensagem de sucesso
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("msg", "Usuário criado com sucesso! Faça login."));
    }

    // ====== REFRESH TOKEN ======

    /**
     * Endpoint para renovar (refresh) um token JWT válido.
     * Recebe o token atual e devolve um novo com prazo renovado.
     */
    @Operation(
            summary = "Renovar token JWT",
            description = "Recebe um token JWT válido e retorna um novo token com tempo renovado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado.")
    })
    @PostMapping("/refresh") // POST /api/auth/refresh
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            // Extrai o "username" (e-mail) do token recebido
            String username = jwtService.extractUsername(request.getToken());
            log.info(" Solicitada renovação de token para usuário: {}", username);

            // Gera um novo token JWT para esse usuário
            String novoToken = jwtService.generateToken(username);

            // Opcional: busca o usuário para retornar dados junto com o novo token
            Usuario usuario = usuarioService.buscarPorEmailEntidade(username);

            // Retorna novo token e dados do usuário
            return ResponseEntity.ok(
                    AuthResponse.builder()
                            .token(novoToken)
                            .tipo(usuario.getTipo().name())
                            .nome(usuario.getNome())
                            .email(usuario.getEmail())
                            .id(usuario.getId())
                            .build()
            );

        } catch (Exception e) {
            // Qualquer problema ao extrair username ou validar o token
            log.warn("⚠️ Token de refresh inválido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "Token inválido ou expirado."));
        }
    }

    // ====== LOGOUT ======

    /**
     * Endpoint de logout.
     * Como JWT é stateless, aqui apenas informamos o cliente para descartar o token.
     */
    @Operation(
            summary = "Logout",
            description = "Finaliza a sessão localmente no cliente (token expira no dispositivo)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout efetuado com sucesso.")
    })
    @PostMapping("/logout") // POST /api/auth/logout
    public ResponseEntity<Map<String, String>> logout() {
        // Apenas loga a ação; o cliente é responsável por apagar o token localmente
        log.info("Usuário efetuou logout (token expira apenas no cliente).");

        return ResponseEntity.ok(
                Map.of(
                        "mensagem",
                        "Logout realizado com sucesso. O token expirará automaticamente."
                )
        );
    }
}
