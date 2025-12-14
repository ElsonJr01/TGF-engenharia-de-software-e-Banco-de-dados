package ui;

import dominio.entidades.Usuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import dominio.enums.TipoUsuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Tela de Login conectada ao backend REST (Spring Boot 3.2 / JWT)
 */
public class LoginUI extends JFrame {

    // Campos de entrada de credenciais
    private JTextField txtEmail;
    private JPasswordField txtSenha;
    // Botões de ação
    private JButton btnLogin;
    private JButton btnLimpar;

    // Construtor: monta a tela e registra eventos
    public LoginUI() {
        inicializarComponentes();
        configurarEventos();
    }

    // Configuração visual da janela e dos componentes
    private void inicializarComponentes() {
        setTitle("THE CLUB - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 400);
        setLocationRelativeTo(null); // centraliza
        setResizable(false);

        // Painel principal com fundo em gradiente
        JPanel panelMain = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // Gradiente azul -> roxo
                GradientPaint gradiente = new GradientPaint(0, 0,
                        new Color(33, 150, 243),
                        getWidth(), getHeight(),
                        new Color(138, 43, 226));
                g2d.setPaint(gradiente);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelMain.setLayout(new GridBagLayout()); // layout flexível em grade
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // espaçamentos
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título da tela
        JLabel lblTitulo = new JLabel("THE CLUB - Jornal Universitário", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panelMain.add(lblTitulo, gbc);

        // Label E-mail
        gbc.gridy++;
        JLabel lblEmail = new JLabel("E-mail:");
        lblEmail.setForeground(Color.WHITE);
        panelMain.add(lblEmail, gbc);

        // Campo de texto para e-mail
        gbc.gridy++;
        txtEmail = new JTextField(25);
        panelMain.add(txtEmail, gbc);

        // Label Senha
        gbc.gridy++;
        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setForeground(Color.WHITE);
        panelMain.add(lblSenha, gbc);

        // Campo de senha
        gbc.gridy++;
        txtSenha = new JPasswordField(25);
        panelMain.add(txtSenha, gbc);

        // Painel de botões (Entrar / Limpar)
        gbc.gridy++;
        JPanel painelBotoes = new JPanel(new FlowLayout());
        painelBotoes.setOpaque(false); // sem fundo sólido

        btnLogin = new JButton("Entrar");
        btnLimpar = new JButton("Limpar");
        estilizarBotao(btnLogin, new Color(0, 123, 255));  // azul
        estilizarBotao(btnLimpar, new Color(220, 53, 69)); // vermelho

        painelBotoes.add(btnLogin);
        painelBotoes.add(btnLimpar);
        panelMain.add(painelBotoes, gbc);

        add(panelMain);

        // Foca o campo de e-mail após montar a tela
        SwingUtilities.invokeLater(() -> txtEmail.requestFocus());
    }

    // Aplica um estilo padrão aos botões
    private void estilizarBotao(JButton botao, Color cor) {
        botao.setBackground(cor);
        botao.setForeground(Color.WHITE);
        botao.setFont(new Font("Arial", Font.BOLD, 14));
        botao.setFocusPainted(false);
        botao.setPreferredSize(new Dimension(100, 35));
    }

    // Registra listeners dos botões e da tecla Enter no campo senha
    private void configurarEventos() {
        btnLogin.addActionListener(this::realizarLogin);
        btnLimpar.addActionListener(e -> {
            txtEmail.setText("");
            txtSenha.setText("");
        });
        // Permite apertar Enter na senha para logar
        txtSenha.addActionListener(this::realizarLogin);
    }

    /**
     * Realiza o login consultando o endpoint REST /api/auth/login
     */
    private void realizarLogin(ActionEvent e) {
        String email = txtEmail.getText().trim();
        String senha = new String(txtSenha.getPassword());

        // Validação básica de campos obrigatórios
        if (email.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Informe e-mail e senha.",
                    "Campos obrigatórios",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Abre conexão HTTP com o endpoint de login
            URL url = new URL("http://localhost:8081/api/auth/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true); // vamos enviar corpo na requisição

            // Monta JSON com email e senha usando Jackson
            ObjectMapper mapper = new ObjectMapper();
            String jsonInput = mapper.writeValueAsString(
                    Map.of("email", email, "senha", senha)
            );

            // Envia o corpo JSON no output stream da requisição
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                // Lê resposta JSON (contendo token, usuário etc.)
                Map<String, Object> response =
                        mapper.readValue(connection.getInputStream(), Map.class);

                String token = (String) response.get("token");

                // Mostra mensagem de sucesso (exibe só parte do token)
                JOptionPane.showMessageDialog(this,
                        "Login realizado com sucesso!\nToken: " +
                                token.substring(0, Math.min(token.length(), 20)) + "...",
                        "Autenticação JWT",
                        JOptionPane.INFORMATION_MESSAGE);

                // Cria um usuário mock para abrir o Dashboard (aqui ainda não usa o backend)
                Usuario usuarioMock = new Usuario();
                usuarioMock.setNome(email);
                usuarioMock.setTipo(TipoUsuario.valueOf("ADMIN"));

                // Abre a tela principal
                abrirDashboard(usuarioMock);

            } else if (responseCode == 401) {
                // Não autorizado: credenciais inválidas
                JOptionPane.showMessageDialog(this,
                        "Credenciais incorretas!",
                        "Erro de autenticação",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                // Outros erros HTTP
                JOptionPane.showMessageDialog(this,
                        "Erro: HTTP " + responseCode,
                        "Falha de Login",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            // Erro de rede ou outra exceção
            JOptionPane.showMessageDialog(this,
                    "Erro ao conectar ao servidor:\n" + ex.getMessage(),
                    "Falha",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre o painel principal (Dashboard) passando o usuário logado
     */
    private void abrirDashboard(Usuario usuario) {
        SwingUtilities.invokeLater(() -> {
            try {
                ui.DashboardUI dashboard = new ui.DashboardUI(usuario);
                dashboard.setVisible(true);
                dispose(); // fecha a tela de login
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao abrir painel: " + e.getMessage(),
                        "Erro do Sistema",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // Método main: inicializa LookAndFeel e exibe a tela de login
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Usa aparência padrão do sistema operacional
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new LoginUI().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
