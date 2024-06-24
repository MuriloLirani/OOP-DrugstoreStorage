import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Main GUI class for managing the medication and stock system using different panels.
 */
public class MedicamentoGUI extends JFrame {
    private JPanel mainPanel;`
    private CardLayout cardLayout;

    /**
     * Constructs the main GUI framework for the medication and inventory system.
     *
     * @param cadastro Reference to the medication registration system.
     * @param estoque Reference to the stock management system.
     */
    public MedicamentoGUI(CadastroMedicamentos cadastro, Estoque estoque) {
        setTitle("Cadastro de Medicamentos e Estoque");
        setSize(1200, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initializing all panels used in the application
        InicioPanel inicioPanel = new InicioPanel(cardLayout, mainPanel, estoque, cadastro);
        CadastroPanel cadastroPanel = new CadastroPanel(cardLayout, mainPanel, cadastro);
        ListaPanel listaPanel = new ListaPanel(cardLayout, mainPanel, cadastro);
        RegistroPanel registroPanel = new RegistroPanel(cardLayout, mainPanel, estoque, cadastro);
        HistoricoPanel historicoPanel = new HistoricoPanel(cardLayout, mainPanel, estoque, cadastro);
        EstoqueAtualPanel estoqueAtualPanel = new EstoqueAtualPanel(cardLayout, mainPanel, estoque, cadastro);
        VencidosPanel vencidosPanel = new VencidosPanel(cardLayout, mainPanel, estoque, cadastro);
        FiltroMedicamentoPanel filtroMedicamentoPanel = new FiltroMedicamentoPanel(cardLayout, mainPanel, estoque, cadastro);
        RelatorioBuscaPanel relatorioBuscaPanel = new RelatorioBuscaPanel(cardLayout, mainPanel, estoque, cadastro);

        // Adding panels to the main panel with identifiers
        mainPanel.add(inicioPanel, "inicio");
        mainPanel.add(cadastroPanel, "cadastro");
        mainPanel.add(listaPanel, "lista");
        mainPanel.add(registroPanel, "registro");
        mainPanel.add(historicoPanel, "historico");
        mainPanel.add(estoqueAtualPanel, "estoqueAtual");
        mainPanel.add(vencidosPanel, "vencidos");
        mainPanel.add(filtroMedicamentoPanel, "filtroMedicamento");
        mainPanel.add(relatorioBuscaPanel, "relatorioBusca");

        add(mainPanel);
        cardLayout.show(mainPanel, "inicio");

        setVisible(true);
        verificarMedicamentosVencidos(estoque);
    }

    /**
     * Checks for expired medications in the stock right when the program's started.
     * if there are any medications expired, the user is warned and can manage them properly - for example, by "throwing them away", that corresponds to deleting from the stock
     *
     * @param estoque The stock management system used to check for expired medications.
     */
    private void verificarMedicamentosVencidos(Estoque estoque) {
        try {
            Thread.sleep(500); // Delay added to simulate the checking process
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Registro> registrosVencidos = estoque.verificarValidade();
        if (!registrosVencidos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Existem medicamentos vencidos no estoque.", "Atenção", JOptionPane.WARNING_MESSAGE);
        }
    }

    //main method. Uploads CSV with med records and initializes GUI
    public static void main(String[] args) {
        CadastroMedicamentos cadastro = new CadastroMedicamentos();
        cadastro.uploadCSV("src/med_cadastro.csv");

        Estoque estoque = new Estoque(cadastro);
        estoque.uploadCSV("src/hist_estoque.csv");

        new MedicamentoGUI(cadastro, estoque);
    }
}
