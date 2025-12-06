package app; // Pacote onde o controller de usuários está definido

// Enum com os tipos de usuário (ADMIN, EDITOR, REDATOR, LEITOR)
import dominio.enums.TipoUsuario;
// DTO de entrada para criar/atualizar usuário (lado admin)
import dominio.dto.request.UsuarioRequestDTO;
// DTO de saída para devolver dados de usuário
import dominio.dto.response.UsuarioResponseDTO;
// Serviço com as regras de negócio de usuário
import servicos_tecnicos.UsuarioService;
// DTO de cadastro público (usado no signup)
import dominio.dto.RegisterRequest;
// Entidade Usuario persistida no banco
import dominio.entidades.Usuario;

// Classes HTTP e REST
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
// Lombok: gera construtor com campos final
import lombok.RequiredArgsConstructor;
// Paginação e ordenação
import org.springframework.data.domain.*;
// Segurança: controle por roles
import org.springframework.security.access.prepost.PreAuthorize;
// Swagger/OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

// Validação de DTOs
import jakarta.validation.Valid;

/**
 * Controller REST para gestão de usuários.
 * Contém:
 * - endpoint público de cadastro (LEITOR)
 * - endpoints administrativos (CRUD completo)
 */
@RestController // Indica que expõe endpoints REST (JSON)
@RequestMapping("/api/admin/usuarios") // Base das rotas administrativas
@RequiredArgsConstructor // Lombok: injeta UsuarioService via construtor
@Tag(name = "Usuários", description = "Gerenciamento completo de usuários (somente Admin)")
@SecurityRequirement(name = "bearerAuth") // Exige JWT nos endpoints (exceto o público, ver rota)
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Libera CORS para esses frontends
public class UsuarioController {

    // Serviço responsável pela lógica de usuários
    private final UsuarioService usuarioService;

    // ====== ENDPOINT PÚBLICO PARA CADASTRO DE USUÁRIOS COMUNS ======

    /**
     * Cadastro público de novos usuários do tipo LEITOR.
     * Não exige estar logado (rota pública).
     */
    @PostMapping("/public/cadastro") // POST /api/admin/usuarios/public/cadastro
    @Operation(
            summary="Cadastro público de novos usuários",
            description="Permite qualquer usuário se cadastrar como LEITOR."
    )
    public ResponseEntity<?> cadastroPublico(@Valid @RequestBody RegisterRequest req) {
        // Verifica se o e-mail já existe
        if (usuarioService.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("E-mail já cadastrado.");
        }
        // Cria novo usuário padrão LEITOR
        Usuario user = usuarioService.criarNovoUsuario(req);
        // Retorna 201 (Created) com mensagem
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuário criado com sucesso! Faça login.");
    }

    // ====== CRUD ADMIN ======

    /**
     * Cria um novo usuário (qualquer tipo), apenas para Admins.
     */
    @Operation(
            summary = "Cadastrar novo usuário",
            description = "Cria um novo usuário. Acesso restrito a Admins."
    )
    @PreAuthorize("hasRole('ADMIN')") // Apenas ADMIN
    @PostMapping // POST /api/admin/usuarios
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(
            @Valid @RequestBody UsuarioRequestDTO request
    ) {
        UsuarioResponseDTO usuario = usuarioService.criarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    /**
     * Listagem paginada e filtrável de usuários.
     */
    @Operation(
            summary = "Listar usuários",
            description = "Lista paginada e filtrável dos usuários cadastrados."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping // GET /api/admin/usuarios
    public ResponseEntity<Page<UsuarioResponseDTO>> listarUsuarios(
            @Parameter(description = "Página (0-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Itens por página")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Campo de ordenação")
            @RequestParam(defaultValue = "nome") String sort,

            @Parameter(description = "Direção da ordenação")
            @RequestParam(defaultValue = "asc") String direction,

            @Parameter(description = "Filtrar por nome")
            @RequestParam(required = false) String nome,

            @Parameter(description = "Filtrar por e-mail")
            @RequestParam(required = false) String email,

            @Parameter(description = "Filtrar por tipo de usuário")
            @RequestParam(required = false) TipoUsuario tipo,

            @Parameter(description = "Filtrar por status (ativo)")
            @RequestParam(required = false) Boolean ativo
    ) {

        // Define direção ASC/DESC
        Sort.Direction dir =
                direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Cria objeto Pageable com paginação e ordenação
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        // Se algum filtro foi informado, usa busca filtrada; senão, lista todos
        Page<UsuarioResponseDTO> usuarios =
                (nome != null || email != null || tipo != null || ativo != null)
                        ? usuarioService.buscarComFiltros(nome, email, tipo, ativo, pageable)
                        : usuarioService.listarTodos(pageable);

        return ResponseEntity.ok(usuarios);
    }

    /**
     * Busca usuário por ID.
     */
    @Operation(
            summary = "Buscar usuário por ID",
            description = "Obtém dados completos de um usuário."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}") // GET /api/admin/usuarios/{id}
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    /**
     * Atualiza os dados de um usuário específico.
     */
    @Operation(
            summary = "Atualizar usuário",
            description = "Permite ao Admin atualizar os dados do usuário."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}") // PUT /api/admin/usuarios/{id}
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO request
    ) {
        return ResponseEntity.ok(usuarioService.atualizarUsuario(id, request));
    }

    /**
     * Altera o status (ativo/inativo) de um usuário – soft delete.
     */
    @Operation(
            summary = "Alterar status do usuário",
            description = "Ativa ou desativa um usuário (soft delete)."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status") // PATCH /api/admin/usuarios/{id}/status?ativo=true
    public ResponseEntity<Void> alterarStatus(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean ativo
    ) {
        usuarioService.alterarStatus(id, ativo);
        return ResponseEntity.noContent().build();
    }

    /**
     * Desativa logicamente um usuário (equivalente a soft delete).
     */
    @Operation(
            summary = "Excluir usuário",
            description = "Desativa logicamente um usuário (soft delete)."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}") // DELETE /api/admin/usuarios/{id}
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista usuários filtrando por tipo (papel: ADMIN, EDITOR, etc.).
     */
    @Operation(
            summary = "Listar usuários por tipo",
            description = "Lista usuários por papel (ADMIN, EDITOR, REDATOR, LEITOR)."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/tipo/{tipo}") // GET /api/admin/usuarios/tipo/{tipo}
    public ResponseEntity<Page<UsuarioResponseDTO>> listarPorTipo(
            @PathVariable TipoUsuario tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nome"));
        return ResponseEntity.ok(usuarioService.listarPorTipo(tipo, pageable));
    }
}
