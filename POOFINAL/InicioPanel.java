import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InicioPanel extends JPanel {
    private JButton mostrarVencidosButton;

    public InicioPanel(CardLayout cardLayout, JPanel mainPanel, Estoque estoque, CadastroMedicamentos cadastro) {
        setLayout(new BorderLayout());

        JLabel tituloLabel = new JLabel("Cadastro de Medicamentos e Estoque", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(tituloLabel, BorderLayout.NORTH);

        JPanel botoesPanel = new JPanel();
        botoesPanel.setLayout(new FlowLayout());

        JButton cadastrarMedButton = new JButton("Cadastrar Medicamentos");
        cadastrarMedButton.addActionListener(e -> cardLayout.show(mainPanel, "cadastro"));

        JButton mostrarMedButton = new JButton("Mostrar Medicamentos");
        mostrarMedButton.addActionListener(e -> {
            ((ListaPanel) mainPanel.getComponent(2)).loadMedicamentos();
            cardLayout.show(mainPanel, "lista");
        });

        JButton novoRegistroButton = new JButton("Novo Registro de Estoque");
        novoRegistroButton.addActionListener(e -> cardLayout.show(mainPanel, "registro"));

        JButton historicoButton = new JButton("Mostrar Histórico de Estoque");
        historicoButton.addActionListener(e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String hoje = sdf.format(new Date());

            ((HistoricoPanel) mainPanel.getComponent(4)).loadHistorico("01/01/1900", hoje);
            cardLayout.show(mainPanel, "historico");
        });

        JButton mostrarEstoqueButton = new JButton("Mostrar Estoque Atual");
        mostrarEstoqueButton.addActionListener(e -> {
            ((EstoqueAtualPanel) mainPanel.getComponent(5)).loadEstoqueAtual();
            cardLayout.show(mainPanel, "estoqueAtual");
        });

        JButton filtrarMedicamentoButton = new JButton("Filtrar por Medicamento");
        filtrarMedicamentoButton.addActionListener(e -> cardLayout.show(mainPanel, "filtroMedicamento"));

        botoesPanel.add(cadastrarMedButton);
        botoesPanel.add(mostrarMedButton);
        botoesPanel.add(novoRegistroButton);
        botoesPanel.add(historicoButton);
        botoesPanel.add(mostrarEstoqueButton);
        botoesPanel.add(filtrarMedicamentoButton);

        add(botoesPanel, BorderLayout.CENTER);

        mostrarVencidosButton = new JButton("Mostrar Medicamentos Vencidos");
        mostrarVencidosButton.addActionListener(e -> {
            ((VencidosPanel) mainPanel.getComponent(6)).loadVencidos();
            cardLayout.show(mainPanel, "vencidos");
        });

        add(mostrarVencidosButton, BorderLayout.SOUTH);

        atualizarAvisos(estoque);
    }

    public void atualizarAvisos(Estoque estoque) {
        List<Registro> registrosVencidos = estoque.verificarValidade();
        mostrarVencidosButton.setVisible(!registrosVencidos.isEmpty());
    }
}
