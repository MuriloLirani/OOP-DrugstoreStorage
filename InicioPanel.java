import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Date;

/**
 * This panel serves as the main menu for the application, offering navigation options to other panels
 * it's the first one to show up.
 */
public class InicioPanel extends JPanel {
    private JButton mostrarVencidosButton;

    /**
     * Constructs the initial panel that serves as the application's main menu.
     *
     * @param cardLayout The layout manager that allows switching between panels.
     * @param mainPanel The main container holding all the panels.
     * @param estoque Reference to the stock management system.
     * @param cadastro Reference to the medication registration system.
     */
    public InicioPanel(CardLayout cardLayout, JPanel mainPanel, Estoque estoque, CadastroMedicamentos cadastro) {
        setLayout(new BorderLayout());

        // Title label
        JLabel tituloLabel = new JLabel("Cadastro de Medicamentos e Estoque", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(tituloLabel, BorderLayout.NORTH);

        // Button panel for navigation
        JPanel botoesPanel = new JPanel();
        botoesPanel.setLayout(new FlowLayout());

        //buttons & listeners
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

        //buttons' added to the menu
        botoesPanel.add(cadastrarMedButton);
        botoesPanel.add(mostrarMedButton);
        botoesPanel.add(novoRegistroButton);
        botoesPanel.add(historicoButton);
        botoesPanel.add(mostrarEstoqueButton);
        botoesPanel.add(filtrarMedicamentoButton);

        add(botoesPanel, BorderLayout.CENTER);

        // Button to show expired medications
        mostrarVencidosButton = new JButton("Mostrar Medicamentos Vencidos");
        mostrarVencidosButton.addActionListener(e -> {
            ((VencidosPanel) mainPanel.getComponent(6)).loadVencidos();
            cardLayout.show(mainPanel, "vencidos");
        });

        add(mostrarVencidosButton, BorderLayout.SOUTH);

        // Check and display a warning if there are expired medications
        atualizarAvisos(estoque);
    }

    /**
     * Updates the visibility of the button based on whether there are expired medications in stock.
     *
     * @param estoque The stock management system used to check for expired medications.
     */
    public void atualizarAvisos(Estoque estoque) {
        List<Registro> registrosVencidos = estoque.verificarValidade();
        boolean hasVencidos = registrosVencidos.stream().anyMatch(reg -> {
            int saldo = estoque.getEstoqueAtualPorLocal().getOrDefault(reg.getEstLocal(), new HashMap<>()).getOrDefault(reg.getMedId(), 0);
            return saldo > 0;
        });
        mostrarVencidosButton.setVisible(hasVencidos);
    }
}
