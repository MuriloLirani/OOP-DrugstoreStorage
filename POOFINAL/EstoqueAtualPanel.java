import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class EstoqueAtualPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private Estoque estoque;
    private CadastroMedicamentos cadastro;
    private JComboBox<String> viewComboBox;

    public EstoqueAtualPanel(CardLayout cardLayout, JPanel mainPanel, Estoque estoque, CadastroMedicamentos cadastro) {
        this.estoque = estoque;
        this.cadastro = cadastro;
        setLayout(new BorderLayout());

        JLabel tituloLabel = new JLabel("Estoque Atual", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(tituloLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new FlowLayout());
        viewComboBox = new JComboBox<>(new String[]{"Por Medicamento", "Por Local"});
        viewComboBox.addActionListener(e -> loadEstoqueAtual());

        optionsPanel.add(new JLabel("Ver Estoque:"));
        optionsPanel.add(viewComboBox);
        add(optionsPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel botoesPanel = new JPanel();
        JButton voltarButton = new JButton("Voltar");
        voltarButton.addActionListener(e -> cardLayout.show(mainPanel, "inicio"));
        botoesPanel.add(voltarButton);
        add(botoesPanel, BorderLayout.SOUTH);

        loadEstoqueAtual();
    }

    public void loadEstoqueAtual() {
        tableModel.setRowCount(0);

        if (viewComboBox.getSelectedItem().equals("Por Medicamento")) {
            tableModel.setColumnIdentifiers(new String[]{"ID Medicamento", "Nome", "Quantidade"});

            Map<Integer, Integer> estoquePorMedicamento = estoque.getEstoqueAtualPorMedicamento();

            for (Map.Entry<Integer, Integer> entry : estoquePorMedicamento.entrySet()) {
                int medId = entry.getKey();
                int quantidade = entry.getValue();
                Medicamento med = cadastro.getMedicamento(medId);
                String medNome = med != null ? med.getMedNome() : "Desconhecido";

                if (quantidade > 0) {
                    tableModel.addRow(new Object[]{medId, medNome, quantidade});
                }
            }
        } else if (viewComboBox.getSelectedItem().equals("Por Local")) {
            tableModel.setColumnIdentifiers(new String[]{"Local", "ID Medicamento", "Nome", "Quantidade", "Validade"});

            Map<String, Map<Integer, Integer>> estoquePorLocal = estoque.getEstoqueAtualPorLocal();
            Map<String, Map<Integer, String>> validadePorLocal = estoque.getValidadePorLocal();

            for (String local : estoquePorLocal.keySet()) {
                for (Integer medId : estoquePorLocal.get(local).keySet()) {
                    Medicamento med = cadastro.getMedicamento(medId);
                    String medNome = med != null ? med.getMedNome() : "Desconhecido";
                    int quantidade = estoquePorLocal.get(local).get(medId);
                    String validade = validadePorLocal.get(local).get(medId);

                    if (quantidade > 0) {
                        tableModel.addRow(new Object[]{local, medId, medNome, quantidade, validade});
                    }
                }
            }
        }
    }
}
