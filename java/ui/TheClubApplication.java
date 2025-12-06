package ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Classe principal da aplicação THE CLUB.
 * Responsável por inicializar o contexto Spring Boot,
 * escanear componentes, entidades e repositórios.
 */
@SpringBootApplication
// Define onde o Spring deve procurar @Component, @Service, @Controller, etc.
@ComponentScan(basePackages = {
        "ui",                 // telas / UI desktop integradas
        "app",                // camada de aplicação (controllers REST, etc.)
        "dominio",            // entidades, DTOs, regras de domínio
        "lib",                // configs, segurança, repositórios adicionais
        "servicos_tecnicos"   // serviços de negócio
})
// Define o pacote onde estão as entidades JPA (@Entity)
@EntityScan(basePackages = "dominio.entidades")
// Define o pacote base onde estão os repositórios JPA (@Repository)
@EnableJpaRepositories(basePackages = "lib.repository")
public class TheClubApplication {

    public static void main(String[] args) {
        // Mensagens de inicialização no console
        System.out.println("########################### Iniciando THE CLUB - Jornal Universitário");
        System.out.println("########################### Backend Spring Boot + REST API");
        System.out.println("########################### Acesse: http://localhost:8081");
        System.out.println("########################### API Docs (Swagger): http://localhost:8081/swagger-ui/index.html");
        System.out.println("*******************************************************************************************");

        // Sobe o contexto Spring Boot (embutindo Tomcat, registrando beans, etc.)
        SpringApplication.run(TheClubApplication.class, args);
    }
}
