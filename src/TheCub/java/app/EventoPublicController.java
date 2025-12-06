package app; // Pacote onde o controller público de eventos está localizado

// Entidade JPA que representa um evento cultural no banco
import dominio.entidades.Cultura;
// DTO usado para expor os dados do evento para o frontend
import dominio.dto.response.CulturaResponseDTO;
// Repositório para acessar eventos no banco
import lib.repository.CulturaRepository;

// Swagger/OpenAPI para documentação
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// Lombok: gera construtor com campos final
import lombok.RequiredArgsConstructor;
// Spring Data para paginação
import org.springframework.data.domain.*;
// Construção de respostas HTTP
import org.springframework.http.ResponseEntity;
// Anotações REST
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST público para consulta de eventos.
 * Não exige autenticação, usado pelo site/app para exibir eventos.
 */
@RestController // Indica que a classe expõe endpoints REST (retornam JSON)
@RequestMapping("/api/public/eventos") // Prefixo base das rotas públicas de eventos
@RequiredArgsConstructor // Lombok: gera construtor com o campo final eventoRepository
@Tag(
        name = "Eventos Públicos",
        description = "Endpoints públicos para consulta de eventos"
) // Grupo no Swagger
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Libera CORS para esses frontends
public class EventoPublicController {

    // Repositório responsável por consultar eventos (Cultura) no banco
    private final CulturaRepository eventoRepository;

    /**
     * Lista próximos eventos ativos (data futura), de forma paginada.
     */
    @Operation(
            summary = "Listar próximos eventos",
            description = "Lista eventos futuros ativos"
    )
    @GetMapping // GET /api/public/eventos
    public ResponseEntity<Page<CulturaResponseDTO>> listarEventos(
            @RequestParam(defaultValue = "0") int page, // Número da página
            @RequestParam(defaultValue = "10") int size // Tamanho da página
    ) {
        // Cria o objeto Pageable (sem ordenação explícita, delega à query)
        Pageable pageable = PageRequest.of(page, size);

        // Busca eventos futuros (dataEvento > agora) no repositório
        Page<Cultura> eventos =
                eventoRepository.findProximosEventos(LocalDateTime.now(), pageable);

        // Converte cada Cultura da Page para CulturaResponseDTO
        Page<CulturaResponseDTO> response = eventos.map(this::converterParaDTO);

        // Retorna a página de DTOs com status 200 (OK)
        return ResponseEntity.ok(response);
    }

    /**
     * Busca um evento específico por ID, apenas se estiver ativo.
     */
    @Operation(
            summary = "Buscar evento por ID",
            description = "Obtém detalhes de um evento específico"
    )
    @GetMapping("/{id}") // GET /api/public/eventos/{id}
    public ResponseEntity<CulturaResponseDTO> buscarPorId(@PathVariable Long id) {
        return eventoRepository.findById(id) // Optional<Cultura>
                // Filtra para garantir que o evento está ativo
                .filter(Cultura::getAtivo)
                // Converte a entidade para DTO
                .map(this::converterParaDTO)
                // Encapsula em ResponseEntity.ok(...)
                .map(ResponseEntity::ok)
                // Se não encontrar ou estiver inativo, retorna 404
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista eventos em destaque, em geral eventos que vão ocorrer nos próximos 7 dias.
     */
    @Operation(
            summary = "Eventos em destaque",
            description = "Lista eventos dos próximos 7 dias"
    )
    @GetMapping("/destaques") // GET /api/public/eventos/destaques
    public ResponseEntity<List<CulturaResponseDTO>> listarDestaques() {
        // Intervalo de datas: agora até 7 dias depois
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime semanaDepois = agora.plusDays(7);

        // Busca no repositório os eventos dentro desse intervalo (lógica de destaque)
        List<Cultura> culturas =
                eventoRepository.findEventosEmDestaque(agora, semanaDepois);

        // Converte lista de entidades Cultura para lista de CulturaResponseDTO
        List<CulturaResponseDTO> response = culturas.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Método utilitário para converter entidade Cultura em CulturaResponseDTO.
     * Evita expor diretamente a entidade JPA ao frontend.
     */
    private CulturaResponseDTO converterParaDTO(Cultura cultura) {
        return CulturaResponseDTO.builder()
                .id(cultura.getId())                        // ID do evento
                .titulo(cultura.getTitulo())                // Título do evento
                .descricao(cultura.getDescricao())          // Descrição detalhada
                .dataEvento(cultura.getDataEvento())        // Data e hora do evento
                .localEvento(cultura.getLocalEvento())      // Local onde acontecerá
                .imagem(cultura.getImagem())                // URL/arquivo da imagem
                .linkInscricao(cultura.getLinkInscricao())  // Link para inscrição, se houver
                .ativo(cultura.getAtivo())                  // Flag se o evento está ativo
                // Nome do organizador, se existir vínculo com entidade organizador
                .organizadorNome(
                        cultura.getOrganizador() != null
                                ? cultura.getOrganizador().getNome()
                                : null
                )
                .build();
    }
}
