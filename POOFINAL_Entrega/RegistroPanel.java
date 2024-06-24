import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * In this panel, the user may add to or retrieve from the stock
 */
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

    /**
     * Constructs a panel for registering new stock entries or exits.
     *
     * @param cardLayout The layout manager for switching between panels.
     * @param mainPanel The main panel that contains all other panels.
     * @param estoque Reference to the stock management system.
     * @param cadastro Reference to the medication management system.
     */
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

        //specifies the location of each element in border layout
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

    /**
     * Validates and saves a new inventory record based on the user input - not only stock is updated, but also stocks' history
     *
     * @return true if the record is successfully saved, false otherwise.
     */
    public boolean salvarRegistro() {
        String estNat = (String) naturezaComboBox.getSelectedItem();
        String estData = dataField.getText();
        int medId;
        try {
            medId = Integer.parseInt(idMedField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID do Medicamento deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Medicamento medicamento = cadastro.getMedicamento(medId);
        if (medicamento == null) {
            JOptionPane.showMessageDialog(this, "ID desse Medicamento ainda não existe.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String estLocal = localField.getText();
        if (!isValidLocal(estLocal)) {
            JOptionPane.showMessageDialog(this, "O campo Local deve estar no formato @#### (letra maiúscula e 4 dígitos).", "Erro no Local", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (medicamento.isMedRefri() && !estLocal.startsWith("G")) {
            JOptionPane.showMessageDialog(this, "Medicamentos refrigerados devem ser armazenados em locais que comecem com 'G'.", "Erro no Local", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String estValid = validadeField.getText();
        int estQnt;
        try {
            estQnt = Integer.parseInt(quantidadeField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantidade deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!isValidDate(estData)) {
            JOptionPane.showMessageDialog(this, "Data deve estar no formato válido (DD/MM/AAAA).", "Erro na Data", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (isFutureDate(estData)) {
            JOptionPane.showMessageDialog(this, "Data não pode ser futura.", "Erro na Data", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if ("Entrada".equals(estNat)) {
            if (!isValidDate(estValid)) {
                JOptionPane.showMessageDialog(this, "Validade deve estar no formato válido (DD/MM/AAAA).", "Erro na Validade", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (isBeforeToday(estValid)) {
                JOptionPane.showMessageDialog(this, "Validade não pode ser anterior ao dia de hoje.", "Erro na Validade", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (!estoque.isLocalPermitido(estLocal, medId, estValid, estNat)) {
                JOptionPane.showMessageDialog(this, "Não é possível cadastrar entradas em locais que ainda possuem estoque com outro medicamento ou outra validade.", "Erro no Local", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        if ("Saída".equals(estNat)) {
            estValid = "";
        }

        Registro novoRegistro = new Registro(estData, medId, estNat, estLocal, estValid, estQnt);
        estoque.novoRegistro(novoRegistro, true); // Passa true para verbose

        // Clear fields after saving
        resetFields();
        return true;
    }

    //existing locals to from A to Z and from 0 to 999.
    private boolean isValidLocal(String local) {
        return local.matches("^[A-Z]\\d{4}$");
    }

    //slip date up to guarantee date inputted is valid
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

    //verifies if date inputted is before the actual date
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

    //similarly, guarantees the date inputted has not yet happened
    //foe example, it is used to not allow user to add an expireds' meds record to stock
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

    //makes page clear for the next usage.
    private void resetFields() {
        naturezaComboBox.setSelectedIndex(0);
        dataField.setValue(null);
        idMedField.setText("");
        localField.setText("");
        validadeField.setValue(null);
        quantidadeField.setText("");
    }
}
