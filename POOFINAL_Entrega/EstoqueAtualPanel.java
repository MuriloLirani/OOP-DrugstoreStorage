import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

/**
 * A JPanel that displays current stock situation in a table, either by medication or by location.
 */
public class EstoqueAtualPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private Estoque estoque;
    private CadastroMedicamentos cadastro;
    private JComboBox<String> viewComboBox;

    /**
     * Constructor of EstoqueAtualPanel.
     * @param cardLayout The layout manager for the parent panel.
     * @param mainPanel The main panel that holds this panel.
     * @param estoque The stock management object.
     * @param cadastro The medication registration system.
     */
    public EstoqueAtualPanel(CardLayout cardLayout, JPanel mainPanel, Estoque estoque, CadastroMedicamentos cadastro) {
        this.estoque = estoque;
        this.cadastro = cadastro;
        setLayout(new BorderLayout());

        // Setup the title label
        JLabel tituloLabel = new JLabel("Estoque Atual", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(tituloLabel, BorderLayout.NORTH);

        // Setup the options panel with a combo box for selecting criterea to be shown
        JPanel optionsPanel = new JPanel(new FlowLayout());
        viewComboBox = new JComboBox<>(new String[]{"Por Medicamento", "Por Local"});
        viewComboBox.addActionListener(e -> loadEstoqueAtual());

        optionsPanel.add(new JLabel("Ver Estoque:"));
        optionsPanel.add(viewComboBox);
        add(optionsPanel, BorderLayout.NORTH);

        // Setup the table to display stock details
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Setup the buttons panel with a back button
        JPanel botoesPanel = new JPanel();
        JButton voltarButton = new JButton("Voltar");
        voltarButton.addActionListener(e -> cardLayout.show(mainPanel, "inicio"));
        botoesPanel.add(voltarButton);
        add(botoesPanel, BorderLayout.SOUTH);

        // Initially load the stock details
        loadEstoqueAtual();
    }

    /**
     * Loads the current stock details into the table based on the selected criterea.
     */
    public void loadEstoqueAtual() {
        tableModel.setRowCount(0); // Clears the table

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
            tableModel.setColumnIdentifiers(new String[]{"ID Medicamento", "Local", "Nome", "Quantidade", "Validade"});

            Map<String, Map<Integer, Integer>> estoquePorLocal = estoque.getEstoqueAtualPorLocal();
            Map<String, Map<Integer, String>> validadePorLocal = estoque.getValidadePorLocal();

            for (String local : estoquePorLocal.keySet()) {
                for (Integer medId : estoquePorLocal.get(local).keySet()) {
                    Medicamento med = cadastro.getMedicamento(medId);
                    String medNome = med != null ? med.getMedNome() : "Desconhecido";
                    int quantidade = estoquePorLocal.get(local).get(medId);
                    String validade = validadePorLocal.get(local).get(medId);

                    if (quantidade > 0) {
                        tableModel.addRow(new Object[]{medId, local, medNome, quantidade, validade});
                    }
                }
            }
        }

        // Sort the table
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setComparator(0, Comparator.comparingInt(o -> (Integer) o));

        // Set sort keys based on the selected view mode
        if (viewComboBox.getSelectedItem().equals("Por Medicamento")) {
            sorter.setSortKeys(Arrays.asList(
                    new RowSorter.SortKey(0, SortOrder.ASCENDING), // Sort by Medication ID
                    new RowSorter.SortKey(1, SortOrder.ASCENDING)  // Sort by Name
            ));
        } else if (viewComboBox.getSelectedItem().equals("Por Local")) {
            sorter.setSortKeys(Arrays.asList(
                    new RowSorter.SortKey(0, SortOrder.ASCENDING), // Sort by Medication ID
                    new RowSorter.SortKey(1, SortOrder.ASCENDING)  // Sort by Location
            ));
        }
    }
}
