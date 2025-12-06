package servicos_tecnicos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Serviço responsável por salvar e gerenciar arquivos uploadados no sistema.
 * Trabalha com o sistema de arquivos local, usando um diretório configurável.
 */
@Service
public class FileStorageService {

    // Diretório raiz onde os arquivos serão armazenados
    private final Path fileStorageLocation;

    /**
     * Construtor recebe o diretório de upload via configuração:
     * app.upload.dir=uploads
     * ou um caminho absoluto.
     */
    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        // Converte a string em Path absoluto e normalizado
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            // Cria o diretório (e pais) se ainda não existir
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            // Erro na criação do diretório de upload
            throw new RuntimeException("Não foi possível criar o diretório de upload.", ex);
        }
    }

    /**
     * Salva o arquivo no servidor e retorna APENAS o nome do arquivo salvo.
     * A URL completa geralmente é montada pelo controller/frontend:
     *   /uploads/{nomeArquivo}
     */
    public String salvarArquivo(MultipartFile file) {
        // Nome original enviado pelo cliente (pode ser nulo)
        String nomeOriginal = file.getOriginalFilename();

        // Extrai a extensão, se houver (ex.: ".png", ".pdf")
        String extensao = nomeOriginal != null && nomeOriginal.contains(".")
                ? nomeOriginal.substring(nomeOriginal.lastIndexOf("."))
                : "";

        // Gera um nome único (UUID) para evitar colisão de arquivos
        String nomeArquivo = UUID.randomUUID().toString() + extensao;

        try {
            // Caminho final do arquivo: {uploadDir}/{nomeArquivo}
            Path targetLocation = this.fileStorageLocation.resolve(nomeArquivo);

            // Copia o conteúdo do MultipartFile para o destino
            // REPLACE_EXISTING: sobrescreve se já existir (raro por causa do UUID)
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Retorna só o nome gerado (para ser guardado no banco)
            return nomeArquivo;
        } catch (IOException ex) {
            // Qualquer problema de IO ao salvar o arquivo
            throw new RuntimeException("Erro ao salvar arquivo " + nomeOriginal, ex);
        }
    }

    /**
     * Deleta um arquivo do servidor, se existir.
     * Não lança erro se o arquivo já não existir.
     */
    public void deletarArquivo(String nomeArquivo) {
        try {
            // Resolve o caminho completo do arquivo a partir do nome
            Path filePath = this.fileStorageLocation.resolve(nomeArquivo).normalize();

            // Tenta apagar; se não existir, simplesmente não faz nada
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Problema de IO ao tentar deletar
            throw new RuntimeException("Erro ao deletar arquivo " + nomeArquivo, ex);
        }
    }
}
