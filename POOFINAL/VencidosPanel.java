import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VencidosPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private Estoque estoque;
    private CadastroMedicamentos cadastro;

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
                return column == 5;
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
        voltarButton.addActionListener(e -> cardLayout.show(mainPanel, "inicio"));
        botoesPanel.add(voltarButton);
        add(botoesPanel, BorderLayout.SOUTH);

        loadVencidos();
    }

    public void loadVencidos() {
        tableModel.setRowCount(0);
        List<Registro> registrosVencidos = estoque.verificarValidade();

        for (Registro reg : registrosVencidos) {
            Medicamento med = cadastro.getMedicamento(reg.getMedId());
            String medNome = med != null ? med.getMedNome() : "Desconhecido";
            tableModel.addRow(new Object[]{reg.getEstLocal(), reg.getMedId(), medNome, reg.getEstQnt(), reg.getEstValid(), "Baixa no Estoque"});
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

    class ButtonEditor extends DefaultCellEditor {
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            JButton button = new JButton(label);
            button.addActionListener(e -> {
                fireEditingStopped();
                int medId = (int) table.getValueAt(row, 1);
                String local = (String) table.getValueAt(row, 0);
                String validade = "";
                int quantidade = (int) table.getValueAt(row, 3);
                Date hoje = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String dataHoje = sdf.format(hoje);

                Registro novoRegistro = new Registro(dataHoje, medId, "Saída", local, validade, quantidade);
                estoque.novoRegistro(novoRegistro, true);
                loadVencidos();
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
