package app; // Pacote onde o controller de eventos (cultura) está localizado

// DTO de entrada para criar/atualizar evento
import dominio.dto.request.CulturaRequestDTO;
// DTO de saída para devolver dados de evento ao cliente
import dominio.dto.response.CulturaResponseDTO;
// Serviço que contém a lógica de negócio dos eventos culturais
import servicos_tecnicos.CulturaService;

// Swagger/OpenAPI para documentação dos endpoints
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

// Validação de dados de entrada
import jakarta.validation.Valid;
// Lombok: gera construtor com campos final
import lombok.RequiredArgsConstructor;

// Spring Data para paginação
import org.springframework.data.domain.*;
// Classes HTTP para respostas
import org.springframework.http.*;
// Segurança: controle de acesso por roles
import org.springframework.security.access.prepost.PreAuthorize;
// Anotações REST
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST administrativo para gerenciamento de eventos universitários.
 * Acesso restrito a ADMIN e EDITOR.
 */
@RestController // Indica que a classe expõe endpoints REST (JSON)
@RequestMapping("/api/admin/eventos") // Prefixo base das rotas administrativas de eventos
@RequiredArgsConstructor // Lombok: gera construtor com o campo final eventoService
@Tag(
        name = "Eventos",
        description = "Gerenciamento de eventos universitários (Admin/Editor)"
) // Grupo no Swagger
@SecurityRequirement(name = "bearerAuth") // Exige autenticação JWT (bearerAuth no Swagger)
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Libera CORS para esses frontends
public class CulturaController {

    // Serviço responsável pela lógica de eventos (criar, listar, atualizar, etc.)
    private final CulturaService eventoService;

    /**
     * Cria um novo evento. Apenas Admins ou Editores podem acessar.
     */
    @Operation(
            summary = "Criar evento",
            description = "Cria um novo evento. Apenas Admins ou Editores podem criar."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')") // Restringe o acesso
    @PostMapping // POST /api/admin/eventos
    public ResponseEntity<CulturaResponseDTO> criarEvento(
            @Valid @RequestBody CulturaRequestDTO dto // Dados do evento vindo do frontend
    ) {
        CulturaResponseDTO evento = eventoService.criarEvento(dto);
        // Retorna 201 (Created) com o evento criado
        return ResponseEntity.status(HttpStatus.CREATED).body(evento);
    }

    /**
     * Busca detalhes completos de um evento pelo ID.
     */
    @Operation(
            summary = "Buscar por ID",
            description = "Obtém detalhes completos de um evento."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @GetMapping("/{id}") // GET /api/admin/eventos/{id}
    public ResponseEntity<CulturaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    /**
     * Lista paginada de todos os eventos cadastrados (independente de status).
     */
    @Operation(
            summary = "Listar todos",
            description = "Lista paginada de todos os eventos cadastrados."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @GetMapping // GET /api/admin/eventos
    public ResponseEntity<Page<CulturaResponseDTO>> listarTodos(
            @RequestParam(defaultValue = "0") int page,          // Página atual
            @RequestParam(defaultValue = "10") int size,         // Tamanho da página
            @RequestParam(defaultValue = "dataEvento") String sort, // Campo de ordenação
            @RequestParam(defaultValue = "asc") String direction // Direção da ordenação
    ) {

        // Converte string asc/desc para Sort.Direction
        Sort.Direction dir =
                direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Cria objeto Pageable com paginação e ordenação
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        // Chama o service para listar todos
        return ResponseEntity.ok(eventoService.listarTodos(pageable));
    }

    /**
     * Lista apenas próximos eventos (data maior que hoje).
     */
    @Operation(
            summary = "Listar próximos eventos",
            description = "Lista eventos futuros (data maior que hoje)."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @GetMapping("/proximos") // GET /api/admin/eventos/proximos
    public ResponseEntity<List<CulturaResponseDTO>> listarProximos() {
        return ResponseEntity.ok(eventoService.listarProximosEventos());
    }

    /**
     * Lista eventos em destaque (normalmente próximos 7 dias).
     */
    @Operation(
            summary = "Listar destaques",
            description = "Eventos que ocorrem nos próximos 7 dias."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @GetMapping("/destaques") // GET /api/admin/eventos/destaques
    public ResponseEntity<List<CulturaResponseDTO>> listarDestaques() {
        return ResponseEntity.ok(eventoService.listarEventosEmDestaque());
    }

    /**
     * Atualiza dados de um evento (título, descrição, data, local, etc.).
     */
    @Operation(
            summary = "Atualizar evento",
            description = "Permite editar título, descrição, data e local de eventos futuros."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @PutMapping("/{id}") // PUT /api/admin/eventos/{id}
    public ResponseEntity<CulturaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CulturaRequestDTO dto
    ) {
        return ResponseEntity.ok(eventoService.atualizarEvento(id, dto));
    }

    /**
     * Altera o status de um evento (ativo/inativo) – soft delete.
     */
    @Operation(
            summary = "Alterar status",
            description = "Ativa ou desativa um evento (soft delete)."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @PatchMapping("/{id}/status") // PATCH /api/admin/eventos/{id}/status?ativo=true
    public ResponseEntity<Void> alterarStatus(
            @PathVariable Long id,
            @RequestParam Boolean ativo // true = ativo, false = desativado
    ) {
        eventoService.alterarStatus(id, ativo);
        // NO_CONTENT: operação executada sem corpo de resposta
        return ResponseEntity.noContent().build();
    }

    /**
     * Cancela um evento (em vez de apagar), aplicável a eventos futuros.
     */
    @Operation(
            summary = "Cancelar evento",
            description = "Cancela eventos futuros (não ocorridos)."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @PatchMapping("/{id}/cancelar") // PATCH /api/admin/eventos/{id}/cancelar
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        eventoService.cancelarEvento(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Remove permanentemente um evento do banco. Apenas Admin.
     */
    @Operation(
            summary = "Excluir evento",
            description = "Remove permanentemente um evento (somente Admin)."
    )
    @PreAuthorize("hasRole('ADMIN')") // Somente ADMIN pode excluir de vez
    @DeleteMapping("/{id}") // DELETE /api/admin/eventos/{id}
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        eventoService.deletarEvento(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna estatísticas gerais de eventos (totais por status, etc.).
     */
    @Operation(
            summary = "Estatísticas gerais",
            description = "Retorna total de eventos e status (ativos, cancelados, etc)."
    )
    @PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
    @GetMapping("/estatisticas") // GET /api/admin/eventos/estatisticas
    public ResponseEntity<CulturaService.EventoEstatisticas> estatisticas() {
        return ResponseEntity.ok(eventoService.obterEstatisticas());
    }
}
