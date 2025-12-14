package app; // Pacote onde este controller está localizado

// Entidade JPA que representa a tabela de artigos no banco
import dominio.entidades.Artigo;
// Enum que indica o status do artigo (ex.: PUBLICADO, RASCUNHO)
import dominio.enums.StatusArtigo;
// DTO usado para enviar dados de artigo na resposta da API pública
import dominio.dto.ArtigoResponse;
// Repositório Spring Data para acessar a base de artigos
import lib.repository.ArtigoRepository;
// Serviço com regras de negócio relacionadas a artigos (ex.: incrementar visualizações)
import servicos_tecnicos.ArtigoService;

// Anotações do Swagger/OpenAPI para documentar os endpoints
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// Lombok: gera construtor com todos os campos final (injeção de dependência)
import lombok.RequiredArgsConstructor;

// Classes do Spring Data para paginação e ordenação
import org.springframework.data.domain.*;
// Classe ResponseEntity para controlar status e corpo da resposta HTTP
import org.springframework.http.ResponseEntity;
// Anotações para criar endpoints REST
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST público para acesso aos artigos.
 * Não exige autenticação (rota /api/public/artigos).
 */
@RestController // Indica que esta classe expõe endpoints REST (retornam JSON)
@RequestMapping("/api/public/artigos") // Prefixo base para todos os endpoints públicos de artigo
@RequiredArgsConstructor // Lombok: gera construtor com os campos final (repository e service)
@Tag(name = "Artigos Públicos", description = "Endpoints públicos para consulta de artigos") // Agrupa no Swagger
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Libera CORS para esses frontends
public class ArtigoPublicController {

    // Repositório para operações diretas no banco envolvendo a entidade Artigo
    private final ArtigoRepository artigoRepository;

    // Serviço com lógica adicional (ex.: contabilizar visualizações)
    private final ArtigoService artigoService;

    /**
     * Endpoint público para listar artigos publicados (status PUBLICADO) de forma paginada.
     */
    @Operation(
            summary = "Listar artigos publicados",
            description = "Lista paginada de artigos com status PUBLICADO"
    )
    @GetMapping // GET /api/public/artigos
    public ResponseEntity<Page<ArtigoResponse>> listarArtigos(
            @RequestParam(defaultValue = "0") int page, // Número da página (começa em 0)
            @RequestParam(defaultValue = "10") int size // Quantidade de itens por página
    ) {
        // Cria objeto Pageable com ordenação por dataPublicacao decrescente (mais recentes primeiro)
        Pageable pageable =
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataPublicacao"));

        // Busca artigos com status PUBLICADO no repositório, já ordenados por dataPublicacao desc
        Page<Artigo> artigos =
                artigoRepository.findByStatusOrderByDataPublicacaoDesc(StatusArtigo.PUBLICADO, pageable);

        // Converte cada entidade Artigo da página para o DTO ArtigoResponse
        Page<ArtigoResponse> response = artigos.map(this::converterParaDTO);

        // Retorna a página de ArtigoResponse com status 200 (OK)
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint público para buscar um artigo específico por ID.
     * Só retorna se o artigo estiver PUBLICADO. Também incrementa o contador de visualizações.
     */
    @Operation(
            summary = "Buscar artigo por ID",
            description = "Obtém detalhes de um artigo específico e contabiliza visualização"
    )
    @GetMapping("/{id}") // GET /api/public/artigos/{id}
    public ResponseEntity<ArtigoResponse> buscarPorId(@PathVariable Long id) {
        // Busca o artigo pelo ID; Optional<Artigo>
        return artigoRepository.findById(id)
                // Filtra para garantir que o artigo está com status PUBLICADO
                .filter(artigo -> artigo.getStatus() == StatusArtigo.PUBLICADO)
                // Se passou no filtro, mapeia para incrementar visualização e converter para DTO
                .map(artigo -> {
                    // Chama o service para incrementar o contador de visualizações
                    artigoService.incrVisualizacao(artigo);
                    // Converte a entidade Artigo em ArtigoResponse
                    return converterParaDTO(artigo);
                })
                // Encapsula o DTO em ResponseEntity.ok(...)
                .map(ResponseEntity::ok)
                // Se não encontrar ou não for PUBLICADO, retorna 404 (NOT FOUND)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint público para listar artigos em destaque.
     * Normalmente são artigos marcados com uma flag "destaque".
     */
    @Operation(
            summary = "Artigos em destaque",
            description = "Lista artigos marcados como destaque"
    )
    @GetMapping("/destaques") // GET /api/public/artigos/destaques
    public ResponseEntity<Page<ArtigoResponse>> listarDestaques(
            @RequestParam(defaultValue = "0") int page, // Página atual
            @RequestParam(defaultValue = "5") int size  // Tamanho da página (default 5)
    ) {
        // Pageable simples (sem ordenação explícita, depende da query no repositório)
        Pageable pageable = PageRequest.of(page, size);

        // Busca os artigos em destaque no repositório (query customizada)
        Page<Artigo> artigos = artigoRepository.findArtigosEmDestaque(pageable);

        // Converte a página de entidades Artigo para a página de DTOs ArtigoResponse
        Page<ArtigoResponse> response = artigos.map(this::converterParaDTO);

        // Retorna a página de artigos em destaque
        return ResponseEntity.ok(response);
    }

    /**
     * Método utilitário privado para converter a entidade Artigo em ArtigoResponse (DTO).
     * Evita expor diretamente a entidade para o frontend.
     */
    private ArtigoResponse converterParaDTO(Artigo artigo) {
        return ArtigoResponse.builder()
                // ID do artigo
                .id(artigo.getId())
                // Título da notícia
                .titulo(artigo.getTitulo())
                // Resumo/descrição curta
                .resumo(artigo.getResumo())
                // Conteúdo completo
                .conteudo(artigo.getConteudo())
                // Status atual do artigo
                .status(artigo.getStatus())
                // URL ou caminho da imagem de capa
                .imagemCapa(artigo.getImagemCapa())
                // Número de visualizações
                .visualizacoes(artigo.getVisualizacoes())
                // Quantidade de reações "gostei"
                .gostei(artigo.getGostei())
                // Quantidade de reações "neutro"
                .neutro(artigo.getNeutro())
                // Quantidade de reações "não gostei"
                .naoGostei(artigo.getNaoGostei())
                // Flag indicando se é destaque
                .destaque(artigo.getDestaque())
                // Data/hora em que foi publicado
                .dataPublicacao(artigo.getDataPublicacao())
                // Data/hora de criação do registro
                .dataCriacao(artigo.getDataCriacao())
                // Nome do autor, se existir autor associado
                .autorNome(artigo.getAutor() != null ? artigo.getAutor().getNome() : null)
                // Nome da categoria, se existir categoria associada
                .categoriaNome(artigo.getCategoria() != null ? artigo.getCategoria().getNome() : null)
                // ID da categoria, se existir
                .categoriaId(artigo.getCategoria() != null ? artigo.getCategoria().getId() : null)
                .build(); // Constrói o objeto imutável ArtigoResponse
    }
}
