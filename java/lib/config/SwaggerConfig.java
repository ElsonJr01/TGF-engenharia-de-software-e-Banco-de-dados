package lib.config; // Pacote de configurações da aplicação (infra)

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do Swagger / OpenAPI para a API do THE CLUB.
 *
 * Essa classe define:
 * - Metadados da API (título, versão, descrição, contato, licença)
 * - Ambientes/servers que aparecerão no Swagger UI (dev, produção, etc.)
 *
 * O Springdoc (ou lib equivalente) lê esse bean OpenAPI e monta
 * automaticamente a documentação interativa em /swagger-ui/**.
 */
@Configuration // Indica que esta classe contém beans de configuração
public class SwaggerConfig {

    /**
     * Define um bean do tipo OpenAPI com as informações personalizadas
     * da sua API.
     *
     * Esse bean será usado pelo mecanismo de documentação para gerar
     * a especificação OpenAPI 3 e o Swagger UI.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Bloco de informações gerais da API
                .info(new Info()
                        .title("THE CLUB - API do Jornal Universitário") // Título exibido no Swagger UI
                        .version("2.0.0")                                // Versão da API
                        .description("""
                            API REST do sistema de jornal universitário **THE CLUB**.

                            ### Funcionalidades Principais:
                            - **Artigos:** Criação, edição e publicação de notícias
                            - **Categorias:** Organização e cores personalizadas
                            - **Comentários:** Interação e moderação de discussões
                            - **Eventos:** Agenda e notificações universitárias
                            - **Usuários:** Controle de papéis e autenticação por JWT

                            ### Acesso
                            - Público: endpoints abertos para leitura
                            - Administrativo: protegido via JWT e roles específicas
                            - Documentação interativa disponível abaixo
                            """) // Descrição rica em Markdown para o Swagger
                        // Informações de contato exibidas na documentação
                        .contact(new Contact()
                                .name("Equipe THE CLUB")
                                .email("contato@theclub.com")
                                .url("https://theclub.unifesspa.edu.br"))
                        // Informação de licença do projeto (ex.: MIT)
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )
                // Lista de servidores (ambientes) disponíveis para testar na doc
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")                  // URL base do ambiente de dev
                                .description("Ambiente de Desenvolvimento"),
                        new Server()
                                .url("https://api.theclub.unifesspa.edu.br")  // URL base do ambiente de produção
                                .description("Ambiente de Produção")
                ));
    }
}
