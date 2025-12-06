package app; // Pacote onde fica o controller de health/check

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller de saúde/status da aplicação.
 * Útil para testes rápidos, monitoramento e ambiente de dev.
 */
@RestController // Indica que expõe endpoints REST que retornam JSON
public class HealthController {

    /**
     * Endpoint raiz da API.
     * Retorna informações básicas da aplicação e link para o Swagger.
     */
    @GetMapping("/") // GET /
    public Map<String, Object> home() {
        // Mapa de resposta com informações diversas
        Map<String, Object> response = new HashMap<>();
        response.put("application", "THE CLUB - Jornal Universitário"); // Nome da aplicação
        response.put("status", "online"); // Status simples
        response.put("timestamp", LocalDateTime.now()); // Data/hora atual do servidor
        response.put("swagger", "http://localhost:8081/swagger-ui/index.html"); // URL do Swagger em dev
        return response; // Spring converte o Map em JSON automaticamente
    }

    /**
     * Endpoint de health-check.
     * Pode ser usado por monitoramento (Docker, Kubernetes, etc.).
     */
    @GetMapping("/health") // GET /health
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP"); // Indica que a API está no ar
        response.put("message", "API está funcionando perfeitamente!"); // Mensagem amigável
        return response;
    }
}
