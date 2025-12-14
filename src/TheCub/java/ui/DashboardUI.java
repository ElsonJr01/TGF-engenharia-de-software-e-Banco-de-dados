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
 * Dashboard Desktop para o sistema THE CLUB
 * Adaptada para consumir API Spring Boot via HTTP (REST)
 */
public class DashboardUI extends JFrame {

    // Usuário autenticado no cliente desktop (usado para permissões e exibição)
    private final Usuario usuarioLogado;

    // Componente de abas principais do dashboard
    private JTabbedPane tabbedPane;

    // Label que mostra nome/tipo do usuário logado no cabeçalho
    private JLabel lblUsuarioInfo;

    // Label que exibe métricas agregadas no rodapé
    private JLabel lblMetricas;

    // Construtor: recebe o usuário logado e inicializa UI + eventos + métricas
    public DashboardUI(Usuario usuario) {
        this.usuarioLogado = usuario;
        inicializarComponentes();
        configurarEventos();
        carregarMetricasAPI();
    }

    // Monta a janela, cabeçalho, abas e rodapé
    private void inicializarComponentes() {
        setTitle("THE CLUB - Dashboard (Cliente Desktop)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);             // centraliza na tela
        setExtendedState(JFrame.MAXIMIZED_BOTH); // abre maximizado

        JPanel panelMain = new JPanel(new BorderLayout());
        panelMain.add(criarHeader(), BorderLayout.NORTH);

        // Cria o componente de abas e define fonte
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        // Adiciona abas com conteúdo placeholder (podem ser painéis depois)
        tabbedPane.addTab("🏠 Home", new JLabel("Bem-vindo ao THE CLUB!"));
        tabbedPane.addTab("📰 Noticia", new JLabel("Gerenciamento de Noticia (Web API)."));
        tabbedPane.addTab("👥 Usuários", new JLabel("Gerenciamento de Usuários (Web API)."));
        tabbedPane.addTab("📅 Eventos", new JLabel("Gerenciamento de Eventos."));
        tabbedPane.addTab("📊 Relatórios", new JLabel("Vizualização de métricas geradas pelo backend."));

        // Habilita/desabilita abas conforme tipo de usuário
        configurarPermissoes();

        panelMain.add(tabbedPane, BorderLayout.CENTER);
        panelMain.add(criarFooter(), BorderLayout.SOUTH);
        add(panelMain);
    }

    // Cria o cabeçalho com título da aplicação, usuário logado e botão Sair
    private JPanel criarHeader() {
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(70, 130, 180)); // azul
        panelHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panelHeader.setPreferredSize(new Dimension(getWidth(), 80));

        JLabel lblTitulo = new JLabel("THE CLUB - Jornal Universitário");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);

        JPanel panelUser = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelUser.setOpaque(false); // fundo transparente

        // Mostra nome e tipo do usuário (ex.: João (EDITOR))
        lblUsuarioInfo = new JLabel(String.format("%s (%s)", usuarioLogado.getNome(), usuarioLogado.getTipo()));
        lblUsuarioInfo.setFont(new Font("Arial", Font.BOLD, 14));
        lblUsuarioInfo.setForeground(Color.WHITE);

        // Botão de logout
        JButton btnLogout = new JButton("Sair");
        btnLogout.setBackground(new Color(220, 53, 69)); // vermelho
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> fazerLogout());

        panelUser.add(lblUsuarioInfo);
        panelUser.add(Box.createHorizontalStrut(20)); // espaçamento
        panelUser.add(btnLogout);

        panelHeader.add(lblTitulo, BorderLayout.WEST);
        panelHeader.add(panelUser, BorderLayout.EAST);
        return panelHeader;
    }

    // Cria o rodapé com label onde serão exibidas as métricas da API
    private JPanel criarFooter() {
        JPanel panelFooter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelFooter.setBackground(new Color(240, 240, 240)); // cinza claro
        panelFooter.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        lblMetricas = new JLabel("Carregando métricas do servidor...");
        lblMetricas.setFont(new Font("Arial", Font.PLAIN, 12));
        panelFooter.add(lblMetricas);

        return panelFooter;
    }

    // Desabilita abas conforme o tipo do usuário logado
    private void configurarPermissoes() {
        String tipoUsuario = String.valueOf(usuarioLogado.getTipo());
        if (tipoUsuario.equalsIgnoreCase("LEITOR")) {
            // Leitor vê apenas a aba Home
            tabbedPane.setEnabledAt(1, false);
            tabbedPane.setEnabledAt(2, false);
            tabbedPane.setEnabledAt(3, false);
            tabbedPane.setEnabledAt(4, false);
        } else if (tipoUsuario.equalsIgnoreCase("REDATOR")) {
            // Redator não acessa Usuários nem Relatórios
            tabbedPane.setEnabledAt(2, false);
            tabbedPane.setEnabledAt(4, false);
        }
        // EDITOR e ADMIN mantêm todas as abas habilitadas
    }

    // Chama a API de métricas em background e atualiza o rodapé com os dados
    private void carregarMetricasAPI() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Endpoint REST que fornece estatísticas gerais do sistema
                    URL url = new URL("http://localhost:8081/api/admin/relatorios/metricas");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "application/json");

                    if (connection.getResponseCode() == 200) {
                        // Lê o corpo da resposta JSON
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        // Converte JSON em Map genérico usando Jackson
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> metricas = mapper.readValue(response.toString(), Map.class);

                        // Atualiza o label de métricas na thread da UI
                        SwingUtilities.invokeLater(() -> lblMetricas.setText(String.format(
                                "📊 Usuários: %s | Noticia: %s | Comentários: %s | Eventos: %s",
                                metricas.get("totalUsuarios"),
                                metricas.get("totalArtigos"),
                                metricas.get("totalComentarios"),
                                metricas.get("totalEventos")
                        )));
                    } else {
                        // Caso a resposta HTTP não seja 200, exibe código de erro
                        SwingUtilities.invokeLater(() ->
                        {
                            try {
                                lblMetricas.setText("⚠️ Erro ao obter métricas: HTTP " + connection.getResponseCode());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                } catch (Exception e) {
                    // Erros de rede ou parsing são mostrados no rodapé
                    SwingUtilities.invokeLater(() ->
                            lblMetricas.setText("❌ Erro na comunicação com o servidor: " + e.getMessage()));
                }
                return null;
            }
        };
        worker.execute(); // executa em thread separada para não travar a interface
    }

    // Mostra diálogo de confirmação e fecha a janela em caso afirmativo
    private void fazerLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente sair?",
                "Logout",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) dispose();
    }

    // Registra listeners de UI; aqui apenas loga mudança de aba no console
    private void configurarEventos() {
        // Configura eventos de abas (futuramente pode chamar endpoints REST específicos)
        tabbedPane.addChangeListener(e -> {
            int aba = tabbedPane.getSelectedIndex();
            System.out.println("Aba alterada: " + tabbedPane.getTitleAt(aba));
        });
    }
}
