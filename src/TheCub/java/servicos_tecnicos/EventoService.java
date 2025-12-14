package servicos_tecnicos;

import dominio.entidades.Evento;
import dominio.entidades.Usuario;
import dominio.dto.request.EventoRequestDTO;
import dominio.dto.response.EventoResponseDTO;
import dominio.exception.BusinessException;
import dominio.exception.ResourceNotFoundException;
import lib.repository.EventoRepository;
import lib.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela gestão de eventos culturais e universitários.
 * Controla criação, listagem, atualização e cancelamento de eventos.
 */
@Service
@RequiredArgsConstructor // injeta repositórios via construtor
@Slf4j // habilita logging estruturado
public class EventoService {

    // Repositório JPA da entidade Cultura (eventos)
    private final EventoRepository eventoRepository;
    // Repositório de usuários (organizadores dos eventos)
    private final UsuarioRepository usuarioRepository;

    // ====== CRIAR EVENTO ======

    /**
     * Cria um novo evento cultural/universitário.
     * Valida permissões do organizador e regras de data/local.
     */
    @Transactional
    public EventoResponseDTO criarEvento(EventoRequestDTO dto) {
        log.info(" Criando novo evento: {}", dto.getTitulo());

        // Busca o organizador; se não existir, lança 404
        Usuario organizador = usuarioRepository.findById(dto.getOrganizadorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário", "id", dto.getOrganizadorId())
                );

        // Regra de negócio: apenas editores e admins podem criar eventos
        if (!organizador.isEditorOuAdmin()) {
            throw new BusinessException("Apenas editores e administradores podem criar eventos");
        }

        // Validações de negócio
        validarDataEvento(dto.getDataEvento());
        validarConflitoDeHorario(dto.getDataEvento(), dto.getLocalEvento(), null);

        // Monta a entidade Cultura (evento) usando Builder pattern
        Evento cultura = Evento.builder()
                .titulo(dto.getTitulo().trim())
                .descricao(dto.getDescricao().trim())
                .dataEvento(dto.getDataEvento())
                .localEvento(dto.getLocalEvento().trim())
                .imagem(dto.getImagem())
                .linkInscricao(dto.getLinkInscricao())
                .ativo(true) // evento sempre nasce ativo
                .organizador(organizador)
                .build();

        // Persiste no banco (o @CreationTimestamp define dataCriacao automaticamente)
        eventoRepository.save(cultura);
        log.info(" Evento criado com sucesso: ID = {}", cultura.getId());

        // Converte para DTO de resposta
        return convertToDTO(cultura);
    }

    // ====== BUSCAR ======

    /**
     * Busca um evento específico por ID.
     */
    @Transactional(readOnly = true)
    public EventoResponseDTO buscarPorId(Long id) {
        Evento cultura = eventoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Evento", "id", id)
                );
        return convertToDTO(cultura);
    }

    // ====== LISTAGENS ======

    /**
     * Lista todos os eventos (ativos e inativos) com paginação.
     */
    @Transactional(readOnly = true)
    public Page<EventoResponseDTO> listarTodos(Pageable pageable) {
        return eventoRepository
                .findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Lista apenas eventos ativos, com paginação.
     */
    @Transactional(readOnly = true)
    public Page<EventoResponseDTO> listarAtivos(Pageable pageable) {
        return eventoRepository
                .findByAtivoTrue(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Retorna lista de eventos que vão acontecer nos próximos 7 dias.
     * Sem paginação, para exibição em destaque/calendário.
     */
    @Transactional(readOnly = true)
    public List<EventoResponseDTO> listarProximosEventos() {
        return eventoRepository
                .findProximosEventos(LocalDateTime.now())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista eventos próximos com suporte a paginação.
     */
    @Transactional(readOnly = true)
    public Page<EventoResponseDTO> listarProximosEventosComPaginacao(Pageable pageable) {
        return eventoRepository
                .findProximosEventos(LocalDateTime.now(), pageable)
                .map(this::convertToDTO);
    }

    /**
     * Lista eventos que já ocorreram (passados).
     */
    @Transactional(readOnly = true)
    public Page<EventoResponseDTO> listarEventosPassados(Pageable pageable) {
        return eventoRepository
                .findEventosPassados(LocalDateTime.now(), pageable)
                .map(this::convertToDTO);
    }

    /**
     * Lista eventos em destaque (próximos 7 dias, normalmente para homepage).
     */
    @Transactional(readOnly = true)
    public List<EventoResponseDTO> listarEventosEmDestaque() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime janela = agora.plusDays(7);
        return eventoRepository
                .findEventosEmDestaque(agora, janela)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista eventos organizados por um usuário específico.
     */
    @Transactional(readOnly = true)
    public Page<EventoResponseDTO> listarEventosPorOrganizador(Long id, Pageable pageable) {
        // Garante que o organizador existe
        Usuario organizador = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário", "id", id)
                );

        return eventoRepository
                .findByOrganizador(organizador, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Lista eventos dentro de um período específico (data inicial e final).
     */
    @Transactional(readOnly = true)
    public List<EventoResponseDTO> listarEventosPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return eventoRepository
                .findEventosPorPeriodo(inicio, fim)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ====== BUSCAS E FILTROS ======

    /**
     * Busca eventos usando múltiplos filtros simultâneos.
     * Permite filtrar por título, local, organizador, status, período.
     */
    @Transactional(readOnly = true)
    public Page<EventoResponseDTO> buscarComFiltros(
            String titulo,
            String local,
            Long organizadorId,
            Boolean ativo,
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    ) {
        return eventoRepository
                .buscarComFiltros(titulo, local, organizadorId, ativo, inicio, fim, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Busca eventos por parte do título (case-insensitive).
     */
    @Transactional(readOnly = true)
    public Page<EventoResponseDTO> buscarPorTitulo(String titulo, Pageable pageable) {
        return eventoRepository
                .findByTituloContainingIgnoreCase(titulo.trim(), pageable)
                .map(this::convertToDTO);
    }

    // ====== ATUALIZAR / CANCELAR / EXCLUIR ======

    /**
     * Atualiza os dados de um evento existente.
     * Revalida data e conflitos de horário.
     */
    @Transactional
    public EventoResponseDTO atualizarEvento(Long id, EventoRequestDTO dto) {
        // Busca o evento ou lança 404
        Evento cultura = eventoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Evento", "id", id)
                );

        // Revalida as regras de negócio
        validarDataEvento(dto.getDataEvento());
        validarConflitoDeHorario(dto.getDataEvento(), dto.getLocalEvento(), id);

        // Atualiza campos
        cultura.setTitulo(dto.getTitulo().trim());
        cultura.setDescricao(dto.getDescricao().trim());
        cultura.setDataEvento(dto.getDataEvento());
        cultura.setLocalEvento(dto.getLocalEvento().trim());
        cultura.setImagem(dto.getImagem());
        cultura.setLinkInscricao(dto.getLinkInscricao());

        eventoRepository.save(cultura);
        log.info(" Evento atualizado (ID = {})", id);

        return convertToDTO(cultura);
    }

    /**
     * Altera o status ativo/inativo de um evento.
     */
    @Transactional
    public void alterarStatus(Long id, Boolean ativo) {
        Evento cultura = eventoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Evento", "id", id)
                );

        cultura.setAtivo(ativo);
        eventoRepository.save(cultura);

        log.info(" Status do evento atualizado (ID = {}, ativo = {})", id, ativo);
    }

    /**
     * Cancela um evento (marca como inativo).
     * Não permite cancelar eventos que já ocorreram.
     */
    @Transactional
    public void cancelarEvento(Long id) {
        Evento cultura = eventoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Evento", "id", id)
                );

        // Regra de negócio: não cancelar eventos passados
        if (cultura.getDataEvento().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Não é possível cancelar um evento já ocorrido.");
        }

        cultura.setAtivo(false);
        eventoRepository.save(cultura);

        log.warn(" Evento cancelado (ID = {})", id);
    }

    /**
     * Exclui permanentemente um evento do banco.
     */
    @Transactional
    public void deletarEvento(Long id) {
        Evento cultura = eventoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Evento", "id", id)
                );

        eventoRepository.delete(cultura);
        log.error(" Evento deletado permanentemente (ID = {})", id);
    }

    // ====== CONSULTAS DE STATUS ======

    /**
     * Verifica se um evento está próximo (próximos 7 dias).
     * Usa o método `isProximo()` da própria entidade.
     */
    @Transactional(readOnly = true)
    public boolean eventoEstaProximo(Long id) {
        Evento cultura = eventoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Evento", "id", id)
                );

        LocalDateTime agora = LocalDateTime.now();
        // Próximo = após agora E antes de agora + 7 dias
        return cultura.getDataEvento().isBefore(agora.plusDays(7))
                && cultura.getDataEvento().isAfter(agora);
    }

    /**
     * Verifica se um evento já aconteceu.
     * Usa o método `jaAconteceu()` da própria entidade.
     */
    @Transactional(readOnly = true)
    public boolean eventoJaAconteceu(Long id) {
        Evento cultura = eventoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Evento", "id", id)
                );

        return cultura.getDataEvento().isBefore(LocalDateTime.now());
    }

    // ====== ESTATÍSTICAS ======

    /**
     * Conta quantos eventos vão acontecer após agora (futuros).
     */
    @Transactional(readOnly = true)
    public Long contarProximosEventos() {
        return eventoRepository.countByDataEventoAfter(LocalDateTime.now());
    }

    /**
     * Retorna estatísticas completas de eventos:
     * - total geral
     * - ativos
     * - cancelados
     * - próximos
     * - passados
     */
    @Transactional(readOnly = true)
    public EventoEstatisticas obterEstatisticas() {
        long total = eventoRepository.count();
        long ativos = eventoRepository.countByAtivoTrue();
        long cancelados = eventoRepository.countByAtivoFalse();
        long proximos = eventoRepository.countByDataEventoAfter(LocalDateTime.now());
        long passados = eventoRepository.countByDataEventoBefore(LocalDateTime.now());

        log.info(" Estatísticas de eventos — Total: {}, Ativos: {}, Cancelados: {}", total, ativos, cancelados);

        // Retorna record com as estatísticas
        return new EventoEstatisticas(total, ativos, proximos, cancelados, passados);
    }

    /**
     * Record imutável para encapsular estatísticas de eventos.
     * Usado como retorno do método obterEstatisticas().
     */
    public record EventoEstatisticas(
            long total,      // total geral de eventos
            long ativos,     // eventos com ativo = true
            long proximos,   // eventos com data > agora
            long cancelados, // eventos com ativo = false
            long passados    // eventos com data < agora
    ) {}

    // ====== UTILITÁRIOS ======

    /**
     * Validações de data do evento:
     * - não pode ser nula
     * - deve ser futura
     * - não pode ser mais de 1 ano no futuro
     */
    private void validarDataEvento(LocalDateTime dataEvento) {
        if (dataEvento == null) {
            throw new BusinessException("A data do evento é obrigatória.");
        }
        if (dataEvento.isBefore(LocalDateTime.now())) {
            throw new BusinessException("A data do evento deve ser futura.");
        }
        if (dataEvento.isAfter(LocalDateTime.now().plusYears(1))) {
            throw new BusinessException("O evento não pode ser agendado para mais de 1 ano no futuro.");
        }
    }

    /**
     * Verifica conflitos de horário no mesmo local.
     * Regra: eventos no mesmo local não podem estar a menos de 2 horas de distância.
     */
    private void validarConflitoDeHorario(LocalDateTime dataEvento, String local, Long eventoIdExcluir) {
        // Define janela do dia inteiro para buscar eventos
        LocalDateTime inicio = dataEvento.toLocalDate().atStartOfDay();
        LocalDateTime fim = dataEvento.toLocalDate().atTime(23, 59, 59);

        // Busca todos os eventos do mesmo dia no mesmo local
        List<Evento> eventosNoDia = eventoRepository
                .findEventosPorPeriodo(inicio, fim)
                .stream()
                // Filtra pelo local (case-insensitive)
                .filter(e -> e.getLocalEvento().equalsIgnoreCase(local))
                // Exclui o evento atual se for uma atualização
                .filter(e -> eventoIdExcluir == null || !e.getId().equals(eventoIdExcluir))
                .collect(Collectors.toList());

        // Para cada evento encontrado, verifica conflito de horário
        for (Evento e : eventosNoDia) {
            // Calcula diferença em horas entre os eventos
            long horas = Math.abs(
                    java.time.Duration.between(e.getDataEvento(), dataEvento).toHours()
            );
            if (horas < 2) {
                throw new BusinessException(
                        "Conflito de horário com outro evento: " +
                                e.getTitulo() + " às " + e.getDataEvento()
                );
            }
        }
    }

    /**
     * Converte entidade Cultura para DTO de resposta.
     * Inclui informações do organizador.
     */
    private EventoResponseDTO convertToDTO(Evento cultura) {
        return EventoResponseDTO.builder()
                .id(cultura.getId())
                .titulo(cultura.getTitulo())
                .descricao(cultura.getDescricao())
                .dataEvento(cultura.getDataEvento())
                .localEvento(cultura.getLocalEvento())
                .imagem(cultura.getImagem())
                .linkInscricao(cultura.getLinkInscricao())
                .ativo(cultura.getAtivo())
                .organizadorNome(cultura.getOrganizador().getNome())
                .organizadorId(cultura.getOrganizador().getId())
                .dataCriacao(cultura.getDataCriacao())
                .build();
    }
}
