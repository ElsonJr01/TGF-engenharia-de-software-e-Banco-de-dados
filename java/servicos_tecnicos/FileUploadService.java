package servicos_tecnicos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Serviço responsável por receber uploads de imagens
 * e salvá-las no sistema de arquivos, retornando uma URL de acesso.
 */
@Service
public class FileUploadService {

    /**
     * Diretório base onde os arquivos serão armazenados.
     * Vem da configuração:
     *   file.upload-dir=uploads
     * Se não existir no properties, usa "uploads" como valor padrão.
     */
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Salva uma imagem no diretório configurado e retorna a URL pública
     * para acessá-la (ex.: "/uploads/{nomeGerado}.png").
     */
    public String salvarImagem(MultipartFile file) throws IOException {
        // Caminho da pasta de upload (relativo ou absoluto, conforme config)
        Path uploadPath = Paths.get(uploadDir);

        // Se a pasta não existir, cria
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("📁 Pasta criada: " + uploadPath.toAbsolutePath());
        }

        // Nome original enviado pelo cliente (ex.: "foto.png")
        String originalFilename = file.getOriginalFilename();

        // Extrai a extensão do arquivo (ex.: ".png")
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // Gera um novo nome único usando UUID para evitar sobrescrita
        String novoNomeArquivo = UUID.randomUUID().toString() + extension;

        // Monta o caminho completo do arquivo: {uploadDir}/{novoNomeArquivo}
        Path filePath = uploadPath.resolve(novoNomeArquivo);

        // Copia o conteúdo do MultipartFile para o caminho de destino
        Files.copy(file.getInputStream(), filePath);

        System.out.println("✅ Arquivo salvo: " + filePath.toAbsolutePath());

        // Retorna a URL de acesso que será usada pelo frontend
        // (assumindo que "/uploads/**" está mapeado em WebConfig/FileUploadConfig)
        return "/uploads/" + novoNomeArquivo;
    }
}
