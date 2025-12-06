package lib.config; // Pacote de configuração da aplicação (infra/infraestrutura)

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração global de CORS da aplicação.
 *
 * CORS (Cross-Origin Resource Sharing) controla quais domínios
 * externos podem acessar a sua API via browser (fetch/axios).
 *
 * Implementando WebMvcConfigurer, você consegue customizar
 * o comportamento do Spring MVC, incluindo as regras de CORS.
 */
@Configuration // Indica que esta classe define beans/configurações do Spring
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Método chamado pelo Spring para registrar as configurações de CORS.
     *
     * Aqui você está liberando TUDO:
     *  - qualquer caminho da API (/**)
     *  - qualquer origem (allowedOrigins("*"))
     *  - qualquer método HTTP (GET, POST, PUT, DELETE, etc.)
     *  - qualquer header
     *
     * Isso é ótimo para desenvolvimento/local, mas em produção
     * normalmente se restringe os domínios (origens) permitidos.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")      // Aplica essa configuração a todos os endpoints
                .allowedOrigins("*")    // Permite qualquer origem (http://qualquer-coisa)
                .allowedMethods("*")    // Permite todos os métodos HTTP
                .allowedHeaders("*");   // Permite todos os cabeçalhos
    }
}
