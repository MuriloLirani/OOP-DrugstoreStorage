import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.ParseException;

/**
 * This panel displays a list of expired medications and allows the user to manage them.
 */
public class VencidosPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private Estoque estoque;
    private CadastroMedicamentos cadastro;

    /**
     * Constructs the VencidosPanel which displays expired medications.
     *
     * @param cardLayout The layout manager for switching between panels.
     * @param mainPanel The main panel that contains all subpanels.
     * @param estoque Reference to the stock management system.
     * @param cadastro Reference to the medication registration system.
     */
    public VencidosPanel(CardLayout cardLayout, JPanel mainPanel, Estoque estoque, CadastroMedicamentos cadastro) {
        this.estoque = estoque;
        this.cadastro = cadastro;
        setLayout(new BorderLayout());

        JLabel tituloLabel = new JLabel("Medicamentos Vencidos", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(tituloLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only the action column is editable
            }
        };
        tableModel.setColumnIdentifiers(new String[]{"Local", "ID Medicamento", "Nome", "Quantidade", "Validade", "Ação"});
        table = new JTable(tableModel);
        table.getColumn("Ação").setCellRenderer(new ButtonRenderer());
        table.getColumn("Ação").setCellEditor(new ButtonEditor(new JCheckBox()));
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel botoesPanel = new JPanel();
        JButton voltarButton = new JButton("Voltar");
        voltarButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "inicio");
            ((InicioPanel) mainPanel.getComponent(0)).atualizarAvisos(estoque); // Updates notifications when returning to the main menu
        });
        botoesPanel.add(voltarButton);
        add(botoesPanel, BorderLayout.SOUTH);

        loadVencidos();
    }

    /**
     * Loads expired medications into the table based on the current stock and expiration dates.
     */
    public void loadVencidos() {
        tableModel.setRowCount(0); // Clears the table
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date today = new Date();

        Map<String, Map<Integer, Integer>> estoquePorLocal = estoque.getEstoqueAtualPorLocal();
        Map<String, Map<Integer, String>> validadePorLocal = estoque.getValidadePorLocal();

        for (String local : estoquePorLocal.keySet()) {
            for (Integer medId : estoquePorLocal.get(local).keySet()) {
                Medicamento med = cadastro.getMedicamento(medId);
                String medNome = med != null ? med.getMedNome() : "Desconhecido";
                int quantidade = estoquePorLocal.get(local).get(medId);
                String validadeStr = validadePorLocal.get(local).get(medId);

                try {
                    Date validade = sdf.parse(validadeStr);
                    if (validade.before(today) && quantidade > 0) {
                        tableModel.addRow(new Object[]{local, medId, medNome, quantidade, validadeStr, "Baixa no Estoque"});
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    /**
     * Manages editing actions within a table cell, specifically for executing medication stock decrement of expired meds.
     */
    class ButtonEditor extends DefaultCellEditor {
        private String label;
        private boolean isPushed;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            label = (value == null) ? "" : value.toString();
            JButton button = new JButton(label);
            button.addActionListener(e -> {
                fireEditingStopped();
                int medId = (int) table.getValueAt(row, 1);
                String local = (String) table.getValueAt(row, 0);
                String validade = (String) table.getValueAt(row, 4);
                int quantidade = (int) table.getValueAt(row, 3);
                Date hoje = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String dataHoje = sdf.format(hoje);

                // Create a new exit record
                Registro novoRegistro = new Registro(dataHoje, medId, "Saída", local, validade, quantidade);
                estoque.novoRegistro(novoRegistro, true); // Pass true for verbose
                loadVencidos(); // Refresh the table after stock reduction
            });
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}
