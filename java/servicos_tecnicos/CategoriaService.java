package servicos_tecnicos;

import dominio.entidades.Categoria;
import dominio.dto.request.CategoriaRequestDTO;
import dominio.dto.response.CategoriaResponseDTO;
import dominio.exception.BusinessException;
import dominio.exception.ResourceNotFoundException;
import lib.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelas regras de negócio relacionadas às categorias de artigos.
 * Centraliza criação, atualização, listagens, exclusão e estatísticas de categorias.
 */
@Service
@RequiredArgsConstructor // injeta o CategoriaRepository via construtor
@Slf4j // habilita logging com log.info, log.warn, log.error etc.
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // ====== CRIAR ======

    /**
     * Cria uma nova categoria a partir de um DTO de requisição.
     * Aplica validação de nome único (case-insensitive) e define valores padrão.
     */
    @Transactional
    public CategoriaResponseDTO criarCategoria(CategoriaRequestDTO dto) {
        String nome = dto.getNome().trim();

        log.info("Criando categoria: {}", nome);

        // Verifica se já existe outra categoria com o mesmo nome (ignorando maiúsculas/minúsculas)
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new BusinessException("Já existe uma categoria com esse nome: " + nome);
        }

        // Monta a entidade Categoria usando o padrão Builder
        Categoria categoria = Categoria.builder()
                .nome(nome)
                .descricao(dto.getDescricao())
                // Se a cor não vier, usa um padrão (#007bff)
                .cor(dto.getCor() != null ? dto.getCor() : "#007bff")
                .icone(dto.getIcone())
                .ativa(true) // categoria sempre nasce ativa
                .build();

        // Persiste no banco
        categoriaRepository.save(categoria);
        log.info(" Categoria criada com sucesso. ID: {}", categoria.getId());

        // Converte a entidade para DTO de resposta
        return toResponse(categoria);
    }

    // ====== BUSCAR ======

    /**
     * Busca categoria por ID e retorna em formato DTO.
     * Se não encontrar, dispara ResourceNotFoundException.
     */
    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorId(Long id) {
        Categoria categoria = obterCategoria(id);
        return toResponse(categoria);
    }

    /**
     * Busca categoria pelo nome (ignorando maiúsculas/minúsculas).
     */
    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorNome(String nome) {
        Categoria categoria = categoriaRepository.findByNomeIgnoreCase(nome.trim())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Categoria", "nome", nome)
                );
        return toResponse(categoria);
    }

    // ====== LISTAR ======

    /**
     * Lista todas as categorias paginadas.
     */
    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> listarTodas(Pageable pageable) {
        return categoriaRepository
                .findAll(pageable)
                .map(this::toResponse);
    }

    /**
     * Lista somente as categorias ativas, ordenadas alfabeticamente.
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarAtivas() {
        return categoriaRepository
                .findByAtivaTrueOrderByNomeAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista categorias que possuem pelo menos um artigo publicado.
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarComArtigosPublicados() {
        return categoriaRepository
                .findCategoriasComArtigosPublicados()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ====== LISTAR MAIS POPULARES ======

    /**
     * Lista categorias mais populares (em geral ordenadas por quantidade de artigos),
     * com suporte a paginação.
     */
    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> listarMaisPopulares(Pageable pageable) {
        Page<Categoria> categorias = categoriaRepository.findCategoriasMaisPopulares(pageable);
        return categorias.map(this::toResponse);
    }

    /**
     * Lista categorias ordenadas pela quantidade de artigos,
     * retornando tudo em uma lista (sem paginação).
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarOrdenadasPorQuantidadeArtigos() {
        return categoriaRepository
                .findCategoriasOrdenadaPorQuantidadeArtigos()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ====== ATUALIZAR ======

    /**
     * Atualiza os dados básicos de uma categoria existente.
     * Valida se o novo nome não entra em conflito com outra categoria.
     */
    @Transactional
    public CategoriaResponseDTO atualizarCategoria(Long id, CategoriaRequestDTO dto) {
        // Busca categoria ou lança 404
        Categoria categoria = obterCategoria(id);

        String novoNome = dto.getNome().trim();

        // Se o nome foi alterado, verifica duplicidade
        if (!categoria.getNome().equalsIgnoreCase(novoNome)
                && categoriaRepository.existsByNomeIgnoreCase(novoNome)) {
            throw new BusinessException("Já existe uma categoria com esse nome: " + novoNome);
        }

        // Atualiza campos
        categoria.setNome(novoNome);
        categoria.setDescricao(dto.getDescricao());
        // Se a nova cor vier nula, mantém a cor atual
        categoria.setCor(dto.getCor() != null ? dto.getCor() : categoria.getCor());
        categoria.setIcone(dto.getIcone());

        categoriaRepository.save(categoria);

        log.info(" Categoria ID {} atualizada com sucesso", id);
        return toResponse(categoria);
    }

    // ====== STATUS ======

    /**
     * Altera o status de ativa/inativa da categoria.
     */
    @Transactional
    public void alterarStatus(Long id, Boolean ativa) {
        Categoria categoria = obterCategoria(id);
        categoria.setAtiva(ativa);
        categoriaRepository.save(categoria);

        log.info(" Categoria ID {} marcada como {}", id, ativa ? "Ativa" : "Inativa");
    }

    // ====== EXCLUSÃO ======

    /**
     * "Exclui" uma categoria de forma lógica (soft delete).
     * Apenas desativa, desde que não haja artigos publicados vinculados.
     */
    @Transactional
    public void deletarCategoria(Long id) {
        Categoria categoria = obterCategoria(id);

        // Consulta quantos artigos publicados estão vinculados a essa categoria
        Long artigos = categoriaRepository.contarArtigosPublicadosPorCategoria(id);

        // Se houver artigos, bloqueia a exclusão lógica
        if (artigos != null && artigos > 0) {
            throw new BusinessException(
                    "Não é possível excluir: há " + artigos + " artigo(s) vinculados à categoria."
            );
        }

        // Marca como inativa (soft delete)
        categoria.setAtiva(false);
        categoriaRepository.save(categoria);

        log.warn(" Categoria ID {} desativada com sucesso (soft delete)", id);
    }

    /**
     * Exclusão permanente da categoria.
     * Só é permitido se não houver artigos vinculados (em qualquer status).
     */
    @Transactional
    public void deletarCategoriaPermanente(Long id) {
        Categoria categoria = obterCategoria(id);

        // Verifica se ainda existem artigos vinculados (lista não vazia)
        if (!categoria.getArtigos().isEmpty()) {
            throw new BusinessException(
                    "Não é possível excluir permanentemente: há artigos vinculados."
            );
        }

        categoriaRepository.delete(categoria);
        log.error(" Categoria ID {} removida permanentemente", id);
    }

    // ====== REATIVAR ======

    /**
     * Reativa uma categoria que está inativa.
     * Se já estiver ativa, lança BusinessException.
     */
    @Transactional
    public CategoriaResponseDTO reativarCategoria(Long id) {
        Categoria categoria = obterCategoria(id);

        // Se já está ativa, não faz sentido reativar
        if (Boolean.TRUE.equals(categoria.getAtiva())) {
            throw new BusinessException("Categoria já está ativa.");
        }

        categoria.setAtiva(true);
        categoriaRepository.save(categoria);

        log.info(" Categoria ID {} reativada com sucesso", id);
        return toResponse(categoria);
    }

    // ====== ESTATÍSTICAS ======

    /**
     * Retorna uma categoria em formato DTO incluindo estatísticas,
     * como total de artigos publicados.
     */
    @Transactional(readOnly = true)
    public CategoriaResponseDTO obterEstatisticas(Long id) {
        Categoria categoria = obterCategoria(id);
        CategoriaResponseDTO dto = toResponse(categoria);
        dto.setTotalArtigos(categoriaRepository.contarArtigosPublicadosPorCategoria(id));
        return dto;
    }

    /**
     * Conta quantas categorias estão ativas no sistema.
     */
    @Transactional(readOnly = true)
    public Long contarCategoriasAtivas() {
        Long total = categoriaRepository.countByAtivaTrue();
        log.info("Total de categorias ativas: {}", total);
        return total;
    }

    // ====== AUXILIAR ======

    /**
     * Busca a categoria por ID ou lança ResourceNotFoundException.
     * Método auxiliar para reaproveitar a lógica.
     */
    private Categoria obterCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Categoria", "id", id)
                );
    }

    /**
     * Converte a entidade Categoria para o DTO CategoriaResponseDTO.
     * Inclui o total de artigos publicados calculado pela própria entidade.
     */
    private CategoriaResponseDTO toResponse(Categoria categoria) {
        Long totalArtigos = categoria.contarArtigosPublicados();

        return CategoriaResponseDTO.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .descricao(categoria.getDescricao())
                .cor(categoria.getCor())
                .icone(categoria.getIcone())
                .ativa(categoria.getAtiva())
                .totalArtigos(totalArtigos)
                .build();
    }
}
