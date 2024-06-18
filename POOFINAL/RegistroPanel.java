import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RegistroPanel extends JPanel {
    private JComboBox<String> naturezaComboBox;
    private JFormattedTextField dataField;
    private JFormattedTextField validadeField;
    private JTextField idMedField;
    private JTextField localField;
    private JTextField quantidadeField;
    private JLabel validadeLabel;
    private JPanel formPanel;
    private Estoque estoque;
    private CadastroMedicamentos cadastro;
    private Runnable onSaveListener;

    public RegistroPanel(CardLayout cardLayout, JPanel mainPanel, Estoque estoque, CadastroMedicamentos cadastro) {
        this.estoque = estoque;
        this.cadastro = cadastro;
        setLayout(new BorderLayout());

        JLabel registroLabel = new JLabel("Novo Registro de Estoque", SwingConstants.CENTER);
        registroLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(registroLabel, BorderLayout.NORTH);

        formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        naturezaComboBox = new JComboBox<>(new String[]{"Entrada", "Saída"});
        naturezaComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (naturezaComboBox.getSelectedItem().equals("Saída")) {
                    validadeLabel.setVisible(false);
                    validadeField.setVisible(false);
                } else {
                    validadeLabel.setVisible(true);
                    validadeField.setVisible(true);
                }
                revalidate();
                repaint();
            }
        });

        try {
            MaskFormatter dateMask = new MaskFormatter("##/##/####");
            dateMask.setPlaceholderCharacter('_');
            dataField = new JFormattedTextField(dateMask);
            validadeField = new JFormattedTextField(dateMask);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        idMedField = new JTextField();
        localField = new JTextField();
        quantidadeField = new JTextField();

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Natureza:"), gbc);

        gbc.gridx = 1;
        formPanel.add(naturezaComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Data:"), gbc);

        gbc.gridx = 1;
        formPanel.add(dataField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("ID Medicamento:"), gbc);

        gbc.gridx = 1;
        formPanel.add(idMedField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Local:"), gbc);

        gbc.gridx = 1;
        formPanel.add(localField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        validadeLabel = new JLabel("Validade:");
        formPanel.add(validadeLabel, gbc);

        gbc.gridx = 1;
        formPanel.add(validadeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Quantidade:"), gbc);

        gbc.gridx = 1;
        formPanel.add(quantidadeField, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel botoesRegistroPanel = new JPanel();
        JButton salvarButton = new JButton("Salvar");
        JButton voltarButton = new JButton("Voltar");

        salvarButton.addActionListener(e -> {
            if (salvarRegistro()) {
                if (onSaveListener != null) {
                    onSaveListener.run();
                }
            }
        });

        voltarButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "inicio");
            resetFields();
        });

        botoesRegistroPanel.add(salvarButton);
        botoesRegistroPanel.add(voltarButton);

        add(botoesRegistroPanel, BorderLayout.SOUTH);
    }

    public boolean salvarRegistro() {
        String estNat = (String) naturezaComboBox.getSelectedItem();
        String estData = dataField.getText();
        int medId;
        try {
            medId = Integer.parseInt(idMedField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(RegistroPanel.this, "ID do Medicamento deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (cadastro.getMedicamento(medId) == null) {
            JOptionPane.showMessageDialog(RegistroPanel.this, "ID desse Medicamento ainda não existe.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String estLocal = localField.getText();
        String estValid = validadeField.getText();
        int estQnt;
        try {
            estQnt = Integer.parseInt(quantidadeField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(RegistroPanel.this, "Quantidade deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!isValidDate(estData)) {
            JOptionPane.showMessageDialog(RegistroPanel.this, "Data deve estar no formato válido (DD/MM/AAAA).", "Erro na Data", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (isFutureDate(estData)) {
            JOptionPane.showMessageDialog(RegistroPanel.this, "Data não pode ser futura.", "Erro na Data", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if ("Entrada".equals(estNat)) {
            if (!isValidDate(estValid)) {
                JOptionPane.showMessageDialog(RegistroPanel.this, "Validade deve estar no formato válido (DD/MM/AAAA).", "Erro na Validade", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (isBeforeToday(estValid)) {
                JOptionPane.showMessageDialog(RegistroPanel.this, "Validade não pode ser anterior ao dia de hoje.", "Erro na Validade", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (!estoque.isLocalPermitido(estLocal, medId, estValid, estNat)) {
                JOptionPane.showMessageDialog(RegistroPanel.this, "Não é possível cadastrar entradas em locais que ainda possuem estoque com outro medicamento ou outra validade.", "Erro no Local", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        if ("Saída".equals(estNat)) {
            List<Registro> lotes = estoque.getEstoquePorLote(medId, estLocal, estValid);
            int quantidadeDisponivel = lotes.stream().mapToInt(Registro::getEstQnt).sum();

            if (estQnt > quantidadeDisponivel) {
                JOptionPane.showMessageDialog(RegistroPanel.this, "Quantidade não disponível no lote especificado. Registro não realizado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        Registro novoRegistro = new Registro(estData, medId, estNat, estLocal, estValid, estQnt);
        estoque.novoRegistro(novoRegistro, true);

        resetFields();
        return true;
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

    private boolean isBeforeToday(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date validDate = sdf.parse(date);
            Date today = new Date();
            if (validDate.before(sdf.parse(sdf.format(today)))) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isFutureDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date registroDate = sdf.parse(date);
            Date today = sdf.parse(sdf.format(new Date()));
            if (registroDate.after(today)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void resetFields() {
        naturezaComboBox.setSelectedIndex(0);
        dataField.setValue(null);
        idMedField.setText("");
        localField.setText("");
        validadeField.setValue(null);
        quantidadeField.setText("");
    }

    public void preencherCampos(String dataHoje, int medId, String local, int quantidade, String validade) {
        naturezaComboBox.setSelectedItem("Saída");
        dataField.setText(dataHoje);
        idMedField.setText(String.valueOf(medId));
        localField.setText(local);
        quantidadeField.setText(String.valueOf(quantidade));
        validadeField.setText(validade);
    }

    public void setOnSaveListener(Runnable onSaveListener) {
        this.onSaveListener = onSaveListener;
    }
}
