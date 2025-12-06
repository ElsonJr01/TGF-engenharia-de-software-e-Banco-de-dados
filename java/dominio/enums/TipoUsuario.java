package dominio.enums;

/**
 * Enum que representa os tipos de usuário do sistema.
 * Cada constante tem uma descrição legível para exibição na interface.
 */
public enum TipoUsuario {

    // Usuário administrador, com acesso total ao sistema
    ADMIN("Administrador"),

    // Usuário editor, com poderes de revisão/publicação de conteúdo
    EDITOR("Editor"),

    // Usuário redator, focado em criação de conteúdo
    REDATOR("Redator"),

    // Usuário leitor, perfil padrão com acesso básico
    LEITOR("Leitor");

    // Texto descritivo amigável para ser mostrado no frontend
    private final String descricao;

    // Construtor do enum, recebe a descrição legível
    TipoUsuario(String descricao) {
        this.descricao = descricao;
    }

    // Retorna a descrição legível do tipo de usuário
    public String getDescricao() {
        return descricao;
    }
}
