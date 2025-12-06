package ui;

import dominio.entidades.Usuario;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Painel de gerenciamento de eventos que consome a API do backend THE CLUB.
 * Exibe eventos recentes em um JTextArea.
 */
public class CulturaPanel extends JPanel {

    // Usuário logado na aplicação desktop (pode ser usado para permissões depois)
    private final Usuario usuarioLogado;

    // Área de texto onde os eventos serão exibidos
    private JTextArea areaConteudo;

    /**
     * Construtor recebe o usuário logado e inicia UI + chamada à API.
     */
    public CulturaPanel(Usuario usuario) {
        this.usuarioLogado = usuario;
        inicializarComponentes(); // configura layout e componentes visuais
        carregarEventosAPI();     // dispara a carga inicial de eventos da API
    }

    /**
     * Configuração visual do painel (layout, título, área de texto).
     */
    private void inicializarComponentes() {
        // Layout principal com regiões (NORTH, CENTER etc.)
        setLayout(new BorderLayout());
        // Margem interna
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Label de título no topo
        JLabel lblTitulo = new JLabel("Gerenciamento de Eventos", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitulo, BorderLayout.NORTH);

        // Área de texto para exibir lista de eventos
        areaConteudo = new JTextArea();
        areaConteudo.setEditable(false); // apenas leitura
        areaConteudo.setFont(new Font("Arial", Font.PLAIN, 14));
        areaConteudo.setBackground(new Color(245, 245, 245));
        areaConteudo.setText("📅 Carregando eventos do servidor...");

        // Coloca a área de texto dentro de um scroll
        add(new JScrollPane(areaConteudo), BorderLayout.CENTER);
    }

    /**
     * Consome a API REST de eventos e preenche o painel com os dados.
     * Usa SwingWorker para não travar a thread da interface gráfica (EDT).
     */
    private void carregarEventosAPI() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    // URL da API (paginada, 5 eventos por página)
                    URL url = new URL("http://localhost:8081/api/admin/eventos?page=0&size=5");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "application/json");
                    // aqui você poderia adicionar Authorization se precisar de JWT

                    int status = connection.getResponseCode();
                    if (status == 200) {
                        // Lê resposta JSON do backend
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream())
                        );
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        // Converte JSON em Map genérico usando Jackson
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> json =
                                mapper.readValue(response.toString(), Map.class);

                        // Conteúdo da página vem no campo "content" (padrão Page Spring Data)
                        List<Map<String, Object>> eventos =
                                (List<Map<String, Object>>) json.get("content");

                        // Monta texto amigável para exibir na área de texto
                        StringBuilder texto = new StringBuilder("📅 Eventos Recentes:\n\n");
                        for (Map<String, Object> evento : eventos) {
                            texto.append("• Título: ").append(evento.get("titulo")).append("\n")
                                    // Usa o mesmo nome de campo do DTO: "localEvento"
                                    .append("  Local: ").append(
                                            evento.get("localEvento") != null
                                                    ? evento.get("localEvento")
                                                    : "Não informado"
                                    ).append("\n")
                                    .append("  Data: ").append(
                                            evento.get("dataEvento") != null
                                                    ? evento.get("dataEvento")
                                                    : "Sem data"
                                    ).append("\n")
                                    .append("  Organizador: ").append(
                                            evento.get("organizadorNome") != null
                                                    ? evento.get("organizadorNome")
                                                    : "Desconhecido"
                                    ).append("\n")
                                    .append("--------------------------------------------------------------\n");
                        }

                        // Atualiza o JTextArea na EDT (Event Dispatch Thread)
                        SwingUtilities.invokeLater(() ->
                                areaConteudo.setText(texto.toString())
                        );

                    } else {
                        // Em caso de erro HTTP, mostra o código de status
                        SwingUtilities.invokeLater(() ->
                                areaConteudo.setText("⚠️ Erro ao carregar eventos. HTTP " + status)
                        );
                    }

                } catch (Exception e) {
                    // Erro de rede, parsing, etc.
                    SwingUtilities.invokeLater(() ->
                            areaConteudo.setText("❌ Falha na comunicação com servidor: " + e.getMessage())
                    );
                }
                return null;
            }
        };
        worker.execute(); // dispara execução em background
    }
}
