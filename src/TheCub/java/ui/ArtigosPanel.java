package ui;

import dominio.entidades.Usuario;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

/**
 * Painel Swing para gerenciamento/visualização de artigos.
 * Esse painel se conecta à API REST (Spring Boot) para listar artigos recentes.
 */
public class ArtigosPanel extends JPanel {

    // Representa o usuário logado na aplicação desktop
    private final Usuario usuarioLogado;

    // Área de texto onde os artigos serão exibidos
    private JTextArea areaConteudo;

    /**
     * Construtor recebe o usuário logado (pode ser útil depois para filtros/permissões).
     */
    public ArtigosPanel(Usuario usuario) {
        this.usuarioLogado = usuario;
        inicializarComponentes(); // monta layout e componentes
        carregarArtigosAPI();     // faz a chamada à API para listar artigos
    }

    /**
     * Configura o layout visual do painel.
     */
    private void inicializarComponentes() {
        // Usa BorderLayout para organizar título (NORTH) e conteúdo (CENTER)
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título no topo
        JLabel lblTitulo = new JLabel("Gerenciamento de Artigos", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitulo, BorderLayout.NORTH);

        // Área de texto para exibir as informações dos artigos
        areaConteudo = new JTextArea();
        areaConteudo.setEditable(false);
        areaConteudo.setFont(new Font("Arial", Font.PLAIN, 14));
        areaConteudo.setBackground(new Color(245, 245, 245));
        areaConteudo.setText("📰 Conectando à API para carregar artigos...");

        // Adiciona área de texto dentro de um JScrollPane (barra de rolagem)
        add(new JScrollPane(areaConteudo), BorderLayout.CENTER);
    }

    /**
     * Consome a API REST do backend e carrega a listagem de artigos no painel.
     * Faz a chamada em background (SwingWorker) para não travar a UI.
     */
    private void carregarArtigosAPI() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Endpoint da API que lista artigos (paginado)
                    URL url = new URL("http://localhost:8081/api/admin/artigos?size=5&page=0");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "application/json");
                    // Se em algum momento você usar JWT, aqui dá pra adicionar Authorization header

                    // Se resposta OK (200)
                    if (connection.getResponseCode() == 200) {
                        // Lê o corpo da resposta JSON
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream())
                        );
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        // Usa Jackson para converter JSON em Map genérico
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> json = mapper.readValue(response.toString(), Map.class);

                        // A lista de artigos vem no campo "content" (padrão do Page do Spring)
                        List<Map<String, Object>> artigos =
                                (List<Map<String, Object>>) json.get("content");

                        // Monta o texto que será exibido na área de conteúdo
                        StringBuilder texto = new StringBuilder("📰 Artigos Recentes:\n\n");
                        for (Map<String, Object> artigo : artigos) {
                            texto.append("• Título: ").append(artigo.get("titulo")).append("\n")
                                    .append("  Autor: ").append(artigo.get("autorNome")).append("\n")
                                    .append("  Status: ").append(artigo.get("status")).append("\n")
                                    .append("  Visualizações: ").append(artigo.get("visualizacoes")).append("\n")
                                    .append("------------------------------------------------------------\n");
                        }

                        // Atualiza a UI na thread de eventos do Swing
                        SwingUtilities.invokeLater(() ->
                                areaConteudo.setText(texto.toString())
                        );

                    } else {
                        // Caso a resposta NÃO seja 200, mostra código HTTP de erro
                        SwingUtilities.invokeLater(() -> {
                            try {
                                areaConteudo.setText(
                                        "@@@@@@@ Erro ao carregar artigos. HTTP " + connection.getResponseCode()
                                );
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }

                } catch (Exception e) {
                    // Erros de conexão, parsing etc.
                    SwingUtilities.invokeLater(() ->
                            areaConteudo.setText("############ Falha na comunicação com servidor: " + e.getMessage())
                    );
                }
                return null;
            }
        }.execute(); // Inicia a execução em background
    }
}
