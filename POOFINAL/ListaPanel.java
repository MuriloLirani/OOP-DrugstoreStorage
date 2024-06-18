import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;

public class ListaPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private CadastroMedicamentos cadastro;
    private JTextField searchField;
    private JComboBox<String> searchCriteriaComboBox;

    public ListaPanel(CardLayout cardLayout, JPanel mainPanel, CadastroMedicamentos cadastro) {
        this.cadastro = cadastro;
        setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        searchCriteriaComboBox = new JComboBox<>(new String[]{"Nome", "Princípio Ativo", "Função", "Todos"});
        JButton searchButton = new JButton("Buscar");

        searchButton.addActionListener(e -> searchMedicamentos());

        searchPanel.add(new JLabel("Buscar:"));
        searchPanel.add(searchField);
        searchPanel.add(searchCriteriaComboBox);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Nome", "Princípios Ativos", "Refrigerado", "Função", "Risco", "Dosagens", "Unidade", "Quant. Doses", "Marca", "Envase"});

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        setColumnWidths();

        JPanel botoesListaPanel = new JPanel();
        JButton voltarListaButton = new JButton("Voltar");
        voltarListaButton.addActionListener(e -> cardLayout.show(mainPanel, "inicio"));
        botoesListaPanel.add(voltarListaButton);
        add(botoesListaPanel, BorderLayout.SOUTH);
    }

    private void setColumnWidths() {
        table.getColumnModel().getColumn(0).setPreferredWidth(40); // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(400); // Nome
        table.getColumnModel().getColumn(2).setPreferredWidth(280); // Princípios Ativos
        table.getColumnModel().getColumn(3).setPreferredWidth(40); // Refrigerado
        table.getColumnModel().getColumn(4).setPreferredWidth(180); // Função
        table.getColumnModel().getColumn(5).setPreferredWidth(80); // Risco
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Dosagens
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Unidade
        table.getColumnModel().getColumn(8).setPreferredWidth(70); // Quant. Doses
        table.getColumnModel().getColumn(9).setPreferredWidth(150); // Marca
        table.getColumnModel().getColumn(10).setPreferredWidth(140); // Envase
    }

    public void loadMedicamentos() {
        tableModel.setRowCount(0);
        List<Medicamento> medicamentos = cadastro.getMedicamentos();
        for (Medicamento med : medicamentos) {
            tableModel.addRow(new Object[]{
                    med.getMedId(),
                    med.getMedNome(),
                    med.getPrincAtvsAsString(),
                    med.isMedRefri() ? "Sim" : "Não",
                    med.getMedFunc(),
                    med.getMedRisco(),
                    med.getDosagensAsString(),
                    med.getMedUndDosag(),
                    med.getMedQntDoses(),
                    med.getMedMarca(),
                    med.getMedEnvase()
            });
        }
    }

    private void searchMedicamentos() {
        String searchText = searchField.getText().toLowerCase();
        String searchCriteria = searchCriteriaComboBox.getSelectedItem().toString().toLowerCase();

        List<Medicamento> medicamentos = cadastro.getMedicamentos();
        List<Medicamento> filteredMedicamentos = medicamentos.stream()
                .filter(med -> {
                    switch (searchCriteria) {
                        case "nome":
                            return med.getMedNome().toLowerCase().contains(searchText);
                        case "princípio ativo":
                            return med.getPrincAtvsAsString().toLowerCase().contains(searchText);
                        case "função":
                            return med.getMedFunc().toLowerCase().contains(searchText);
                        case "todos":
                            return med.getMedNome().toLowerCase().contains(searchText) ||
                                    med.getPrincAtvsAsString().toLowerCase().contains(searchText) ||
                                    med.getMedFunc().toLowerCase().contains(searchText);
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());

        tableModel.setRowCount(0);
        for (Medicamento med : filteredMedicamentos) {
            tableModel.addRow(new Object[]{
                    med.getMedId(),
                    med.getMedNome(),
                    med.getPrincAtvsAsString(),
                    med.isMedRefri() ? "Sim" : "Não",
                    med.getMedFunc(),
                    med.getMedRisco(),
                    med.getDosagensAsString(),
                    med.getMedUndDosag(),
                    med.getMedQntDoses(),
                    med.getMedMarca(),
                    med.getMedEnvase()
            });
        }
    }
}