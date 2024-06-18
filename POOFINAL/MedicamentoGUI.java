import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MedicamentoGUI extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public MedicamentoGUI(CadastroMedicamentos cadastro, Estoque estoque) {
        setTitle("Cadastro de Medicamentos e Estoque");
        setSize(1200, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        InicioPanel inicioPanel = new InicioPanel(cardLayout, mainPanel, estoque, cadastro);
        CadastroPanel cadastroPanel = new CadastroPanel(cardLayout, mainPanel, cadastro);
        ListaPanel listaPanel = new ListaPanel(cardLayout, mainPanel, cadastro);
        RegistroPanel registroPanel = new RegistroPanel(cardLayout, mainPanel, estoque, cadastro);
        HistoricoPanel historicoPanel = new HistoricoPanel(cardLayout, mainPanel, estoque, cadastro);
        EstoqueAtualPanel estoqueAtualPanel = new EstoqueAtualPanel(cardLayout, mainPanel, estoque, cadastro);
        VencidosPanel vencidosPanel = new VencidosPanel(cardLayout, mainPanel, estoque, cadastro);
        FiltroMedicamentoPanel filtroMedicamentoPanel = new FiltroMedicamentoPanel(cardLayout, mainPanel, estoque, cadastro);
        RelatorioBuscaPanel relatorioBuscaPanel = new RelatorioBuscaPanel(cardLayout, mainPanel, estoque, cadastro);

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

    private void verificarMedicamentosVencidos(Estoque estoque) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Registro> registrosVencidos = estoque.verificarValidade();
        if (!registrosVencidos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Existem medicamentos vencidos no estoque.", "Atenção", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        CadastroMedicamentos cadastro = new CadastroMedicamentos();
        cadastro.uploadCSV("src/med_cadastro.csv");

        Estoque estoque = new Estoque(cadastro);
        estoque.uploadCSV("src/hist_estoque.csv");

        new MedicamentoGUI(cadastro, estoque);
    }
}
