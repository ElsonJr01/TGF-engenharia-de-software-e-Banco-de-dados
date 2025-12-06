package app; // Pacote onde o controller de editais está localizado

// DTO de entrada para criar/atualizar edital
import dominio.dto.request.EditalRequestDTO;
// DTO de saída para devolver edital ao cliente
import dominio.dto.response.EditalResponseDTO;
// Serviço com a regra de negócio de editais
import servicos_tecnicos.EditalService;

// Swagger/OpenAPI para documentação
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// Lombok: gera construtor com campos final e logger
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Spring Data para paginação
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// Construção de respostas HTTP
import org.springframework.http.ResponseEntity;
// Anotações REST
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para gerenciamento de editais.
 * Mistura rotas administrativas (/editais) e públicas (/public/editais).
 */
@RestController // Indica que expõe endpoints REST (JSON)
@RequestMapping("/api") // Prefixo base; os paths completos são /api/...
@RequiredArgsConstructor // Lombok: gera construtor com o campo final editalService
@Tag(name = "Editais", description = "Endpoints para gerenciamento de editais") // Grupo no Swagger
@Slf4j // Habilita o logger 'log'
public class EditalController {

    // Serviço responsável pela lógica de criação, listagem e exclusão de editais
    private final EditalService editalService;

    /**
     * Cria um novo edital (rota administrativa, deveria ser protegida por autenticação).
     */
    @PostMapping("/editais") // POST /api/editais
    @Operation(
            summary = "Criar novo edital",
            description = "Cria um novo edital no sistema (requer autenticação)"
    )
    public ResponseEntity<EditalResponseDTO> criar(@RequestBody EditalRequestDTO dto) {
        log.info("° Criando novo edital: {}", dto.getTitulo());
        try {
            // Chama o serviço para criar o edital
            EditalResponseDTO response = editalService.criar(dto);
            log.info("° Edital criado com sucesso - ID: {}", response.getId());
            // Retorna 200 OK com o edital criado (poderia ser 201, mas lógica foi mantida)
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Loga erro e propaga exceção (tratada por handler global, se existir)
            log.error(" Erro ao criar edital: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lista todos os editais ativos (rota pública) com paginação.
     */
    @GetMapping("/public/editais") // GET /api/public/editais
    @Operation(
            summary = "Listar editais",
            description = "Lista todos os editais ativos (público)"
    )
    public ResponseEntity<Page<EditalResponseDTO>> listar(Pageable pageable) {
        // Log com número da página e tamanho da página
        log.info("° Listando editais - Página: {}, Tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Usa o service para listar apenas editais ativos
        return ResponseEntity.ok(editalService.listarAtivos(pageable));
    }

    /**
     * Busca edital específico por ID (rota pública).
     */
    @GetMapping("/public/editais/{id}") // GET /api/public/editais/{id}
    @Operation(
            summary = "Buscar edital",
            description = "Busca um edital específico por ID (público)"
    )
    public ResponseEntity<EditalResponseDTO> buscar(@PathVariable Long id) {
        log.info("° Buscando edital ID: {}", id);
        // Busca o edital e retorna 200 OK com o DTO
        return ResponseEntity.ok(editalService.buscarPorId(id));
    }

    /**
     * Exclui um edital (rota administrativa, exige autenticação/regra de autorização).
     */
    @DeleteMapping("/editais/{id}") // DELETE /api/editais/{id}
    @Operation(
            summary = "Excluir edital",
            description = "Exclui um edital (requer autenticação)"
    )
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        log.info("🗑(LIXEIRA) Excluindo edital ID: {}", id);
        // Chama o service para excluir o edital
        editalService.excluir(id);
        // Retorna 204 NO CONTENT
        return ResponseEntity.noContent().build();
    }

    /**
     * Atualiza um edital existente (rota administrativa).
     */
    @PutMapping("/editais/{id}") // PUT /api/editais/{id}
    @Operation(
            summary = "Atualizar edital",
            description = "Atualiza um edital existente (requer autenticação)"
    )
    public ResponseEntity<EditalResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody EditalRequestDTO dto
    ) {
        log.info("° Atualizando edital ID: {}", id);
        // Atualiza o edital no service e retorna o DTO atualizado
        return ResponseEntity.ok(editalService.atualizar(id, dto));
    }
}
