package lib.config; // Pacote de configurações da aplicação (infraestrutura)

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuração para servir arquivos estáticos (imagens uploadadas).
 *
 * A ideia é:
 * - Você faz upload de arquivos para uma pasta no servidor (ex.: ./uploads)
 * - E expõe esses arquivos via HTTP, mapeando uma URL (/uploads/**)
 *   para essa pasta física.
 */
@Configuration // Indica que esta classe contém configuração do Spring
public class FileUploadConfig implements WebMvcConfigurer {

    /**
     * Caminho base onde os arquivos serão armazenados no servidor.
     * Vem do application.properties / application.yml:
     *
     *  app.upload.dir=uploads
     *  ou
     *  app.upload.dir=/var/www/theclub/uploads
     */
    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Registra handlers de recursos estáticos.
     *
     * Aqui você diz ao Spring:
     * - Toda requisição que bater em /uploads/** na URL da API
     *   deve ser atendida buscando o arquivo na pasta física
     *   configurada em app.upload.dir.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Constrói o caminho absoluto e normalizado da pasta de upload
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        // Converte para URI no formato esperado por addResourceLocations (ex.: "file:/C:/.../")
        String uploadPathString = uploadPath.toUri().toString();

        // Ex.: GET http://localhost:8081/uploads/imagem.png
        // vai procurar o arquivo físico em uploadDir/imagem.png
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPathString);
    }
}
