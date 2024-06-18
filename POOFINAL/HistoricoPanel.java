import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistoricoPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private Estoque estoque;
    private CadastroMedicamentos cadastro;
    private JFormattedTextField startDateField;
    private JFormattedTextField endDateField;

    public HistoricoPanel(CardLayout cardLayout, JPanel mainPanel, Estoque estoque, CadastroMedicamentos cadastro) {
        this.estoque = estoque;
        this.cadastro = cadastro;
        setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String hoje = sdf.format(new Date());
        String defaultStartDate = "01/01/1900";

        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            startDateField = new JFormattedTextField(dateMask);
            endDateField = new JFormattedTextField(dateMask);
            startDateField.setColumns(10);
            endDateField.setColumns(10);
            startDateField.setText(defaultStartDate);
            endDateField.setText(hoje);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JButton searchButton = new JButton("Buscar");

        searchButton.addActionListener(e -> {
            String startDate = startDateField.getText();
            String endDate = endDateField.getText();
            if (isValidDate(startDate) && isValidDate(endDate)) {
                if (isEndDateAfterStartDate(startDate, endDate)) {
                    loadHistorico(startDate, endDate);
                } else {
                    JOptionPane.showMessageDialog(this, "A data final deve ser maior ou igual à data inicial.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Datas inválidas. Por favor, use o formato DD/MM/YYYY.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        searchPanel.add(new JLabel("Data Inicial:"));
        searchPanel.add(startDateField);
        searchPanel.add(new JLabel("Data Final:"));
        searchPanel.add(endDateField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Natureza", "Data", "ID Medicamento", "Medicamento", "Validade", "Local", "Quantidade"});

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel botoesHistoricoPanel = new JPanel();
        JButton voltarHistoricoButton = new JButton("Voltar");
        voltarHistoricoButton.addActionListener(e -> cardLayout.show(mainPanel, "inicio"));
        botoesHistoricoPanel.add(voltarHistoricoButton);
        add(botoesHistoricoPanel, BorderLayout.SOUTH);
    }

    public void loadHistorico(String startDate, String endDate) {
        List<Registro> registros = estoque.getEstoque();
        tableModel.setRowCount(0); // Limpa a tabela

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date start = null, end = null;
        try {
            start = sdf.parse(startDate);
            end = sdf.parse(endDate);
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Datas inválidas. Por favor, use o formato DD/MM/YYYY.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (Registro reg : registros) {
            try {
                Date regDate = sdf.parse(reg.getEstData());
                if ((regDate.equals(start) || regDate.after(start)) && (regDate.equals(end) || regDate.before(end))) {
                    Medicamento med = cadastro.getMedicamento(reg.getMedId());
                    tableModel.addRow(new Object[]{
                            reg.getEstId(),
                            reg.getEstNat(),
                            reg.getEstData(),
                            reg.getMedId(),
                            med != null ? med.getMedNome() : "Desconhecido",
                            reg.getEstValid(),
                            reg.getEstLocal(),
                            reg.getEstQnt()
                    });
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty() || date.contains("_")) {
            return false;
        }
        String[] parts = date.split("/");
        if (parts.length != 3) {
            return false;
        }
        try {
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            if (day < 1 || day > 31 || month < 1 || month > 12 || parts[2].length() != 4) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isEndDateAfterStartDate(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            return !end.before(start);
        } catch (ParseException e) {
            return false;
        }
    }
}
