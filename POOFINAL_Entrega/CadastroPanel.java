import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * Panel for registering new medications in the stock control system.
 */
public class CadastroPanel extends JPanel {
    // Regex pattern to restrict letters, numbers, spaces, semicolons, and percent signs only.
    private static final Pattern STRING_PATTERN = Pattern.compile("^[a-zA-Z0-9 ;%]+$");

    /**
     * Constructs a CadastroPanel for medication registration.
     *
     * @param cardLayout   The CardLayout managing different panels.
     * @param mainPanel    The main panel containing different cards.
     * @param cadastro     The registration handler for medications.
     */
    public CadastroPanel(CardLayout cardLayout, JPanel mainPanel, CadastroMedicamentos cadastro) {
        setLayout(new BorderLayout());

        // Setup header label for the panel.
        JLabel cadastroLabel = new JLabel("Cadastro de Medicamento", SwingConstants.CENTER);
        cadastroLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(cadastroLabel, BorderLayout.NORTH);

        // Setup form panel with a grid layout for input fields.
        JPanel formPanel = new JPanel(new GridLayout(11, 2));

        // Fields for entering medication data.
        JTextField nomeField = new JTextField();
        JTextField princAtvsField = new JTextField();
        JCheckBox refriCheckBox = new JCheckBox();
        JTextField funcField = new JTextField();
        JComboBox<String> tarjaComboBox = new JComboBox<>(new String[]{"Branca", "Vermelha", "Preta"});
        JTextField dosagField = new JTextField();
        JTextField undDosagField = new JTextField();
        JTextField qntDosesField = new JTextField();
        JTextField marcaField = new JTextField();
        JTextField envaseField = new JTextField();

        // Adding labels and fields to the panel.
        formPanel.add(new JLabel("Nome:"));
        formPanel.add(nomeField);
        formPanel.add(new JLabel("Princípios Ativos (separados por ';'):"));
        formPanel.add(princAtvsField);
        formPanel.add(new JLabel("Refrigerado:"));
        formPanel.add(refriCheckBox);
        formPanel.add(new JLabel("Função:"));
        formPanel.add(funcField);
        formPanel.add(new JLabel("Tarja:"));
        formPanel.add(tarjaComboBox);
        formPanel.add(new JLabel("Dosagens (separadas por ';'):"));
        formPanel.add(dosagField);
        formPanel.add(new JLabel("Unidade de Dosagem:"));
        formPanel.add(undDosagField);
        formPanel.add(new JLabel("Quantidade de Doses:"));
        formPanel.add(qntDosesField);
        formPanel.add(new JLabel("Marca:"));
        formPanel.add(marcaField);
        formPanel.add(new JLabel("Envase:"));
        formPanel.add(envaseField);

        add(formPanel, BorderLayout.CENTER);

        // Setup panel for action buttons.
        JPanel botoesCadastroPanel = new JPanel();
        JButton salvarButton = new JButton("Salvar");
        JButton voltarButton = new JButton("Voltar");

        // Adds an action listener for each button.
        salvarButton.addActionListener(e -> {
            // Get text from fields.
            String nome = nomeField.getText();
            String princAtvs = princAtvsField.getText();
            boolean refri = refriCheckBox.isSelected();
            String func = funcField.getText();
            String tarja = (String) tarjaComboBox.getSelectedItem();
            String dosag = dosagField.getText();
            String undDosag = undDosagField.getText();
            String qntDosesStr = qntDosesField.getText();
            String marca = marcaField.getText();
            String envase = envaseField.getText();

            // Check for empty fields and show error message if medication's data's not complete.
            if (nome.isEmpty() || princAtvs.isEmpty() || func.isEmpty() || tarja.isEmpty() ||
                    dosag.isEmpty() || undDosag.isEmpty() || qntDosesStr.isEmpty() || marca.isEmpty() || envase.isEmpty()) {
                JOptionPane.showMessageDialog(CadastroPanel.this, "Todos os campos devem ser preenchidos.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate string inputs against rules defined previously _ calls up to STRING_PATTERN indirectly.
            if (!isValidString(nome) || !isValidString(princAtvs) || !isValidString(func) ||
                    !isValidString(dosag) || !isValidString(undDosag) || !isValidString(marca) || !isValidString(envase)) {
                JOptionPane.showMessageDialog(CadastroPanel.this, "Os campos não devem conter caracteres especiais (exceto ponto e vírgula).", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Medication's dosages should always be integers. User may change medication's unit system as needed to adjust to it
            // Convert doses quantity to integer and validate.
            int qntDoses;
            try {
                qntDoses = Integer.parseInt(qntDosesStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(CadastroPanel.this, "Quantidade de Doses deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Split active ingredients and dosages, matching them in order
            String[] princAtvsArray = princAtvs.split(";\\s*");
            String[] dosagArray = dosag.split(";\\s*");
            if (princAtvsArray.length != dosagArray.length) {
                JOptionPane.showMessageDialog(CadastroPanel.this, "O número de Princípios Ativos deve ser igual ao número de Dosagens.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate each dosage entry is a valid number.
            try {
                for (String dosagem : dosagArray) {
                    Double.parseDouble(dosagem);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(CadastroPanel.this, "Todas as Dosagens devem ser números válidos.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create new medication object and add to registry.
            Medicamento novoMedicamento = new Medicamento(nome, princAtvs, refri, func, tarja, dosag, undDosag, qntDoses, marca, envase);
            int previousId = cadastro.getMedicamentos().size();
            cadastro.adicionarMedicamento(novoMedicamento);

            // Confirmation message after successful registration.
            if (cadastro.getMedicamentos().size() > previousId) {
                JOptionPane.showMessageDialog(CadastroPanel.this, "Medicamento cadastrado com sucesso!");
            }

            // Clear fields after registration - resets page.
            nomeField.setText("");
            princAtvsField.setText("");
            refriCheckBox.setSelected(false);
            funcField.setText("");
            tarjaComboBox.setSelectedIndex(0);
            dosagField.setText("");
            undDosagField.setText("");
            qntDosesField.setText("");
            marcaField.setText("");
            envaseField.setText("");
        });

        // allows user to go back to initial page
        voltarButton.addActionListener(e -> cardLayout.show(mainPanel, "inicio"));

        botoesCadastroPanel.add(salvarButton);
        botoesCadastroPanel.add(voltarButton);

        add(botoesCadastroPanel, BorderLayout.SOUTH);
    }

    /**
     * Validates if the given string matches the allowed pattern.
     *
     * @param str The string to be validated.
     * @return true if the string matches the pattern and doesn't contain a backslash.
     */
    private boolean isValidString(String str) {
        return STRING_PATTERN.matcher(str).matches() && !str.contains("\\");
    }
}
