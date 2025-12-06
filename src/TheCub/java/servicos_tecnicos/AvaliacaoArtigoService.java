package servicos_tecnicos;

import dominio.entidades.AvaliacaoArtigo;
import dominio.entidades.Artigo;
import dominio.entidades.Usuario;
import dominio.enums.TipoAvaliacao;
import lib.repository.AvaliacaoArtigoRepository;
import lib.repository.ArtigoRepository;
import lib.repository.UsuarioRepository;
import dominio.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

/**
 * Serviço responsável pela lógica de avaliação de artigos
 * (gostei, neutro, não gostei) feita pelos usuários.
 */
@Service
@RequiredArgsConstructor // gera construtor com os campos final
public class AvaliacaoArtigoService {

    // Repositório para a entidade de avaliação (tabela avaliacoes_artigos)
    private final AvaliacaoArtigoRepository avaliacaoRepo;

    // Repositório de usuários (quem avalia)
    private final UsuarioRepository usuarioRepo;

    // Repositório de artigos (o que é avaliado)
    private final ArtigoRepository artigoRepo;

    /**
     * Cria ou atualiza a avaliação de um usuário para um artigo específico.
     *
     * Regra:
     * - Cada usuário só pode ter UMA avaliação por artigo.
     * - Se já existir, é atualizada.
     * - Se não existir, é criada uma nova.
     */
    @Transactional
    public void avaliarArtigo(Long usuarioId, Long artigoId, TipoAvaliacao avaliacao) {

        // Busca o usuário que está avaliando; se não existir, lança 404
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario", "id", usuarioId)
                );

        // Busca o artigo que está sendo avaliado; se não existir, lança 404
        Artigo artigo = artigoRepo.findById(artigoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Artigo", "id", artigoId)
                );

        // Procura se já existe uma avaliação desse usuário para esse artigo
        // Se não existir, cria um novo objeto AvaliacaoArtigo "em branco"
        AvaliacaoArtigo av = avaliacaoRepo.findByUsuarioAndArtigo(usuario, artigo)
                .orElse(new AvaliacaoArtigo());

        // Preenche/atualiza os dados da avaliação
        av.setUsuario(usuario);
        av.setArtigo(artigo);
        av.setAvaliacao(avaliacao);

        // Salva (insert ou update, dependendo se já tinha ID)
        avaliacaoRepo.save(av);
    }

    /**
     * Retorna um resumo da quantidade de avaliações por tipo
     * para um determinado artigo.
     *
     * Exemplo de retorno:
     *  {
     *    GOSTEI=10,
     *    NEUTRO=3,
     *    NAO_GOSTEI=1
     *  }
     */
    public Map<TipoAvaliacao, Long> obterResumoAvaliacoes(Long artigoId) {

        // Garante que o artigo existe
        Artigo artigo = artigoRepo.findById(artigoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Artigo", "id", artigoId)
                );

        // Usa EnumMap por ser mais eficiente para chave enum
        Map<TipoAvaliacao, Long> contagem = new EnumMap<>(TipoAvaliacao.class);

        // Para cada tipo de avaliação, faz uma contagem no repositório
        for (TipoAvaliacao tipo : TipoAvaliacao.values()) {
            contagem.put(
                    tipo,
                    avaliacaoRepo.countByArtigoAndAvaliacao(artigo, tipo)
            );
        }

        return contagem;
    }
}
