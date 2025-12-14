package app; // Pacote onde fica o controller público de categorias

// Entidade JPA de Categoria (mapeada para tabela no banco)
import dominio.entidades.Categoria;
// DTO usado para expor dados de categoria ao frontend
import dominio.dto.response.CategoriaResponseDTO;
// Repositório Spring Data para operações com Categoria
import lib.repository.CategoriaRepository;

// Swagger/OpenAPI para documentação dos endpoints
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// Lombok: gera construtor com campos final para injeção
import lombok.RequiredArgsConstructor;
// Construção de respostas HTTP
import org.springframework.http.ResponseEntity;
// Anotações REST
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST público para categorias.
 * Não exige autenticação. Usado pelo site para exibir categorias.
 */
@RestController // Indica que a classe expõe endpoints REST (JSON)
@RequestMapping("/api/public/categorias") // Prefixo base das rotas públicas de categoria
@RequiredArgsConstructor // Lombok: gera construtor com o campo final categoriaRepository
@Tag(
        name = "Categorias Públicas",
        description = "Endpoints públicos para consulta de categorias"
) // Grupo no Swagger
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Libera CORS para esses frontends
public class CategoriaPublicController {

    // Repositório usado para acessar categorias no banco
    private final CategoriaRepository categoriaRepository;

    /**
     * Lista todas as categorias ativas, ordenadas por nome (ascendente).
     */
    @Operation(
            summary = "Listar categorias ativas",
            description = "Lista todas as categorias ativas"
    )
    @GetMapping // GET /api/public/categorias
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategorias() {
        // Busca somente categorias com flag ativa = true, ordenadas por nome asc
        List<Categoria> categorias = categoriaRepository.findByAtivaTrueOrderByNomeAsc();

        // Converte a lista de entidades Categoria para lista de DTOs CategoriaResponseDTO
        List<CategoriaResponseDTO> response = categorias.stream()
                .map(this::converterParaDTO) // converte cada Categoria em DTO
                .collect(Collectors.toList());

        // Retorna a lista com status 200 (OK)
        return ResponseEntity.ok(response);
    }

    /**
     * Busca uma categoria específica pelo ID, apenas se estiver ativa.
     */
    @Operation(
            summary = "Buscar categoria por ID",
            description = "Obtém detalhes de uma categoria específica"
    )
    @GetMapping("/{id}") // GET /api/public/categorias/{id}
    public ResponseEntity<CategoriaResponseDTO> buscarPorId(@PathVariable Long id) {
        return categoriaRepository.findById(id) // Optional<Categoria>
                // Garante que a categoria está ativa
                .filter(Categoria::getAtiva)
                // Converte para DTO
                .map(this::converterParaDTO)
                // Encapsula em ResponseEntity.ok(...)
                .map(ResponseEntity::ok)
                // Se não existir ou não estiver ativa, retorna 404
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista categorias que possuem artigos publicados (apenas essas).
     */
    @Operation(
            summary = "Categorias com artigos",
            description = "Lista categorias que possuem artigos publicados"
    )
    @GetMapping("/com-artigos") // GET /api/public/categorias/com-artigos
    public ResponseEntity<List<CategoriaResponseDTO>> listarComArtigos() {
        // Busca categorias que têm pelo menos um artigo publicado (query customizada no repo)
        List<Categoria> categorias = categoriaRepository.findCategoriasComArtigosPublicados();

        // Converte a lista de entidades Categoria para lista de DTOs
        List<CategoriaResponseDTO> response = categorias.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Método utilitário para converter a entidade Categoria em CategoriaResponseDTO.
     * Evita expor a entidade diretamente ao frontend.
     */
    private CategoriaResponseDTO converterParaDTO(Categoria categoria) {
        return CategoriaResponseDTO.builder()
                .id(categoria.getId())                  // ID da categoria
                .nome(categoria.getNome())              // Nome exibido no site
                .descricao(categoria.getDescricao())    // Descrição/legenda
                .cor(categoria.getCor())                // Cor usada no frontend (ex.: #FF0000)
                .icone(categoria.getIcone())            // Ícone (por exemplo, nome de classe ou SVG)
                .ativa(categoria.getAtiva())            // Flag indicando se está ativa
                .build();
    }
}
