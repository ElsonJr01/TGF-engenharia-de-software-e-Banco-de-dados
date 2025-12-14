//package servicos_tecnicos;
//
//import dominio.entidades.Artigo;
//import dominio.entidades.Comentario;
//import dominio.entidades.Usuario;
//import dominio.enums.StatusArtigo;
//import dominio.dto.request.ComentarioRequestDTO;
//import dominio.dto.response.ComentarioResponseDTO;
//import dominio.exception.BusinessException;
//import dominio.exception.ResourceNotFoundException;
//import lib.repository.ArtigoRepository;
//import lib.repository.ComentarioRepository;
//import lib.repository.UsuarioRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ComentarioService {
//
//    private final ComentarioRepository comentarioRepository;
//    private final ArtigoRepository artigoRepository;
//    private final UsuarioRepository usuarioRepository;
//
//    // ====== CRIAR ======
//    @Transactional
//    public ComentarioResponseDTO criarComentario(ComentarioRequestDTO dto) {
//        log.info("🗨️ Criando novo comentário para o artigo ID: {}", dto.getArtigoId());
//
//        Artigo artigo = artigoRepository.findById(dto.getArtigoId())
//                .orElseThrow(() -> new ResourceNotFoundException("Artigo", "id", dto.getArtigoId()));
//
//        if (artigo.getStatus() != StatusArtigo.PUBLICADO) {
//            throw new BusinessException("Não é possível comentar em artigos não publicados.");
//        }
//
//        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
//                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", dto.getUsuarioId()));
//
//        if (Boolean.FALSE.equals(usuario.getAtivo())) {
//            throw new BusinessException("Usuário inativo não pode enviar comentários.");
//        }
//
//        validarConteudo(dto.getComentario());
//
//        Comentario comentario = Comentario.builder()
//                .comentario(dto.getComentario().trim())
//                .aprovado(false)
//                .dataComentario(LocalDateTime.now())
//                .usuario(usuario)
//                .artigo(artigo)
//                .build();
//
//        comentarioRepository.save(comentario);
//        log.info("✅ Comentário criado. ID: {} Aguardando moderação.", comentario.getId());
//
//        return convertToDTO(comentario);
//    }
//
//    // ====== BUSCAR ======
//    @Transactional(readOnly = true)
//    public ComentarioResponseDTO buscarPorId(Long id) {
//        Comentario comentario = comentarioRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Comentário", "id", id));
//        return convertToDTO(comentario);
//    }
//
//    @Transactional(readOnly = true)
//    public Page<ComentarioResponseDTO> listarTodos(Pageable pageable) {
//        return comentarioRepository.findAll(pageable).map(this::convertToDTO);
//    }
//
//    @Transactional(readOnly = true)
//    public List<ComentarioResponseDTO> listarComentariosDoArtigo(Long artigoId) {
//        if (!artigoRepository.existsById(artigoId)) {
//            throw new ResourceNotFoundException("Artigo", "id", artigoId);
//        }
//        return comentarioRepository.findByArtigoIdAndAprovadoTrueOrderByDataComentarioDesc(artigoId)
//                .stream().map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public Page<ComentarioResponseDTO> listarPendentes(Pageable pageable) {
//        return comentarioRepository.findByAprovadoFalse(pageable).map(this::convertToDTO);
//    }
//
//    @Transactional(readOnly = true)
//    public Page<ComentarioResponseDTO> listarAprovados(Pageable pageable) {
//        return comentarioRepository.findByAprovadoTrue(pageable).map(this::convertToDTO);
//    }
//
//    // ====== APROVAR / REPROVAR ======
//    @Transactional
//    public ComentarioResponseDTO aprovarComentario(Long id) {
//        Comentario comentario = comentarioRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Comentário", "id", id));
//
//        if (Boolean.TRUE.equals(comentario.getAprovado())) {
//            throw new BusinessException("Comentário já está aprovado.");
//        }
//
//        comentario.aprovar();
//        comentarioRepository.save(comentario);
//
//        log.info("🟢 Comentário aprovado: {}", id);
//        return convertToDTO(comentario);
//    }
//
//    @Transactional
//    public void reprovarComentario(Long id) {
//        Comentario comentario = comentarioRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Comentário", "id", id));
//        comentarioRepository.delete(comentario);
//        log.warn("🗑️ Comentário reprovado e removido: {}", id);
//    }
//
//    @Transactional
//    public int aprovarEmLote(List<Long> ids) {
//        List<Comentario> comentarios = comentarioRepository.findAllById(ids);
//        comentarios.forEach(Comentario::aprovar);
//        comentarioRepository.saveAll(comentarios);
//        log.info("🟢 {} comentários aprovados em lote.", comentarios.size());
//        return comentarios.size();
//    }
//
//    @Transactional
//    public int reprovarEmLote(List<Long> ids) {
//        List<Comentario> comentarios = comentarioRepository.findAllById(ids);
//        comentarioRepository.deleteAll(comentarios);
//        log.warn("🗑️ {} comentários reprovados e deletados.", comentarios.size());
//        return comentarios.size();
//    }
//
//    // ====== ATUALIZAR ======
//    @Transactional
//    public ComentarioResponseDTO atualizarComentario(Long id, String texto, Long usuarioId) {
//        Comentario comentario = comentarioRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Comentário", "id", id));
//
//        if (!comentario.getUsuario().getId().equals(usuarioId)) {
//            throw new BusinessException("Usuário sem permissão para editar este comentário.");
//        }
//
//        validarConteudo(texto);
//
//        comentario.setComentario(texto.trim());
//        comentario.setAprovado(false);
//        comentarioRepository.save(comentario);
//
//        log.info("✏️ Comentário ID {} atualizado (aguardando moderação)", id);
//        return convertToDTO(comentario);
//    }
//
//    // ====== EXCLUSÃO ======
//    @Transactional
//    public void deletar(Long id, Long usuarioId, boolean isAdmin) {
//        Comentario comentario = comentarioRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Comentário", "id", id));
//
//        if (!isAdmin && !comentario.getUsuario().getId().equals(usuarioId)) {
//            throw new BusinessException("Usuário não possui permissão para excluir este comentário.");
//        }
//
//        comentarioRepository.delete(comentario);
//        log.warn("🗑️ Comentário removido (ID: {}) pelo usuário ID {}", id, usuarioId);
//    }
//
//    // ====== ESTATÍSTICAS ======
//    @Transactional(readOnly = true)
//    public Long contarPorArtigo(Long artigoId) {
//        return comentarioRepository.countByArtigoId(artigoId);
//    }
//
//    @Transactional(readOnly = true)
//    public Long contarComentariosDoArtigo(Long artigoId) {
//        log.info("📊 Contando comentários aprovados do artigo ID: {}", artigoId);
//        return comentarioRepository.countByArtigoIdAndAprovadoTrue(artigoId);
//    }
//
//    @Transactional(readOnly = true)
//    public Long contarPendentes() {
//        return comentarioRepository.countByAprovadoFalse();
//    }
//
//    @Transactional(readOnly = true)
//    public ComentarioEstatisticas obterEstatisticas() {
//        long total = comentarioRepository.count();
//        long aprovados = comentarioRepository.countByAprovadoTrue();
//        long pendentes = comentarioRepository.countByAprovadoFalse();
//        log.info("📈 Estatísticas — Total: {}, Aprovados: {}, Pendentes: {}", total, aprovados, pendentes);
//        return new ComentarioEstatisticas(total, aprovados, pendentes);
//    }
//
//    @Transactional(readOnly = true)
//    public Page<ComentarioResponseDTO> buscarComFiltros(Long artigoId, Long usuarioId, Boolean aprovado, Pageable pageable) {
//        return comentarioRepository.findByFiltros(artigoId, usuarioId, aprovado, pageable)
//                .map(this::convertToDTO);
//    }
//
//    // ====== VALIDAÇÃO ======
//    private void validarConteudo(String texto) {
//        if (texto == null || texto.trim().isEmpty()) {
//            throw new BusinessException("O comentário não pode estar vazio.");
//        }
//        if (texto.trim().length() < 3 || texto.trim().length() > 1000) {
//            throw new BusinessException("O comentário deve ter entre 3 e 1000 caracteres.");
//        }
//    }
//
//    // ====== CONVERSÃO DTO ======
//    private ComentarioResponseDTO convertToDTO(Comentario c) {
//        return ComentarioResponseDTO.builder()
//                .id(c.getId())
//                .comentario(c.getComentario())
//                .aprovado(c.getAprovado())
//                .dataComentario(c.getDataComentario())
//                .usuarioId(c.getUsuario().getId())
//                .usuarioNome(c.getUsuario().getNome())
//                .artigoId(c.getArtigo().getId())
//                .artigoTitulo(c.getArtigo().getTitulo())
//                .build();
//    }
//
//    // ====== RECORD PARA DASHBOARD ======
//    public record ComentarioEstatisticas(long total, long aprovados, long pendentes) {}
//}
