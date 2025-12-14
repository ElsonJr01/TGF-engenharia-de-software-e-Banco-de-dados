package ui;

import dominio.entidades.Usuario;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Painel de Relatórios e Estatísticas conectado à API REST do backend.
 */
public class RelatoriosPanel extends JPanel {

    // Usuário logado (pode ser usado para regras de permissão depois)
    private final Usuario usuarioLogado;

    // Área de texto onde os relatórios serão exibidos
    private JTextArea areaConteudo;

    // Construtor: recebe o usuário logado, monta UI e carrega dados da API
    public RelatoriosPanel(Usuario usuario) {
        this.usuarioLogado = usuario;
        inicializarComponentes();
        carregarRelatoriosAPI();
    }

    // Configura layout, título e área de texto do painel
    private void inicializarComponentes() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel("📊 Relatórios e Estatísticas do Sistema", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(33, 37, 41));
        add(lblTitulo, BorderLayout.NORTH);

        areaConteudo = new JTextArea();
        areaConteudo.setEditable(false);
        areaConteudo.setFont(new Font("Consolas", Font.PLAIN, 14)); // fonte monoespaçada
        areaConteudo.setBackground(new Color(245, 245, 245));
        areaConteudo.setText("Carregando relatórios e estatísticas da API...\n");

        add(new JScrollPane(areaConteudo), BorderLayout.CENTER);
    }

    /**
     * Busca as métricas gerais da API Spring Boot (/api/admin/relatorios/metricas)
     * em uma thread de background (SwingWorker) para não travar a UI.
     */
    private void carregarRelatoriosAPI() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Monta conexão HTTP com o endpoint de métricas
                    URL url = new URL("http://localhost:8081/api/admin/relatorios/metricas");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");

                    if (conn.getResponseCode() == 200) {
                        // Lê a resposta JSON da API
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(conn.getInputStream())
                        );
                        StringBuilder resposta = new StringBuilder();
                        String linha;

                        while ((linha = reader.readLine()) != null) {
                            resposta.append(linha);
                        }
                        reader.close();

                        // Converte JSON em Map genérico usando Jackson
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> dados =
                                mapper.readValue(resposta.toString(), Map.class);

                        // Atualiza a UI na EDT chamando o método de renderização
                        SwingUtilities.invokeLater(() -> renderizarRelatorio(dados));
                    } else {
                        // Em caso de erro HTTP, mostra código de status na área de texto
                        SwingUtilities.invokeLater(() ->
                        {
                            try {
                                areaConteudo.setText(
                                        "⚠️ Erro ao carregar relatórios. HTTP " + conn.getResponseCode()
                                );
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                } catch (Exception e) {
                    // Erros de rede ou parsing são exibidos na área de texto
                    SwingUtilities.invokeLater(() ->
                            areaConteudo.setText("❌ Falha ao conectar com a API: " + e.getMessage()));
                }
                return null;
            }
        };
        worker.execute(); // dispara execução em background
    }

    /**
     * Exibe as métricas obtidas da API na área de texto
     * em um formato legível e organizado.
     */
    private void renderizarRelatorio(Map<String, Object> metricas) {
        StringBuilder texto = new StringBuilder();
        texto.append("===== 📈 RELATÓRIO GERAL DO SISTEMA =====\n\n");

        texto.append(String.format("👥 Total de Usuários: %s\n", metricas.get("totalUsuarios")));
        texto.append(String.format("📰 Total de Noticia: %s\n", metricas.get("totalArtigos")));
        texto.append(String.format("💬 Total de Comentários: %s\n", metricas.get("totalComentarios")));
        texto.append(String.format("📅 Total de Eventos: %s\n", metricas.get("totalEventos")));

        texto.append("\n==============================================\n");
        texto.append("📌 Atualizado automaticamente da API REST.\n\n");

        areaConteudo.setText(texto.toString());
    }
}
