//package app;
//
//import dominio.dto.request.ComentarioRequestDTO;
//import dominio.dto.response.ComentarioResponseDTO;
//import servicos_tecnicos.ComentarioService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.*;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/admin/comentarios")
//@RequiredArgsConstructor
//@Tag(name = "Comentários", description = "Gerenciamento e moderação de comentários de artigos")
//@SecurityRequirement(name = "bearerAuth")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
//public class ComentarioController {
//
//    private final ComentarioService comentarioService;
//
//    // ====== CRIAR ======
//    @Operation(summary = "Criar comentário", description = "Cria um novo comentário associado a um artigo.")
//    @PostMapping
//    public ResponseEntity<ComentarioResponseDTO> criarComentario(@Valid @RequestBody ComentarioRequestDTO request) {
//        ComentarioResponseDTO comentario = comentarioService.criarComentario(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(comentario);
//    }
//
//    // ====== BUSCAR ======
//    @Operation(summary = "Buscar por ID", description = "Obtém detalhes de um comentário pelo seu ID.")
//    @GetMapping("/{id}")
//    public ResponseEntity<ComentarioResponseDTO> buscarPorId(@PathVariable Long id) {
//        return ResponseEntity.ok(comentarioService.buscarPorId(id));
//    }
//
//    @Operation(summary = "Listar todos", description = "Retorna uma lista paginada de todos os comentários com filtros.")
//    @GetMapping
//    public ResponseEntity<Page<ComentarioResponseDTO>> listarTodos(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "dataComentario") String sort,
//            @RequestParam(defaultValue = "desc") String direction,
//            @RequestParam(required = false) Long artigoId,
//            @RequestParam(required = false) Long usuarioId,
//            @RequestParam(required = false) Boolean aprovado) {
//
//        Sort.Direction sortDir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
//        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sort));
//        Page<ComentarioResponseDTO> comentarios = comentarioService.buscarComFiltros(artigoId, usuarioId, aprovado, pageable);
//        return ResponseEntity.ok(comentarios);
//    }
//
//    // ====== MODERAÇÃO ======
//    @Operation(summary = "Aprovar comentário", description = "Aprova um comentário para exibição pública.")
//    @PatchMapping("/{id}/aprovar")
//    public ResponseEntity<ComentarioResponseDTO> aprovar(@PathVariable Long id) {
//        return ResponseEntity.ok(comentarioService.aprovarComentario(id));
//    }
//
//    @Operation(summary = "Reprovar comentário", description = "Reprova e remove um comentário.")
//    @DeleteMapping("/{id}/reprovar")
//    public ResponseEntity<String> reprovar(@PathVariable Long id) {
//        comentarioService.reprovarComentario(id);
//        return ResponseEntity.ok("Comentário reprovado e removido com sucesso.");
//    }
//
//    @Operation(summary = "Aprovar em lote", description = "Aprova múltiplos comentários de uma vez.")
//    @PatchMapping("/aprovar-lote")
//    public ResponseEntity<Map<String, Object>> aprovarEmLote(@RequestBody List<Long> ids) {
//        int total = comentarioService.aprovarEmLote(ids);
//        return ResponseEntity.ok(Map.of("mensagem", "Aprovação em lote concluída", "total_aprovados", total));
//    }
//
//    @Operation(summary = "Reprovar em lote", description = "Reprova e remove vários comentários.")
//    @DeleteMapping("/reprovar-lote")
//    public ResponseEntity<Map<String, Object>> reprovarEmLote(@RequestBody List<Long> ids) {
//        int total = comentarioService.reprovarEmLote(ids);
//        return ResponseEntity.ok(Map.of("mensagem", "Reprovação concluída", "total_excluidos", total));
//    }
//
//    // ====== ESTATÍSTICAS ======
//    @Operation(summary = "Contagem geral", description = "Retorna contagem e status dos comentários no sistema.")
//    @GetMapping("/estatisticas")
//    public ResponseEntity<Map<String, Object>> estatisticas() {
//        var stats = comentarioService.obterEstatisticas();
//        return ResponseEntity.ok(Map.of(
//                "total", stats.total(),
//                "aprovados", stats.aprovados(),
//                "pendentes", stats.pendentes()
//        ));
//    }
//
//    @Operation(summary = "Contar pendentes", description = "Conta os comentários aguardando aprovação.")
//    @GetMapping("/pendentes/contagem")
//    public ResponseEntity<Map<String, Long>> pendentes() {
//        return ResponseEntity.ok(Map.of("total_pendentes", comentarioService.contarPendentes()));
//    }
//
//    @Operation(summary = "Contar por artigo", description = "Conta o número de comentários de um artigo específico.")
//    @GetMapping("/artigo/{artigoId}/contagem")
//    public ResponseEntity<Map<String, Long>> contarPorArtigo(@PathVariable Long artigoId) {
//        Long total = comentarioService.contarComentariosDoArtigo(artigoId);
//        return ResponseEntity.ok(Map.of("artigoId", artigoId, "total_comentarios", total));
//    }
//}
