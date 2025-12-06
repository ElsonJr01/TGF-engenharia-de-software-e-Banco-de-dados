package lib.config; // Pacote de configurações web da aplicação

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configurações globais de Web MVC:
 * - Servir arquivos estáticos (uploads)
 * - Regras de CORS para o frontend
 */
@Configuration // Indica que esta classe contém configuração do Spring
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configura mapeamento de recursos estáticos.
     *
     * Aqui você diz ao Spring:
     *  - Qualquer requisição para /uploads/** na URL
     *    deve ser atendida buscando arquivos na pasta física "uploads/"
     *    (relativa ao diretório onde a aplicação está rodando).
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ex.: GET /uploads/imagem.png -> file:uploads/imagem.png
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        // Log simples para confirmar no console que o mapeamento foi configurado
        System.out.println("✅ Configurado para servir arquivos de: file:uploads/");
    }

    /**
     * Configura CORS global para a API.
     *
     * CORS controla quais origens (domínios) podem acessar sua API
     * a partir de chamadas feitas em navegadores (fetch/axios).
     *
     * Aqui:
     *  - Permite qualquer endpoint da API (/**)
     *  - Aceita requisições vindas de:
     *      - http://localhost:3000 (provavelmente React)
     *      - http://localhost:5173 (Vite)
     *  - Libera métodos HTTP comuns
     *  - Libera todos os headers
     *  - allowCredentials(true) permite enviar cookies/Authorization
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todos os endpoints
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
