package app; // Pacote onde o controller de categorias está localizado

// DTO de entrada para criar/atualizar categoria
import dominio.dto.request.CategoriaRequestDTO;
// DTO de saída para devolver categoria ao cliente
import dominio.dto.response.CategoriaResponseDTO;
// Serviço contendo a regra de negócio de categorias
import servicos_tecnicos.CategoriaService;

// Anotações do Swagger/OpenAPI para documentar endpoints
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

// Validação dos dados de entrada
import jakarta.validation.Valid;
// Lombok: gera construtor com campos final
import lombok.RequiredArgsConstructor;

// Spring Data para paginação e ordenação
import org.springframework.data.domain.*;
// Classes HTTP para resposta
import org.springframework.http.*;
// Controle de autorização por role
import org.springframework.security.access.prepost.PreAuthorize;
// Anotações para REST controller
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciamento de categorias.
 * Acesso restrito a ADMIN/EDITOR (rotas administrativas).
 */
@RestController // Indica que é um controller REST que retorna JSON
@RequestMapping("/api/admin/categorias") // Prefixo base das rotas de categoria administrativa
@RequiredArgsConstructor // Lombok: gera construtor com o campo final categoriaService
@Tag(name = "Categorias", description = "Gerenciamento de categorias (Admin/Editor)") // Grupo no Swagger
@SecurityRequirement(name = "bearerAuth") // Exige autenticação Bearer (JWT) nos endpoints
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // CORS liberado para esses frontends
public class CategoriaController {

    // Serviço responsável pelas operações de categoria
    private final CategoriaService categoriaService;

    // ====== CRIAR ======

    /**
     * Cria uma nova categoria de artigos.
     */
    @Operation(
            summary = "Criar nova categoria",
            description = "Cadastra uma nova categoria de artigos no sistema."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')") // Apenas ADMIN e EDITOR podem criar categoria
    @PostMapping // POST /api/admin/categorias
    public ResponseEntity<CategoriaResponseDTO> criarCategoria(
            @Valid @RequestBody CategoriaRequestDTO request // Dados da nova categoria
    ) {
        // Chama o service para criar a categoria
        CategoriaResponseDTO categoria = categoriaService.criarCategoria(request);
        // Retorna status 201 (Created) com a categoria criada
        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }

    // ====== BUSCAR ======

    /**
     * Busca uma categoria específica pelo ID.
     */
    @Operation(
            summary = "Buscar categoria por ID",
            description = "Retorna detalhes de uma categoria existente."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')") // Restrito a ADMIN/EDITOR
    @GetMapping("/{id}") // GET /api/admin/categorias/{id}
    public ResponseEntity<CategoriaResponseDTO> buscarPorId(@PathVariable Long id) {
        // Busca via service e retorna 200 com o DTO
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    /**
     * Busca uma categoria pelo nome.
     */
    @Operation(
            summary = "Buscar categoria por nome",
            description = "Localiza categoria existente pelo nome informado."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @GetMapping("/buscar") // GET /api/admin/categorias/buscar?nome=...
    public ResponseEntity<CategoriaResponseDTO> buscarPorNome(
            @Parameter(description = "Nome da categoria", required = true)
            @RequestParam String nome // Nome passado como query param
    ) {
        return ResponseEntity.ok(categoriaService.buscarPorNome(nome));
    }

    // ====== LISTAR ======

    /**
     * Lista paginada de todas as categorias.
     */
    @Operation(
            summary = "Listar todas as categorias",
            description = "Lista paginada de todas as categorias cadastradas."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @GetMapping // GET /api/admin/categorias
    public ResponseEntity<Page<CategoriaResponseDTO>> listarTodas(
            @RequestParam(defaultValue = "0") int page,     // Número da página
            @RequestParam(defaultValue = "10") int size,    // Tamanho da página
            @RequestParam(defaultValue = "nome") String sort, // Campo de ordenação
            @RequestParam(defaultValue = "asc") String direction // Direção: asc/desc
    ) {
        // Define a direção do sort com base no parâmetro direction
        Sort.Direction dir =
                direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Cria objeto Pageable com paginação e ordenação
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        // Chama o service para listar todas as categorias de forma paginada
        return ResponseEntity.ok(categoriaService.listarTodas(pageable));
    }

    /**
     * Lista todas as categorias que estão ativas.
     */
    @Operation(
            summary = "Listar categorias ativas",
            description = "Lista todas as categorias que estão ativas."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @GetMapping("/ativas") // GET /api/admin/categorias/ativas
    public ResponseEntity<List<CategoriaResponseDTO>> listarAtivas() {
        return ResponseEntity.ok(categoriaService.listarAtivas());
    }

    /**
     * Lista categorias mais populares, geralmente com base na quantidade de artigos publicados.
     */
    @Operation(
            summary = "Listar categorias mais populares",
            description = "Retorna categorias com mais artigos publicados."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @GetMapping("/populares") // GET /api/admin/categorias/populares
    public ResponseEntity<Page<CategoriaResponseDTO>> listarMaisPopulares(
            @RequestParam(defaultValue = "0") int page, // Página atual
            @RequestParam(defaultValue = "5") int size  // Tamanho da página
    ) {
        // Paginador com ordenação decrescente por nome (lógica detalhada está no service/repository)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "nome"));

        return ResponseEntity.ok(categoriaService.listarMaisPopulares(pageable));
    }

    /**
     * Lista apenas categorias que possuem artigos publicados.
     */
    @Operation(
            summary = "Listar categorias com artigos publicados",
            description = "Exibe apenas categorias que possuem artigos publicados."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @GetMapping("/com-artigos") // GET /api/admin/categorias/com-artigos
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategoriasComArtigos() {
        return ResponseEntity.ok(categoriaService.listarComArtigosPublicados());
    }


    /**
     * Atualiza os dados de uma categoria existente.
     */
    @Operation(
            summary = "Atualizar categoria",
            description = "Edita os dados de uma categoria existente."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @PutMapping("/{id}") // PUT /api/admin/categorias/{id}
    public ResponseEntity<CategoriaResponseDTO> atualizarCategoria(
            @PathVariable Long id, // ID da categoria na URL
            @Valid @RequestBody CategoriaRequestDTO request // Dados atualizados
    ) {
        return ResponseEntity.ok(categoriaService.atualizarCategoria(id, request));
    }

    // ====== STATUS E EXCLUSÃO ======

    /**
     * Altera o status ativa/inativa de uma categoria (soft).
     */
    @Operation(
            summary = "Alterar status (ativa/inativa)",
            description = "Ativa ou desativa uma categoria existente."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @PatchMapping("/{id}/status") // PATCH /api/admin/categorias/{id}/status?ativa=true
    public ResponseEntity<Void> alterarStatus(
            @PathVariable Long id,
            @RequestParam Boolean ativa // true para ativar, false para desativar
    ) {
        categoriaService.alterarStatus(id, ativa);
        // NO_CONTENT: operação realizada sem corpo na resposta
        return ResponseEntity.noContent().build();
    }

    /**
     * Reativa uma categoria previamente desativada (caso de uso mais explícito).
     */
    @Operation(
            summary = "Reativar categoria",
            description = "Reativa uma categoria previamente desativada."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @PatchMapping("/{id}/reativar") // PATCH /api/admin/categorias/{id}/reativar
    public ResponseEntity<CategoriaResponseDTO> reativarCategoria(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.reativarCategoria(id));
    }

    /**
     * Soft-delete: desativa uma categoria sem removê-la fisicamente do banco.
     */
    @Operation(
            summary = "Excluir categoria (soft-delete)",
            description = "Desativa uma categoria sem removê-la do banco."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @DeleteMapping("/{id}") // DELETE /api/admin/categorias/{id}
    public ResponseEntity<Void> deletarCategoria(@PathVariable Long id) {
        categoriaService.deletarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exclusão permanente da categoria (hard delete) – apenas ADMIN.
     */
    @Operation(
            summary = "Excluir permanentemente",
            description = "Remove uma categoria do banco de dados (somente Admin)."
    )
    @PreAuthorize("hasRole('ADMIN')") // Apenas administrador pode remover de vez
    @DeleteMapping("/{id}/permanente") // DELETE /api/admin/categorias/{id}/permanente
    public ResponseEntity<Void> deletarPermanente(@PathVariable Long id) {
        categoriaService.deletarCategoriaPermanente(id);
        return ResponseEntity.noContent().build();
    }

    // ====== ESTATÍSTICAS ======

    /**
     * Retorna estatísticas/indicadores de uma categoria específica.
     */
    @Operation(
            summary = "Estatísticas da categoria",
            description = "Fornece informações analíticas e quantitativas da categoria."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @GetMapping("/{id}/estatisticas") // GET /api/admin/categorias/{id}/estatisticas
    public ResponseEntity<CategoriaResponseDTO> estatisticasCategoria(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obterEstatisticas(id));
    }

    /**
     * Conta o total de categorias marcadas como ativas.
     */
    @Operation(
            summary = "Contar categorias ativas",
            description = "Conta o número total de categorias ativas."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @GetMapping("/contagem/ativas") // GET /api/admin/categorias/contagem/ativas
    public ResponseEntity<Long> contarCategoriasAtivas() {
        return ResponseEntity.ok(categoriaService.contarCategoriasAtivas());
    }

    /**
     * Lista categorias ordenadas pela quantidade de artigos publicados.
     */
    @Operation(
            summary = "Listar categorias ordenadas por artigos",
            description = "Ordena categorias com base na quantidade de artigos publicados."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @GetMapping("/ordenadas/artigos") // GET /api/admin/categorias/ordenadas/artigos
    public ResponseEntity<List<CategoriaResponseDTO>> listarPorQuantidadeDeArtigos() {
        return ResponseEntity.ok(categoriaService.listarOrdenadasPorQuantidadeArtigos());
    }
}
