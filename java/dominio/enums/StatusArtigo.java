package dominio.enums;

/**
 * Representa os diferentes estágios do ciclo de vida de um artigo.
 *
 * Cada status possui uma descrição legível para exibição em interfaces.
 */
public enum StatusArtigo {

    // Artigo ainda em edição, não visível ao público
    RASCUNHO("Rascunho"),
    // Artigo em processo de revisão (editorial, ortográfica etc.)
    REVISAO("Em Revisão"),
    // Artigo publicado e visível publicamente
    PUBLICADO("Publicado"),
    // Artigo arquivado, normalmente não listado para o público
    ARQUIVADO("Arquivado");

    // Texto amigável/legível para exibir em telas
    private final String descricao;

    // Construtor do enum, recebe a descrição
    StatusArtigo(String descricao) {
        this.descricao = descricao;
    }

    // Getter da descrição legível
    public String getDescricao() {
        return descricao;
    }

    /**
     * Retorna o StatusArtigo correspondente à string informada (ignora maiúsculas/minúsculas).
     * Aceita tanto o name do enum (RASCUNHO, PUBLICADO) quanto a descrição ("Rascunho", "Publicado").
     * Caso nenhuma correspondência seja encontrada, retorna RASCUNHO como padrão seguro.
     */
    public static StatusArtigo fromString(String valor) {
        if (valor == null) return RASCUNHO;

        for (StatusArtigo status : values()) {
            if (status.name().equalsIgnoreCase(valor)
                    || status.getDescricao().equalsIgnoreCase(valor)) {
                return status;
            }
        }
        return RASCUNHO;
    }

    // toString sobrescrito para retornar a descrição legível em vez do name do enum
    @Override
    public String toString() {
        return descricao;
    }
}
