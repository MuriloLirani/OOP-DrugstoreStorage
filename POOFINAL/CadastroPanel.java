import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class CadastroPanel extends JPanel {
    private static final Pattern STRING_PATTERN = Pattern.compile("^[a-zA-Z0-9 ;%]+$"); 

    public CadastroPanel(CardLayout cardLayout, JPanel mainPanel, CadastroMedicamentos cadastro) {
        setLayout(new BorderLayout());

        JLabel cadastroLabel = new JLabel("Cadastro de Medicamento", SwingConstants.CENTER);
        cadastroLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(cadastroLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(11, 2));

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

        JPanel botoesCadastroPanel = new JPanel();
        JButton salvarButton = new JButton("Salvar");
        JButton voltarButton = new JButton("Voltar");

        salvarButton.addActionListener(e -> {
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

            if (nome.isEmpty() || princAtvs.isEmpty() || func.isEmpty() || tarja.isEmpty() ||
                    dosag.isEmpty() || undDosag.isEmpty() || qntDosesStr.isEmpty() || marca.isEmpty() || envase.isEmpty()) {
                JOptionPane.showMessageDialog(CadastroPanel.this, "Todos os campos devem ser preenchidos.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidString(nome) || !isValidString(princAtvs) || !isValidString(func) ||
                    !isValidString(dosag) || !isValidString(undDosag) || !isValidString(marca) || !isValidString(envase)) {
                JOptionPane.showMessageDialog(CadastroPanel.this, "Os campos não devem conter caracteres especiais (exceto ponto e vírgula).", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int qntDoses;
            try {
                qntDoses = Integer.parseInt(qntDosesStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(CadastroPanel.this, "Quantidade de Doses deve ser um número inteiro.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] princAtvsArray = princAtvs.split(";\\s*");
            String[] dosagArray = dosag.split(";\\s*");

            if (princAtvsArray.length != dosagArray.length) {
                JOptionPane.showMessageDialog(CadastroPanel.this, "O número de Princípios Ativos deve ser igual ao número de Dosagens.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                for (String dosagem : dosagArray) {
                    Double.parseDouble(dosagem);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(CadastroPanel.this, "Todas as Dosagens devem ser números válidos.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Medicamento novoMedicamento = new Medicamento(nome, princAtvs, refri, func, tarja, dosag, undDosag, qntDoses, marca, envase);
            int previousId = cadastro.getMedicamentos().size();
            cadastro.adicionarMedicamento(novoMedicamento);

            if (cadastro.getMedicamentos().size() > previousId) {
                JOptionPane.showMessageDialog(CadastroPanel.this, "Medicamento cadastrado com sucesso!");
            }

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

        voltarButton.addActionListener(e -> cardLayout.show(mainPanel, "inicio"));

        botoesCadastroPanel.add(salvarButton);
        botoesCadastroPanel.add(voltarButton);

        add(botoesCadastroPanel, BorderLayout.SOUTH);
    }

    private boolean isValidString(String str) {
        return STRING_PATTERN.matcher(str).matches() && !str.contains("\\");
    }
}
