package ui;

/**
 * THE CLUB - Launcher Principal
 *
 * Classe auxiliar para inicializar o backend THE CLUB diretamente via IDE.
 * - Não substitui a aplicação principal (TheClubApplication)
 * - Ideal para executar com logs personalizados
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("🚀 Iniciando módulo principal do THE CLUB...");
        System.out.println("📰 Versão: 2.0.0 - Backend (Spring Boot 3.3 / Java 23)");
        System.out.println("======================================================");
        try {
            TheClubApplication.main(args);

            System.out.println("@ Inicialização concluída com sucesso!");
            System.out.println("@ Acesse o sistema:");
            System.out.println("@ API Base: http://localhost:8081");
            System.out.println("@ Swagger UI: http://localhost:8081/swagger-ui/index.html");
            System.out.println("@ Docs JSON: http://localhost:8081/v3/api-docs");
            System.out.println("======================================================");
        } catch (Exception e) {
            System.err.println("@ Erro ao inicializar THE CLUB!");
            System.err.println("Detalhes: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
