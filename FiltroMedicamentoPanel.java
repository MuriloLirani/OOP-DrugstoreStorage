import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

/**
 * A JPanel for filtering and selecting medications from a list.
 */
public class FiltroMedicamentoPanel extends JPanel {
    private JTextField searchField;
    private JTable medicamentosTable;
    private DefaultTableModel medicamentosTableModel;
    private JTable selecionadosTable;
    private DefaultTableModel selecionadosTableModel;
    private Estoque estoque;
    private CadastroMedicamentos cadastro;
    private Set<Integer> selecionadosIds;
    private TableRowSorter<DefaultTableModel> sorter;

    /**
     * Constructs the panel for filtering medications.
     * @param cardLayout The layout manager for the parent panel.
     * @param mainPanel The main container panel.
     * @param estoque The stock management object.
     * @param cadastro The medication registration system.
     */
    public FiltroMedicamentoPanel(CardLayout cardLayout, JPanel mainPanel, Estoque estoque, CadastroMedicamentos cadastro) {
        this.estoque = estoque;
        this.cadastro = cadastro;
        this.selecionadosIds = new HashSet<>();
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));

        // Left panel for filtering and displaying medications
        JPanel leftPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterMedicamentos(); }
            public void removeUpdate(DocumentEvent e) { filterMedicamentos(); }
            public void changedUpdate(DocumentEvent e) { filterMedicamentos(); }
        });

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Buscar Medicamento:"), BorderLayout.NORTH);
        searchPanel.add(searchField, BorderLayout.CENTER);
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        medicamentosTableModel = new DefaultTableModel(new Object[]{"ID", "Nome", "Selecionar"}, 0) {
            public Class<?> getColumnClass(int column) { return column == 2 ? Boolean.class : String.class; }
            public boolean isCellEditable(int row, int column) { return column == 2; }
        };
        medicamentosTable = new JTable(medicamentosTableModel);
        medicamentosTableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 2) {
                int row = e.getFirstRow();
                boolean isSelected = (boolean) medicamentosTableModel.getValueAt(row, 2);
                int medId = (int) medicamentosTableModel.getValueAt(row, 0);
                if (isSelected) { selecionadosIds.add(medId); }
                else { selecionadosIds.remove(medId); }
                atualizarSelecionados();
            }
        });
        JScrollPane leftScrollPane = new JScrollPane(medicamentosTable);
        leftPanel.add(leftScrollPane, BorderLayout.CENTER);

        // Right panel for displaying selected medications
        JPanel rightPanel = new JPanel(new BorderLayout());
        selecionadosTableModel = new DefaultTableModel(new Object[]{"ID", "Nome", "Disponível", "Quantidade"}, 0) {
            public Class<?> getColumnClass(int column) { return column == 3 ? Integer.class : String.class; }
            public boolean isCellEditable(int row, int column) { return column == 3; }
        };
        selecionadosTable = new JTable(selecionadosTableModel);
        selecionadosTableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 3) {
                int row = e.getFirstRow();
                int quantity = (int) selecionadosTableModel.getValueAt(row, 3);
                int available = (int) selecionadosTableModel.getValueAt(row, 2);
                if (quantity < 0 || quantity > available) {
                    selecionadosTableModel.setValueAt(0, row, 3);
                }
            }
        });
        sorter = new TableRowSorter<>(selecionadosTableModel);
        sorter.setComparator(0, Comparator.naturalOrder());
        selecionadosTable.setRowSorter(sorter);
        JScrollPane rightScrollPane = new JScrollPane(selecionadosTable);
        rightPanel.add(new JLabel("Medicamentos Selecionados:"), BorderLayout.NORTH);
        rightPanel.add(rightScrollPane, BorderLayout.CENTER);

        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel with action buttons
        JPanel botoesPanel = new JPanel();
        JButton voltarButton = new JButton("Voltar");
        voltarButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "inicio");
            resetSelections();
        });
        botoesPanel.add(voltarButton);

        JButton gerarRelatorioButton = new JButton("Gerar relatório de busca");
        gerarRelatorioButton.addActionListener(e -> {
            if (validarQuantidades()) {
                RelatorioBuscaPanel relatorioPanel = (RelatorioBuscaPanel) mainPanel.getComponent(8);
                relatorioPanel.loadRelatorio(selecionadosIds, estoque, cadastro, selecionadosTable);
                cardLayout.show(mainPanel, "relatorioBusca");
                resetSelections();
            } else {
                JOptionPane.showMessageDialog(this, "Quantidades não podem ser zero.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        botoesPanel.add(gerarRelatorioButton);
        add(botoesPanel, BorderLayout.SOUTH);

        // Listener to update table when the panel becomes visible
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                loadAllMedicamentos();
            }
        });

        // Load all medications into the left table
        loadAllMedicamentos();
    }

    private void loadAllMedicamentos() {
        medicamentosTableModel.setRowCount(0);
        cadastro.getMedicamentos().forEach(med -> {
            boolean isSelected = selecionadosIds.contains(med.getMedId());
            medicamentosTableModel.addRow(new Object[]{med.getMedId(), med.getMedNome(), isSelected});
        });
    }

    private void filterMedicamentos() {
        String filterText = searchField.getText().toLowerCase();
        medicamentosTableModel.setRowCount(0);
        cadastro.getMedicamentos().stream()
                .filter(med -> med.getMedNome().toLowerCase().contains(filterText))
                .forEach(med -> {
                    boolean isSelected = selecionadosIds.contains(med.getMedId());
                    medicamentosTableModel.addRow(new Object[]{med.getMedId(), med.getMedNome(), isSelected});
                });
    }

    private void atualizarSelecionados() {
        selecionadosTableModel.setRowCount(0);
        Map<Integer, Integer> estoqueAtualPorMedicamento = estoque.getEstoqueAtualPorMedicamento();
        selecionadosIds.forEach(medId -> {
            Medicamento med = cadastro.getMedicamento(medId);
            if (med != null) {
                boolean alreadyExists = false;
                for (int i = 0; i < selecionadosTableModel.getRowCount(); i++) {
                    if ((int) selecionadosTableModel.getValueAt(i, 0) == medId) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) {
                    int quantidadeDisponivel = estoqueAtualPorMedicamento.getOrDefault(medId, 0);
                    selecionadosTableModel.addRow(new Object[]{med.getMedId(), med.getMedNome(), quantidadeDisponivel, 0});
                }
            }
        });
        sorter.sort(); // Sort the table after updates
    }

    private boolean validarQuantidades() {
        return selecionadosTableModel.getDataVector().stream()
                .noneMatch(row -> (int) row.get(3) == 0);
    }

    private void resetSelections() {
        for (int i = 0; i < medicamentosTableModel.getRowCount(); i++) {
            medicamentosTableModel.setValueAt(false, i, 2);
        }
        selecionadosIds.clear();
        atualizarSelecionados();
    }
}
