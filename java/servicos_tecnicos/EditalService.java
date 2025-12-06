package servicos_tecnicos;

import dominio.entidades.Edital;
import dominio.entidades.Usuario;
import dominio.dto.request.EditalRequestDTO;
import dominio.dto.response.EditalResponseDTO;
import lib.repository.EditalRepository;
import lib.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelas regras de negócio relacionadas a Editais.
 * Cuida de criação, listagem, busca, atualização e exclusão de editais.
 */
@Service
@RequiredArgsConstructor // injeta os repositórios via construtor
@Slf4j // habilita logging com log.info, log.error, etc.
public class EditalService {

    // Repositório JPA para a entidade Edital
    private final EditalRepository editalRepository;

    // Repositório para usuários (autores dos editais)
    private final UsuarioRepository usuarioRepository;

    /**
     * Cria um novo edital a partir de um DTO de requisição.
     * Usa o usuário autenticado como autor.
     */
    @Transactional
    public EditalResponseDTO criar(EditalRequestDTO dto) {
        log.info(" Iniciando criação de edital: {}", dto.getTitulo());

        // Recupera o usuário autenticado do contexto de segurança
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info(" Usuário autenticado: {}", auth.getName());

        // Busca o usuário por e-mail; se não encontrar, lança RuntimeException
        Usuario autor = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> {
                    log.error(" Usuário não encontrado: {}", auth.getName());
                    return new RuntimeException("Usuário não encontrado");
                });

        log.info(" Criando edital para o usuário: {}", autor.getNome());

        // Monta a entidade Edital usando o Builder
        Edital edital = Edital.builder()
                .titulo(dto.getTitulo())
                .descricao(dto.getDescricao())
                .arquivoUrl(dto.getArquivoUrl())
                .arquivoNome(dto.getArquivoNome())
                .dataValidade(dto.getDataValidade())
                .ativo(true)           // edital nasce ativo
                .visualizacoes(0)      // começa com 0 visualizações
                .autor(autor)          // associa o autor autenticado
                .build();

        // Persiste no banco
        Edital editalSalvo = editalRepository.save(edital);
        log.info(" Edital salvo com ID: {}", editalSalvo.getId());

        // Converte para DTO de resposta
        return toDTO(editalSalvo);
    }

    /**
     * Lista todos os editais ativos, ordenados pela data de publicação
     * (decrescente), com suporte a paginação.
     */
    @Transactional(readOnly = true)
    public Page<EditalResponseDTO> listarAtivos(Pageable pageable) {
        log.info(" Listando editais ativos");

        // Busca pagina de editais ativos ordenados por dataPublicacao DESC
        Page<Edital> editais = editalRepository.findByAtivoTrueOrderByDataPublicacaoDesc(pageable);

        log.info(" Encontrados {} editais", editais.getTotalElements());

        // Converte a página de entidades para página de DTOs
        return editais.map(this::toDTO);
    }

    /**
     * Busca um edital pelo ID.
     * Se encontrar, incrementa o contador de visualizações.
     */
    @Transactional
    public EditalResponseDTO buscarPorId(Long id) {
        log.info(" Buscando edital ID: {}", id);

        // Busca edital ou lança exceção se não existir
        Edital edital = editalRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(" Edital não encontrado: ID {}", id);
                    return new RuntimeException("Edital não encontrado");
                });

        // Regra de negócio: cada busca incrementa visualizações
        log.info("👁 Incrementando visualizações do edital: {}", edital.getTitulo());
        edital.incrementarVisualizacoes();
        editalRepository.save(edital);

        return toDTO(edital);
    }

    /**
     * Exclui definitivamente um edital pelo ID.
     */
    @Transactional
    public void excluir(Long id) {
        log.info("🗑 Excluindo edital ID: {}", id);

        // Verifica se o edital existe antes de deletar
        if (!editalRepository.existsById(id)) {
            log.error(" Edital não encontrado para exclusão: ID {}", id);
            throw new RuntimeException("Edital não encontrado");
        }

        // Exclui por ID diretamente
        editalRepository.deleteById(id);
        log.info(" Edital excluído com sucesso");
    }

    /**
     * Atualiza dados básicos de um edital existente.
     * Atualiza o arquivo apenas se uma nova URL for fornecida.
     */
    @Transactional
    public EditalResponseDTO atualizar(Long id, EditalRequestDTO dto) {
        log.info(" Atualizando edital ID: {}", id);

        // Busca edital ou lança exceção
        Edital edital = editalRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Edital não encontrado para atualização: ID {}", id);
                    return new RuntimeException("Edital não encontrado");
                });

        log.info("  Dados anteriores - Título: {}", edital.getTitulo());

        // Atualiza campos básicos
        edital.setTitulo(dto.getTitulo());
        edital.setDescricao(dto.getDescricao());
        edital.setDataValidade(dto.getDataValidade());

        // Atualiza arquivo apenas se uma nova URL for enviada
        if (dto.getArquivoUrl() != null && !dto.getArquivoUrl().isEmpty()) {
            log.info("📎 Atualizando arquivo");
            edital.setArquivoUrl(dto.getArquivoUrl());
            edital.setArquivoNome(dto.getArquivoNome());
        }

        // Salva as alterações
        Edital editalAtualizado = editalRepository.save(edital);
        log.info(" Edital atualizado - Novo título: {}", editalAtualizado.getTitulo());

        return toDTO(editalAtualizado);
    }

    /**
     * Converte a entidade Edital para o DTO de resposta EditalResponseDTO.
     * Centraliza o mapeamento para evitar duplicação.
     */
    private EditalResponseDTO toDTO(Edital edital) {
        return EditalResponseDTO.builder()
                .id(edital.getId())
                .titulo(edital.getTitulo())
                .descricao(edital.getDescricao())
                .arquivoUrl(edital.getArquivoUrl())
                .arquivoNome(edital.getArquivoNome())
                .dataPublicacao(edital.getDataPublicacao())
                .dataValidade(edital.getDataValidade())
                .ativo(edital.getAtivo())
                .visualizacoes(edital.getVisualizacoes())
                .autorNome(edital.getAutor().getNome())
                .dataCriacao(edital.getDataCriacao())
                .build();
    }
}
