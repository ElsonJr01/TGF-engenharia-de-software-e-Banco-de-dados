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
 * Painel de gerenciamento de usuários conectado à API REST do backend THE CLUB.
 */
public class UsuariosPanel extends JPanel {

    // Usuário logado no cliente desktop (pode ser usado para permissões futuramente)
    private final Usuario usuarioLogado;

    // Área de texto onde a lista de usuários será exibida
    private JTextArea areaConteudo;

    // Construtor: recebe o usuário logado, monta UI e dispara chamada à API
    public UsuariosPanel(Usuario usuario) {
        this.usuarioLogado = usuario;
        inicializarComponentes();
        carregarUsuariosAPI();
    }

    // Configura layout, título e área de texto do painel
    private void inicializarComponentes() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel("👥 Gerenciamento de Usuários", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitulo, BorderLayout.NORTH);

        // Área de texto para exibir os dados retornados pela API
        areaConteudo = new JTextArea("Carregando lista de usuários...");
        areaConteudo.setEditable(false);
        areaConteudo.setFont(new Font("Arial", Font.PLAIN, 14));
        areaConteudo.setLineWrap(true);      // quebra automática de linha
        areaConteudo.setWrapStyleWord(true); // quebra respeitando palavras
        areaConteudo.setBackground(new Color(245, 245, 245));

        add(new JScrollPane(areaConteudo), BorderLayout.CENTER);
    }

    /**
     * Carrega os dados de usuários do backend Spring Boot via chamada REST
     * usando HttpURLConnection em uma SwingWorker para não travar a UI.
     */
    private void carregarUsuariosAPI() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Endpoint paginado da API de usuários
                    URL url = new URL("http://localhost:8081/api/admin/usuarios?page=0&size=10");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "application/json");

                    int status = connection.getResponseCode();
                    if (status == 200) {
                        // Lê o JSON de resposta
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream())
                        );
                        StringBuilder json = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) json.append(line);
                        reader.close();

                        // Converte JSON em Map genérico
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> response =
                                mapper.readValue(json.toString(), Map.class);

                        // Recupera a lista paginada (campo "content" do Page)
                        List<Map<String, Object>> usuarios =
                                (List<Map<String, Object>>) response.get("content");

                        // Monta texto formatado com dados dos usuários
                        StringBuilder texto = new StringBuilder("👥 Lista de Usuários Ativos:\n\n");
                        for (Map<String, Object> u : usuarios) {
                            texto.append("• Nome: ").append(u.get("nome")).append("\n")
                                    .append("  E-mail: ").append(u.get("email")).append("\n")
                                    .append("  Tipo: ").append(u.get("tipo")).append("\n")
                                    .append("  Ativo: ").append(u.get("ativo")).append("\n")
                                    .append("----------------------------------------------------------\n");
                        }

                        // Atualiza a área de texto na thread da UI (EDT)
                        SwingUtilities.invokeLater(() ->
                                areaConteudo.setText(texto.toString()));
                    } else {
                        // Em caso de erro HTTP, exibe código de status
                        SwingUtilities.invokeLater(() ->
                                areaConteudo.setText("⚠️ Erro ao carregar usuários. HTTP " + status));
                    }

                } catch (Exception e) {
                    // Erros de rede ou parsing são exibidos na área de texto
                    SwingUtilities.invokeLater(() ->
                            areaConteudo.setText("❌ Falha ao obter usuários: " + e.getMessage()));
                }
                return null;
            }
        };
        // Executa a tarefa em background
        worker.execute();
    }
}
