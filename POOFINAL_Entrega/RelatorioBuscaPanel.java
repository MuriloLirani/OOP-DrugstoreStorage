import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.print.PrinterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Panel that provides an interface for generating and handling medication reports
 * based on search criteria.
 */
public class RelatorioBuscaPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private Estoque estoque;
    private CadastroMedicamentos cadastro;
    private Set<Integer> selecionadosIds;
    private JTable selecionadosTable;

    /**
     * Constructs a new RelatorioBuscaPanel.
     *
     * @param cardLayout The layout manager that controls the panel transitions.
     * @param mainPanel  The main container panel for subpanels.
     * @param estoque    The inventory system managing stock.
     * @param cadastro   The registration system for medications.
     */
    public RelatorioBuscaPanel(CardLayout cardLayout, JPanel mainPanel, Estoque estoque, CadastroMedicamentos cadastro) {
        this.estoque = estoque;
        this.cadastro = cadastro;
        setLayout(new BorderLayout());

        JLabel tituloLabel = new JLabel("Relatório de Busca", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(tituloLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only the action column is editable
            }
        };
        tableModel.setColumnIdentifiers(new String[]{"Local", "ID Medicamento", "Nome", "Quantidade a Retirar", "Validade", "Ação"});
        table = new JTable(tableModel);
        table.getColumn("Ação").setCellRenderer(new ButtonRenderer());
        table.getColumn("Ação").setCellEditor(new ButtonEditor(new JCheckBox()));
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel botoesPanel = new JPanel();
        JButton voltarButton = new JButton("Voltar");
        voltarButton.addActionListener(e -> cardLayout.show(mainPanel, "inicio"));
        botoesPanel.add(voltarButton);

        JButton imprimirButton = new JButton("Imprimir");
        imprimirButton.addActionListener(e -> imprimirTabela());
        botoesPanel.add(imprimirButton);

        add(botoesPanel, BorderLayout.SOUTH);
    }

    /**
     * Loads the report based on the selected medication IDs.
     *
     * @param selecionadosIds   The set of selected medication IDs for the report.
     * @param estoque           The inventory system.
     * @param cadastro          The medication registration system.
     * @param selecionadosTable The table containing selected medications.
     */
    public void loadRelatorio(Set<Integer> selecionadosIds, Estoque estoque, CadastroMedicamentos cadastro, JTable selecionadosTable) {
        this.selecionadosIds = selecionadosIds;
        this.selecionadosTable = selecionadosTable;
        tableModel.setRowCount(0); // Clears the table

        Map<Integer, Integer> demanda = new HashMap<>();
        for (int i = 0; i < selecionadosTable.getRowCount(); i++) {
            int medId = (int) selecionadosTable.getValueAt(i, 0);
            int quantidade = (int) selecionadosTable.getValueAt(i, 3);
            demanda.put(medId, quantidade);
        }

        Map<String, Map<Integer, Integer>> estoquePorLocal = estoque.getEstoqueAtualPorLocal();
        Map<String, Map<Integer, String>> validadePorLocal = estoque.getValidadePorLocal();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (Integer medId : selecionadosIds) {
            if (demanda.containsKey(medId)) {
                Medicamento med = cadastro.getMedicamento(medId);
                String medNome = med != null ? med.getMedNome() : "Desconhecido";
                int quantidadeNecessaria = demanda.get(medId);

                List<RegistroOrdenado> registrosOrdenados = new ArrayList<>();
                for (String local : estoquePorLocal.keySet()) {
                    if (estoquePorLocal.get(local).containsKey(medId)) {
                        int quantidade = estoquePorLocal.get(local).get(medId);
                        String validade = validadePorLocal.get(local).get(medId);
                        try {
                            Date validadeDate = sdf.parse(validade);
                            registrosOrdenados.add(new RegistroOrdenado(local, medId, medNome, quantidade, validadeDate));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                registrosOrdenados.sort(Comparator.comparing(RegistroOrdenado::getValidade));

                for (RegistroOrdenado registro : registrosOrdenados) {
                    if (quantidadeNecessaria <= 0) {
                        break;
                    }
                    int quantidadeRetirada = Math.min(quantidadeNecessaria, registro.getQuantidade());
                    if (quantidadeRetirada > 0) {
                        tableModel.addRow(new Object[]{registro.getLocal(), medId, medNome, quantidadeRetirada, sdf.format(registro.getValidade()), "Baixa no Estoque"});
                        quantidadeNecessaria -= quantidadeRetirada;
                    }
                }
            }
        }
    }

    /**
     * Initiates printing of the current table.
     */
    private void imprimirTabela() {
        try {
            boolean complete = table.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "Impressão bem-sucedida", "Resultado da Impressão", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Impressão cancelada", "Resultado da Impressão", JOptionPane.WARNING_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Erro ao imprimir: " + e.getMessage(), "Erro de Impressão", JOptionPane.ERROR_MESSAGE);
        }
    }

    //this class stores sorted information abt records
    class RegistroOrdenado {
        private String local;
        private int medId;
        private String medNome;
        private int quantidade;
        private Date validade;

        public RegistroOrdenado(String local, int medId, String medNome, int quantidade, Date validade) {
            this.local = local;
            this.medId = medId;
            this.medNome = medNome;
            this.quantidade = quantidade;
            this.validade = validade;
        }

        public String getLocal() {
            return local;
        }

        public int getMedId() {
            return medId;
        }

        public String getMedNome() {
            return medNome;
        }

        public int getQuantidade() {
            return quantidade;
        }

        public Date getValidade() {
            return validade;
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

                // Criar um novo registro de saída
                Registro novoRegistro = new Registro(dataHoje, medId, "Saída", local, validade, quantidade);
                estoque.novoRegistro(novoRegistro, true); // Passa true para verbose
                tableModel.removeRow(row); // Remove a linha da tabela
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
