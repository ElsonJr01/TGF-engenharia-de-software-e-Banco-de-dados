package app; // Pacote onde o controller de upload está definido

// Serviço responsável por salvar o arquivo (no disco, S3, etc.)
import servicos_tecnicos.FileUploadService;

// Anotações do Swagger/OpenAPI para documentar o endpoint de upload
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

// Lombok: gera construtor com campos final
import lombok.RequiredArgsConstructor;
// Classe para montar respostas HTTP
import org.springframework.http.ResponseEntity;
// Anotações REST
import org.springframework.web.bind.annotation.*;
// Tipo de arquivo enviado em multipart/form-data
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para upload de arquivos (principalmente imagens).
 * Usado pelo frontend para enviar imagens de capa, etc.
 */
@RestController // Indica que é um controller REST (JSON)
@RequestMapping("/api/upload") // Prefixo base para o endpoint de upload
@RequiredArgsConstructor // Lombok: gera construtor com o campo final fileUploadService
@Tag(name = "Upload de Arquivos") // Grupo no Swagger
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // Libera CORS para esses frontends
public class FileUploadController {

    // Serviço que encapsula a lógica de salvar o arquivo e retornar a URL
    private final FileUploadService fileUploadService;

    /**
     * Endpoint para upload de imagem/arquivo via multipart/form-data.
     * Aceita tanto o campo "imagem" quanto "file".
     */
    @Operation(summary = "Upload de imagem")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = "multipart/form-data",
                    schema = @Schema(type = "object"),
                    schemaProperties = {
                            // Campo "imagem" no form-data (string binária)
                            @SchemaProperty(
                                    name = "imagem",
                                    schema = @Schema(type = "string", format = "binary")
                            ),
                            // Campo alternativo "file" no form-data
                            @SchemaProperty(
                                    name = "file",
                                    schema = @Schema(type = "string", format = "binary")
                            )
                    }
            )
    )
    @PostMapping // POST /api/upload
    public ResponseEntity<Map<String, String>> uploadImagem(
            // Arquivo enviado no campo "imagem" (opcional)
            @RequestParam(value = "imagem", required = false) MultipartFile imagem,
            // Arquivo enviado no campo "file" (opcional)
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {

        try {
            // Escolhe qual MultipartFile usar: prioriza "imagem", senão "file"
            MultipartFile arquivo = imagem != null ? imagem : file;

            // Valida se algum arquivo foi enviado
            if (arquivo == null || arquivo.isEmpty()) {
                System.err.println("%%%%%%%%%%%% Nenhum arquivo recebido");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Nenhum arquivo enviado");
                // Retorna 400 (Bad Request) com mensagem de erro
                return ResponseEntity.badRequest().body(error);
            }

            // Logs simples com informações do arquivo recebido
            System.out.println("°°°°°° Recebendo arquivo: " + arquivo.getOriginalFilename());
            System.out.println("() Tamanho: " + arquivo.getSize() + " bytes");

            // Chama o serviço para salvar a imagem e obter a URL relativa
            String url = fileUploadService.salvarImagem(arquivo);
            // Monta a URL completa, fixando o host/porta da API
            String fullUrl = "http://localhost:8081" + url;

            System.out.println("XXXX URL gerada: " + fullUrl);

            // Corpo da resposta de sucesso
            Map<String, String> response = new HashMap<>();
            response.put("url", fullUrl); // URL completa para o frontend usar
            response.put("message", "Imagem enviada com sucesso!");

            // Retorna 200 (OK) com os dados da imagem
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Tratamento genérico de erro no upload
            System.err.println("°°° Erro ao fazer upload: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao fazer upload");
            error.put("message", e.getMessage());

            // Retorna 500 (Internal Server Error) com detalhes básicos
            return ResponseEntity.status(500).body(error);
        }
    }
}
