package servicos_tecnicos;

import dominio.entidades.Usuario;
import dominio.enums.TipoUsuario;
import dominio.dto.request.UsuarioRequestDTO;
import dominio.dto.response.UsuarioResponseDTO;
import dominio.dto.RegisterRequest;
import dominio.exception.BusinessException;
import dominio.exception.ResourceNotFoundException;
import lib.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelas regras de negócio relacionadas a Usuários.
 * Centraliza cadastro público, gestão de perfis, filtros e soft delete.
 */
@Service
@RequiredArgsConstructor // injeta dependências final via construtor
@Slf4j // habilita logging (log.info, log.warn, log.error)
public class UsuarioService {

    // Repositório JPA de usuários
    private final UsuarioRepository usuarioRepository;

    // Encoder de senha (definido em SecurityConfig)
    private final PasswordEncoder passwordEncoder;

    /**
     * Verifica se já existe um usuário com o e-mail informado.
     * Normaliza o e-mail (trim + lowercase) antes da consulta.
     */
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email.trim().toLowerCase());
    }

    // ====== CADASTRO PÚBLICO (LEITOR) ======

    /**
     * Cadastro público de usuário (perfil padrão: LEITOR).
     * Usado em fluxos abertos, como "criar conta" no site.
     */
    @Transactional
    public Usuario criarNovoUsuario(RegisterRequest req) {
        log.info("Cadastro público de usuário: {}", req.getEmail());

        // Regra de negócio: e-mail deve ser único
        if (existsByEmail(req.getEmail())) {
            throw new BusinessException("O e-mail já está cadastrado: " + req.getEmail());
        }

        // Monta entidade Usuario com dados do request
        Usuario usuario = Usuario.builder()
                .nome(req.getNome().trim())
                .email(req.getEmail().trim().toLowerCase())
                .senha(passwordEncoder.encode(req.getSenha())) // senha codificada
                .tipo(TipoUsuario.LEITOR)                     // papel padrão
                .ativo(true)                                  // nasce ativo
                .build();

        // Salva e retorna a entidade (para uso interno)
        return usuarioRepository.save(usuario);
    }

    // ====== CADASTRO ADMIN ======

    /**
     * Cadastro de usuário via painel/admin.
     * Permite definir tipo (ADMIN, EDITOR, REDATOR, LEITOR).
     */
    @Transactional
    public UsuarioResponseDTO criarUsuario(UsuarioRequestDTO dto) {
        log.info("🧑 Criando novo usuário com e-mail: {}", dto.getEmail());

        String emailNormalizado = dto.getEmail().trim().toLowerCase();

        // Valida e-mail único
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new BusinessException("O e-mail já está cadastrado: " + dto.getEmail());
        }

        // Monta entidade Usuario
        Usuario usuario = Usuario.builder()
                .nome(dto.getNome().trim())
                .email(emailNormalizado)
                .senha(passwordEncoder.encode(dto.getSenha()))
                .tipo(dto.getTipo() != null ? dto.getTipo() : TipoUsuario.LEITOR)
                .foto(dto.getFoto())
                .bio(dto.getBio())
                .ativo(true)
                .build();

        usuarioRepository.save(usuario);
        log.info("✅ Usuário criado com sucesso. ID: {}", usuario.getId());

        return toResponse(usuario);
    }

    // ====== BUSCAS SIMPLES ======

    /**
     * Busca usuário por ID, retornando DTO.
     * Lança ResourceNotFoundException se não existir.
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário", "id", id)
                );
        return toResponse(usuario);
    }

    /**
     * Busca usuário por e-mail, retornando DTO.
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário", "email", email)
                );
        return toResponse(usuario);
    }

    /**
     * Versão que retorna a ENTIDADE Usuario.
     * Útil para AuthController / Security, onde precisa da entidade completa.
     */
    @Transactional(readOnly = true)
    public Usuario buscarPorEmailEntidade(String email) {
        return usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário", "email", email)
                );
    }

    // ====== LISTAGENS ======

    /**
     * Lista todos os usuários paginados.
     */
    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarTodos(Pageable pageable) {
        return usuarioRepository
                .findAll(pageable)
                .map(this::toResponse);
    }

    /**
     * Lista apenas usuários ativos (ativo = true).
     */
    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarAtivos(Pageable pageable) {
        return usuarioRepository
                .findByAtivoTrue(pageable)
                .map(this::toResponse);
    }

    /**
     * Lista usuários por tipo (ADMIN, EDITOR, REDATOR, LEITOR).
     */
    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarPorTipo(TipoUsuario tipo, Pageable pageable) {
        return usuarioRepository
                .findByTipo(tipo, pageable)
                .map(this::toResponse);
    }

    // ====== ATUALIZAR USUÁRIO ======

    /**
     * Atualiza dados de um usuário existente.
     * Valida e-mail único ao alterar.
     */
    @Transactional
    public UsuarioResponseDTO atualizarUsuario(Long id, UsuarioRequestDTO dto) {
        log.info("✏️ Atualizando usuário ID: {}", id);

        // Busca o usuário ou lança 404
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário", "id", id)
                );

        // Normaliza e-mail novo
        String novoEmail = dto.getEmail().trim().toLowerCase();

        // Se o e-mail foi alterado, verifica se já existe para outro usuário
        if (!usuario.getEmail().equalsIgnoreCase(novoEmail)
                && usuarioRepository.existsByEmail(novoEmail)) {
            throw new BusinessException("O e-mail já está cadastrado: " + novoEmail);
        }

        // Atualiza dados básicos
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(novoEmail);
        usuario.setFoto(dto.getFoto());
        usuario.setBio(dto.getBio());
        usuario.setTipo(dto.getTipo() != null ? dto.getTipo() : usuario.getTipo());

        // Atualiza senha apenas se enviada e não vazia
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        usuarioRepository.save(usuario);
        log.info("✅ Usuário ID {} atualizado com sucesso", id);

        return toResponse(usuario);
    }

    // ====== STATUS / SOFT DELETE ======

    /**
     * Altera o status ativo/inativo de um usuário.
     */
    @Transactional
    public void alterarStatus(Long id, Boolean ativo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário", "id", id)
                );

        usuario.setAtivo(ativo);
        usuarioRepository.save(usuario);

        log.info("⚙️ Status do usuário ID {} alterado para {}", id, ativo ? "ATIVO" : "INATIVO");
    }

    /**
     * Soft delete de usuário: apenas marca como inativo.
     * Se já estiver desativado, lança BusinessException.
     */
    @Transactional
    public void deletarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário", "id", id)
                );

        // Regra: não desativar duas vezes
        if (!usuario.getAtivo()) {
            throw new BusinessException("Usuário já está desativado.");
        }

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        log.warn("🗑️ Usuário ID {} desativado (soft delete)", id);
    }

    // ====== BUSCA COM FILTROS ======

    /**
     * Busca usuários com múltiplos filtros opcionais:
     * - nome (like)
     * - email (like)
     * - tipo (enum)
     * - ativo (true/false)
     *
     * Se nenhum filtro for informado, retorna todos paginados.
     */
    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> buscarComFiltros(
            String nome,
            String email,
            TipoUsuario tipo,
            Boolean ativo,
            Pageable pageable
    ) {
        log.info("🔍 Buscando usuários com filtros - Nome: {}, Email: {}, Tipo: {}, Ativo: {}",
                nome, email, tipo, ativo);

        // Se nenhum filtro foi informado, retorna o findAll padrão
        if (nome == null && email == null && tipo == null && ativo == null) {
            return usuarioRepository
                    .findAll(pageable)
                    .map(this::toResponse);
        }

        // Monta padrões de busca com like (%texto%)
        String nomeFiltro = nome != null ? "%" + nome.trim() + "%" : null;
        String emailFiltro = email != null ? "%" + email.trim().toLowerCase() + "%" : null;

        return usuarioRepository
                .findByFiltros(nomeFiltro, emailFiltro, tipo, ativo, pageable)
                .map(this::toResponse);
    }

    // ====== CONVERSÃO ENTIDADE -> DTO ======

    /**
     * Converte a entidade Usuario em UsuarioResponseDTO.
     * Inclui métricas: total de artigos e comentários do usuário.
     */
    private UsuarioResponseDTO toResponse(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .tipo(usuario.getTipo())
                .ativo(usuario.getAtivo())
                .foto(usuario.getFoto())
                .bio(usuario.getBio())
                .dataCriacao(usuario.getDataCriacao())
                // Evita NullPointer se a lista for nula
                .totalArtigos(usuario.getArtigos() != null
                        ? (long) usuario.getArtigos().size()
                        : 0L)
                .totalComentarios(usuario.getComentarios() != null
                        ? (long) usuario.getComentarios().size()
                        : 0L)
                .build();
    }
}
