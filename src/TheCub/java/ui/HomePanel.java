package ui;

import dominio.entidades.Usuario;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Painel "Home" - Mostra métricas, artigos e eventos mais recentes via API REST.
 */
public class HomePanel extends JPanel {

    private final Usuario usuarioLogado;
    private JLabel lblMetricas;
    private JTextArea areaArtigos;
    private JTextArea areaEventos;

    public HomePanel(Usuario usuario) {
        this.usuarioLogado = usuario;
        inicializarComponentes();
        carregarDadosAPI();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblBoasVindas = new JLabel(
                String.format("Bem-vindo(a), %s!", usuarioLogado.getNome()),
                SwingConstants.CENTER
        );
        lblBoasVindas.setFont(new Font("Arial", Font.BOLD, 22));
        lblBoasVindas.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(lblBoasVindas, BorderLayout.NORTH);

        // Painel de métricas
        lblMetricas = new JLabel("Carregando métricas...", SwingConstants.CENTER);
        lblMetricas.setFont(new Font("Arial", Font.PLAIN, 16));
        lblMetricas.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(lblMetricas, BorderLayout.CENTER);

        // Painéis de conteúdo (Artigos e Eventos)
        JSplitPane painelSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        painelSplit.setResizeWeight(0.5);

        areaArtigos = criarArea("📰 Últimos Artigos Publicados...");
        areaEventos = criarArea("📅 Próximos Eventos...");

        painelSplit.setLeftComponent(new JScrollPane(areaArtigos));
        painelSplit.setRightComponent(new JScrollPane(areaEventos));

        add(painelSplit, BorderLayout.SOUTH);
    }

    private JTextArea criarArea(String textoInicial) {
        JTextArea area = new JTextArea(textoInicial);
        area.setEditable(false);
        area.setFont(new Font("Arial", Font.PLAIN, 14));
        area.setBackground(new Color(245, 245, 245));
        area.setMargin(new Insets(10, 10, 10, 10));
        return area;
    }

    /**
     * Faz chamadas REST paralelas para buscar métricas, artigos e eventos.
     */
    private void carregarDadosAPI() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    carregarMetricas();
                    carregarArtigosRecentes();
                    carregarEventosProximos();
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() ->
                            lblMetricas.setText("❌ Erro ao carregar dados: " + e.getMessage()));
                }
                return null;
            }
        }.execute();
    }

    // ====== API MÉTRICAS ======
    private void carregarMetricas() {
        try {
            URL url = new URL("http://localhost:8081/api/admin/relatorios/metricas");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) json.append(line);
                br.close();

                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> metricas = mapper.readValue(json.toString(), Map.class);

                SwingUtilities.invokeLater(() -> lblMetricas.setText(String.format(
                        "📊 Usuários: %s | Artigos: %s | Comentários: %s | Eventos: %s",
                        metricas.get("totalUsuarios"),
                        metricas.get("totalArtigos"),
                        metricas.get("totalComentarios"),
                        metricas.get("totalEventos")
                )));
            } else {
                SwingUtilities.invokeLater(() ->
                {
                    try {
                        lblMetricas.setText("⚠️ Falha ao obter métricas. HTTP " + conn.getResponseCode());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() ->
                    lblMetricas.setText("❌ Erro nas métricas: " + e.getMessage()));
        }
    }

    // ====== API ARTIGOS ======
    private void carregarArtigosRecentes() {
        try {
            URL url = new URL("http://localhost:8081/api/admin/artigos?size=3&page=0");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) json.append(line);
                br.close();

                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> obj = mapper.readValue(json.toString(), Map.class);
                List<Map<String, Object>> artigos = (List<Map<String, Object>>) obj.get("content");

                StringBuilder texto = new StringBuilder("📰 Artigos Recentes:\n\n");
                for (Map<String, Object> artigo : artigos) {
                    texto.append("• ").append(artigo.get("titulo")).append("\n")
                            .append("  Autor: ").append(artigo.get("autorNome")).append("\n")
                            .append("  Status: ").append(artigo.get("status")).append("\n")
                            .append("----------------------------------------------------------\n");
                }

                SwingUtilities.invokeLater(() -> areaArtigos.setText(texto.toString()));
            } else {
                SwingUtilities.invokeLater(() ->
                {
                    try {
                        areaArtigos.setText("⚠️ Erro ao buscar artigos. HTTP " + conn.getResponseCode());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() ->
                    areaArtigos.setText("❌ Falha ao obter artigos: " + e.getMessage()));
        }
    }

    // ====== API EVENTOS ======
    private void carregarEventosProximos() {
        try {
            URL url = new URL("http://localhost:8081/api/admin/eventos?size=3&page=0");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) json.append(line);
                br.close();

                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> obj = mapper.readValue(json.toString(), Map.class);
                List<Map<String, Object>> eventos = (List<Map<String, Object>>) obj.get("content");

                StringBuilder texto = new StringBuilder("📅 Próximos Eventos:\n\n");
                for (Map<String, Object> evento : eventos) {
                    texto.append("• ").append(evento.get("titulo")).append("\n")
                            .append("  Local: ").append(evento.getOrDefault("local", "Não informado")).append("\n")
                            .append("  Data: ").append(evento.getOrDefault("dataEvento", "Sem data")).append("\n")
                            .append("----------------------------------------------------------\n");
                }

                SwingUtilities.invokeLater(() -> areaEventos.setText(texto.toString()));
            } else {
                SwingUtilities.invokeLater(() ->
                {
                    try {
                        areaEventos.setText("⚠️ Erro ao buscar eventos. HTTP " + conn.getResponseCode());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() ->
                    areaEventos.setText("❌ Falha ao obter eventos: " + e.getMessage()));
        }
    }
}
