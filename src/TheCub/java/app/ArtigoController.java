package app; // Pacote onde o controller está localizado no projeto

// Enum que representa os possíveis status de um artigo (RASCUNHO, PUBLICADO, etc.)
import dominio.enums.StatusArtigo;
// DTO usado para receber os dados no corpo da requisição ao criar/atualizar artigo
import dominio.dto.request.ArtigoRequestDTO;
// DTO usado para devolver os dados de artigo na resposta da API
import dominio.dto.ArtigoResponse;
// Serviço responsável pela regra de negócio relacionada a artigos
import servicos_tecnicos.ArtigoService;

// Anotações do Swagger/OpenAPI para documentação da API
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

// Anotação para ativar validação nos DTOs (ex.: @NotNull, @Size)
import jakarta.validation.Valid;
// Lombok: gera automaticamente um construtor com os campos finais (final)
import lombok.RequiredArgsConstructor;

// Classes do Spring Data para paginação e ordenação
import org.springframework.data.domain.*;
import org.springframework.http.*;
// Controle de autorização baseado em roles (Spring Security)
import org.springframework.security.access.prepost.PreAuthorize;
// Anotações de mapeamento REST (GET, POST, etc.)
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST responsável pelo gerenciamento de artigos/notícias.
 * Todos os endpoints começam com o caminho base /api/artigos.
 */
@RestController // Indica que esta classe é um controller REST (retorna JSON/objetos e não views)
@RequestMapping("/api/artigos") // Prefixo comum para todas as rotas deste controller
@RequiredArgsConstructor // Lombok: cria construtor com os campos final (injeção de dependência)
@Tag(name = "Artigos", description = "Gerenciamento de Noticias") // Agrupa endpoints no Swagger sob a tag "Artigos"
@SecurityRequirement(name = "bearerAuth") // Indica no Swagger que os endpoints usam autenticação bearer (JWT)
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Habilita CORS para esses frontends
public class ArtigoController {

    // Serviço de artigos injetado via construtor (por causa de @RequiredArgsConstructor)
    private final ArtigoService artigoService;

    /**
     * Endpoint para criar uma nova notícia/artigo.
     * Requer papel ADMIN, EDITOR ou REDATOR.
     */
    @Operation(summary = "Criar nova noticia") // Descrição curta para o Swagger
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Noticia criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição inválida."),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão.")
    })
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','REDATOR')") // Somente esses perfis podem criar artigo
    @PostMapping // Mapeia requisições HTTP POST em /api/artigos
    public ResponseEntity<ArtigoResponse> criarArtigo(
            @Valid @RequestBody ArtigoRequestDTO request // Corpo da requisição será validado e mapeado para o DTO
    ) {
        // Chama o service para criar o artigo a partir do DTO recebido
        ArtigoResponse artigo = artigoService.criarArtigo(request);
        // Retorna status 201 (CREATED) com o artigo criado no corpo da resposta
        return ResponseEntity.status(HttpStatus.CREATED).body(artigo);
    }

    /**
     * Endpoint para listar artigos com filtros opcionais e paginação.
     * Requer papel ADMIN ou EDITOR.
     */
    @Operation(summary = "Listar artigos")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')") // Apenas ADMIN e EDITOR podem listar com esse endpoint interno
    @GetMapping // Mapeia requisições HTTP GET em /api/artigos
    public ResponseEntity<Page<ArtigoResponse>> listarArtigos(
            @Parameter(description = "Status do artigo")
            @RequestParam(required = false) StatusArtigo status, // Filtro opcional por status

            @Parameter(description = "ID da categoria")
            @RequestParam(required = false) Long categoriaId, // Filtro opcional por categoria

            @Parameter(description = "ID do autor")
            @RequestParam(required = false) Long autorId, // Filtro opcional por autor

            @Parameter(description = "Título")
            @RequestParam(required = false) String titulo, // Filtro opcional por título (ex.: busca por nome)

            @RequestParam(defaultValue = "0") int page, // Página atual (default 0)
            @RequestParam(defaultValue = "10") int size, // Tamanho da página (default 10 registros)
            @RequestParam(defaultValue = "dataCriacao") String sort, // Campo para ordenação (default dataCriacao)
            @RequestParam(defaultValue = "desc") String direction // Direção da ordenação: asc/desc (default desc)
    ) {

        // Define a direção da ordenação com base no parâmetro direction
        Sort sortOrder = Sort.by(
                direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sort
        );

        // Cria o objeto Pageable com página, tamanho e ordenação
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // Busca os artigos no service aplicando filtros e paginação
        Page<ArtigoResponse> artigos =
                artigoService.buscarPorFiltros(status, categoriaId, autorId, titulo, pageable);

        // Retorna a página de artigos com status 200 (OK)
        return ResponseEntity.ok(artigos);
    }

    /**
     * Endpoint para buscar uma notícia específica pelo ID.
     * Requer papel ADMIN, EDITOR ou REDATOR.
     */
    @Operation(summary = "Buscar noticia por ID")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','REDATOR')") // Papéis autorizados para visualizar um artigo específico
    @GetMapping("/{id}") // Mapeia GET /api/artigos/{id}
    public ResponseEntity<ArtigoResponse> buscarPorId(@PathVariable Long id) {
        // Chama o serviço para buscar o artigo pelo ID
        ArtigoResponse artigo = artigoService.buscarPorId(id);
        // Retorna o artigo encontrado com status 200 (OK)
        return ResponseEntity.ok(artigo);
    }

    /**
     * Endpoint para atualizar um artigo existente.
     * Requer papel ADMIN, EDITOR ou REDATOR.
     */
    @Operation(summary = "Atualizar artigo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artigo atualizado."),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado.")
    })
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','REDATOR')") // Perfis que podem atualizar
    @PutMapping("/{id}") // Mapeia PUT /api/artigos/{id}
    public ResponseEntity<ArtigoResponse> atualizarArtigo(
            @PathVariable Long id, // ID do artigo na URL
            @Valid @RequestBody ArtigoRequestDTO request // Dados novos do artigo vindos no corpo da requisição
    ) {

        // Chama o serviço para atualizar o artigo com o ID informado
        ArtigoResponse artigoAtualizado = artigoService.atualizarArtigo(id, request);

        // Retorna o artigo atualizado com status 200 (OK)
        return ResponseEntity.ok(artigoAtualizado);
    }

    /**
     * Endpoint para publicar um artigo (mudança de status, data de publicação, etc.).
     * Requer papel ADMIN ou EDITOR.
     */
    @Operation(summary = "Publicar artigo")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')") // Apenas ADMIN e EDITOR podem publicar
    @PatchMapping("/{id}/publicar") // Mapeia PATCH /api/artigos/{id}/publicar
    public ResponseEntity<ArtigoResponse> publicarArtigo(@PathVariable Long id) {
        // Chama o serviço para publicar o artigo (normalmente altera status para PUBLICADO e seta dataPublicacao)
        ArtigoResponse artigo = artigoService.publicarArtigo(id);
        // Retorna o artigo já publicado
        return ResponseEntity.ok(artigo);
    }

    /**
     * Endpoint para arquivar um artigo (por exemplo, mudar status para ARQUIVADO).
     * Requer papel ADMIN ou EDITOR.
     */
    @Operation(summary = "Arquivar artigo")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')") // Apenas ADMIN e EDITOR podem arquivar
    @PatchMapping("/{id}/arquivar") // Mapeia PATCH /api/artigos/{id}/arquivar
    public ResponseEntity<String> arquivarArtigo(@PathVariable Long id) {
        // Chama o serviço para arquivar o artigo (muda status, etc.)
        artigoService.arquivarArtigo(id);
        // Retorna uma mensagem de sucesso simples com status 200 (OK)
        return ResponseEntity.ok("🗂️ Artigo arquivado com sucesso.");
    }

    /**
     * Endpoint para excluir permanentemente um artigo.
     * Requer papel ADMIN.
     */
    @Operation(summary = "Excluir artigo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Artigo excluído."),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado.")
    })
    @PreAuthorize("hasRole('ADMIN')") // Apenas ADMIN pode deletar artigo definitivamente
    @DeleteMapping("/{id}") // Mapeia DELETE /api/artigos/{id}
    public ResponseEntity<Void> deletarArtigo(@PathVariable Long id) {
        // Chama o serviço para deletar o artigo pelo ID
        artigoService.deletarArtigo(id);
        // Retorna status 204 (NO CONTENT) sem corpo de resposta
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para retornar estatísticas de artigos para dashboard/admin.
     * Pode incluir visualizações, contagem de avaliações, etc.
     * Requer papel ADMIN, EDITOR ou REDATOR.
     */
    // NOVO: Estatísticas detalhadas para dashboard/admin (visualizações, avaliações)
    @GetMapping("/estatisticas") // Mapeia GET /api/artigos/estatisticas
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR','REDATOR')") // Perfis que podem ver estatísticas internas
    public ResponseEntity<List<ArtigoResponse>> estatisticas() {
        // Lista todos os artigos incluindo campos estatísticos adicionais (views, avaliações, etc.)
        List<ArtigoResponse> lista = artigoService.listarTodosComEstatisticas();
        // Retorna a lista com status 200 (OK)
        return ResponseEntity.ok(lista);
    }

    /**
     * Endpoint para listar somente artigos publicados, com paginação.
     * Requer papel ADMIN ou EDITOR.
     */
    // Listar artigos publicados (opcional)
    @Operation(summary = "Listar artigos publicados")
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')") // Perfis autorizados a acessar esta listagem interna
    @GetMapping("/publicados") // Mapeia GET /api/artigos/publicados
    public ResponseEntity<Page<ArtigoResponse>> listarPublicados(
            @RequestParam(defaultValue = "0") int page, // Página atual (default 0)
            @RequestParam(defaultValue = "10") int size // Tamanho da página (default 10)
    ) {
        // Cria Pageable para ordenar por dataPublicacao em ordem decrescente (mais recentes primeiro)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataPublicacao"));

        // Chama o serviço para buscar apenas artigos com status PUBLICADO (ou lógica equivalente)
        Page<ArtigoResponse> artigos = artigoService.listarPublicados(pageable);

        // Retorna a página de artigos publicados
        return ResponseEntity.ok(artigos);
    }

    // Mais endpoints conforme seu original ...
}
