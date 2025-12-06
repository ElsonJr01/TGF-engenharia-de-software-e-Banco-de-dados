package servicos_tecnicos;

import dominio.entidades.Artigo;
import dominio.entidades.Categoria;
import dominio.entidades.Usuario;
import dominio.enums.StatusArtigo;
import dominio.dto.request.ArtigoRequestDTO;
import dominio.dto.ArtigoResponse;
import dominio.exception.ResourceNotFoundException;
import lib.repository.ArtigoRepository;
import lib.repository.CategoriaRepository;
import lib.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Camada de serviço responsável pelas regras de negócio relacionadas a Artigos.
 * Faz a ponte entre controllers e repositórios.
 */
@Service
@RequiredArgsConstructor // gera construtor com os campos final
@Slf4j // habilita logging (log.info, log.warn, etc.)
public class ArtigoService {

    // Repositório JPA para acesso à tabela de artigos
    private final ArtigoRepository artigoRepository;
    // Repositório para usuários (autores)
    private final UsuarioRepository usuarioRepository;
    // Repositório para categorias de artigos
    private final CategoriaRepository categoriaRepository;

    // ====== CRIAR ======

    /**
     * Cria um novo artigo a partir de um DTO de requisição.
     * Define autor, categoria, status inicial e datas.
     */
    @Transactional
    public ArtigoResponse criarArtigo(ArtigoRequestDTO dto) {
        log.info("📰 Criando novo artigo: {}", dto.getTitulo());

        // Validação simples: categoria obrigatória
        if (dto.getCategoriaId() == null) {
            throw new RuntimeException("CategoriaId não pode ser nulo");
        }

        // Busca a categoria informada; se não existir, lança exceção
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() ->
                        new RuntimeException("Categoria não encontrada ID: " + dto.getCategoriaId())
                );

        // Recupera o contexto de autenticação atual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario autor;

        // Se há usuário autenticado (e não é anonymousUser), usa como autor
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String email = auth.getName();
            autor = usuarioRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException("Usuário não encontrado: " + email)
                    );
        } else {
            // Caso contrário, usa um usuário admin padrão
            autor = usuarioRepository.findByEmail("admin@theclub.com")
                    .orElseThrow(() ->
                            new RuntimeException("Usuário admin não encontrado")
                    );
        }

        // Monta a entidade Artigo com base no DTO
        Artigo artigo = new Artigo();
        artigo.setTitulo(dto.getTitulo().trim());
        artigo.setResumo(dto.getResumo() != null ? dto.getResumo().trim() : "");
        artigo.setConteudo(dto.getConteudo().trim());
        artigo.setImagemCapa(dto.getImagemCapa());
        // Se status não vier, padrão RASCUNHO
        artigo.setStatus(dto.getStatus() != null ? dto.getStatus() : StatusArtigo.RASCUNHO);
        artigo.setDestaque(false);
        artigo.setVisualizacoes(0);
        artigo.setGostei(0);
        artigo.setNeutro(0);
        artigo.setNaoGostei(0);
        artigo.setAutor(autor);
        artigo.setCategoria(categoria);
        artigo.setDataCriacao(LocalDateTime.now());
        artigo.setDataAtualizacao(LocalDateTime.now());

        // Se já for criado como PUBLICADO, registra data de publicação
        if (artigo.getStatus() == StatusArtigo.PUBLICADO) {
            artigo.setDataPublicacao(LocalDateTime.now());
        }

        // Persiste no banco
        artigoRepository.save(artigo);
        log.info("✅ Artigo criado ID: {}", artigo.getId());

        // Converte entidade para DTO de resposta
        return toDTO(artigo);
    }

    // ====== CONTABILIZAÇÃO DE VISUALIZAÇÃO ======

    /**
     * Incrementa o contador de visualizações de um artigo e salva.
     */
    @Transactional
    public void incrVisualizacao(Artigo artigo) {
        // Se for null, considera 0 antes de somar
        artigo.setVisualizacoes(
                (artigo.getVisualizacoes() == null ? 0 : artigo.getVisualizacoes()) + 1
        );
        artigoRepository.save(artigo);
    }

    // ====== LISTAR TODOS PARA ESTATÍSTICA DO DASH ======

    /**
     * Retorna todos os artigos convertidos em DTO,
     * normalmente usado para estatísticas no dashboard.
     */
    @Transactional(readOnly = true)
    public List<ArtigoResponse> listarTodosComEstatisticas() {
        return artigoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ====== BUSCAR ======

    /**
     * Busca um artigo por ID e o converte em DTO.
     * Se não encontrar, lança ResourceNotFoundException.
     */
    @Transactional(readOnly = true)
    public ArtigoResponse buscarPorId(Long id) {
        Artigo artigo = artigoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Artigo", "id", id)
                );
        return toDTO(artigo);
    }

    /**
     * Lista artigos publicados, ordenados por data de publicação desc,
     * com suporte a paginação.
     */
    @Transactional(readOnly = true)
    public Page<ArtigoResponse> listarPublicados(Pageable pageable) {
        return artigoRepository
                .findByStatusOrderByDataPublicacaoDesc(StatusArtigo.PUBLICADO, pageable)
                .map(this::toDTO);
    }

    /**
     * Lista artigos por um determinado status (RASCUNHO, PUBLICADO, etc.)
     * com paginação.
     */
    @Transactional(readOnly = true)
    public Page<ArtigoResponse> listarPorStatus(StatusArtigo status, Pageable pageable) {
        return artigoRepository
                .findByStatus(status, pageable)
                .map(this::toDTO);
    }

    /**
     * Busca artigos usando múltiplos filtros opcionais
     * (status, categoria, autor, parte do título), com paginação.
     */
    @Transactional(readOnly = true)
    public Page<ArtigoResponse> buscarPorFiltros(
            StatusArtigo status,
            Long categoriaId,
            Long autorId,
            String titulo,
            Pageable pageable
    ) {
        return artigoRepository
                .buscarArtigosPorFiltros(status, categoriaId, autorId, titulo, pageable)
                .map(this::toDTO);
    }

    /**
     * Lista artigos por um autor específico, com paginação.
     */
    @Transactional(readOnly = true)
    public Page<ArtigoResponse> listarPorAutor(Long autorId, Pageable pageable) {
        // Garante que o autor exista
        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário", "id", autorId)
                );

        return artigoRepository
                .findByAutor(autor, pageable)
                .map(this::toDTO);
    }

    // ====== ATUALIZAR ======

    /**
     * Atualiza os dados de um artigo existente a partir de um DTO.
     */
    @Transactional
    public ArtigoResponse atualizarArtigo(Long id, ArtigoRequestDTO dto) {
        // Busca o artigo
        Artigo artigo = artigoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Artigo", "id", id)
                );

        // Se a categoria mudou, busca a nova e troca
        if (!artigo.getCategoria().getId().equals(dto.getCategoriaId())) {
            Categoria novaCategoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Categoria", "id", dto.getCategoriaId())
                    );
            artigo.setCategoria(novaCategoria);
        }

        // Atualiza campos de texto
        artigo.setTitulo(dto.getTitulo().trim());
        artigo.setResumo(dto.getResumo() != null ? dto.getResumo().trim() : "");
        artigo.setConteudo(dto.getConteudo().trim());
        artigo.setImagemCapa(dto.getImagemCapa());

        // Atualiza status se veio no DTO
        if (dto.getStatus() != null) artigo.setStatus(dto.getStatus());

        // Atualiza timestamp de alteração
        artigo.setDataAtualizacao(LocalDateTime.now());

        artigoRepository.save(artigo);
        log.info("✏️ Artigo {} atualizado", id);

        return toDTO(artigo);
    }

    /**
     * Marca o artigo como PUBLICADO e define a data de publicação.
     */
    @Transactional
    public ArtigoResponse publicarArtigo(Long id) {
        Artigo artigo = artigoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Artigo", "id", id)
                );

        artigo.setStatus(StatusArtigo.PUBLICADO);
        artigo.setDataPublicacao(LocalDateTime.now());
        artigoRepository.save(artigo);

        log.info("🚀 Artigo publicado ID: {}", id);
        return toDTO(artigo);
    }

    /**
     * Altera o status do artigo para ARQUIVADO.
     */
    @Transactional
    public void arquivarArtigo(Long id) {
        Artigo artigo = artigoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Artigo", "id", id)
                );
        artigo.setStatus(StatusArtigo.ARQUIVADO);
        artigoRepository.save(artigo);

        log.warn("📦 Artigo arquivado ID: {}", id);
    }

    /**
     * Exclui definitivamente um artigo do banco.
     */
    @Transactional
    public void deletarArtigo(Long id) {
        Artigo artigo = artigoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Artigo", "id", id)
                );
        artigoRepository.delete(artigo);

        log.warn("🗑️ Artigo excluído ID: {}", id);
    }

    // ====== CONVERSÃO ======

    /**
     * Converte a entidade Artigo para o DTO de resposta ArtigoResponse.
     * Centraliza o mapeamento para evitar repetição nos métodos.
     */
    private ArtigoResponse toDTO(Artigo artigo) {
        return ArtigoResponse.builder()
                .id(artigo.getId())
                .titulo(artigo.getTitulo())
                .resumo(artigo.getResumo())
                .conteudo(artigo.getConteudo())
                .status(artigo.getStatus())
                .imagemCapa(artigo.getImagemCapa())
                .visualizacoes(artigo.getVisualizacoes())
                .gostei(artigo.getGostei())
                .neutro(artigo.getNeutro())
                .naoGostei(artigo.getNaoGostei())
                .destaque(artigo.getDestaque())
                .dataPublicacao(artigo.getDataPublicacao())
                .autorNome(artigo.getAutor().getNome())
                .autorId(artigo.getAutor().getId())
                .categoriaNome(artigo.getCategoria().getNome())
                .categoriaId(artigo.getCategoria().getId())
                .dataCriacao(artigo.getDataCriacao())
                .dataAtualizacao(artigo.getDataAtualizacao())
                .build();
    }
}
