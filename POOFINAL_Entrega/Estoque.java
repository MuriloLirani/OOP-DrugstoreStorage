// Importing necessary libraries for CSV handling and data manipulation and storage
import com.opencsv.CSVWriter;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JOptionPane;

/**
 * Class representing a stock record - "um lote".
 */
class Registro {
    private String estData;
    private String estNat;
    private String estLocal;
    private String estValid;
    private int estQnt;
    private int medId;
    private int estId;

    /**
     * Constructor for stock record.
     * @param estData The date of the stock movement.
     * @param medId The ID of the medicine.
     * @param estNat The nature of the stock movement (entry, exit).
     * @param estLocal The location of the stock - where the medications' be added to.
     * @param estValid The expiration date of the medicine.
     * @param estQnt The quantity of the stock movement.
     */
    public Registro(String estData, int medId, String estNat, String estLocal, String estValid, int estQnt) {
        this.estData = estData;
        this.estNat = estNat;
        this.estLocal = estLocal;
        this.estValid = estValid;
        this.estQnt = estQnt;
        this.medId = medId;
    }

    // Get and Set methods: support methods that allow other parts of the program to acess med record's information
    // example: when checking for expired meds, expiration date field must be accessed.
    public String getEstData() {
        return estData; }

    public void setEstData(String estData) {
        this.estData = estData; }

    public String getEstNat() {
        return estNat; }

    public void setEstNat(String estNat) {
        this.estNat = estNat; }

    public String getEstLocal() {
        return estLocal; }

    public void setEstLocal(String estLocal) {
        this.estLocal = estLocal; }

    public String getEstValid() {
        return estValid; }

    public void setEstValid(String estValid) {
        this.estValid = estValid; }

    public int getEstQnt() {
        return estQnt; }

    public void setEstQnt(int estQnt) {
        this.estQnt = estQnt; }

    public int getMedId() {
        return medId; }

    public void setMedId(int medId) {
        this.medId = medId; }

    public int getEstId() {
        return estId; }

    public void setEstId(int estId) {
        this.estId = estId; }
}

/**
 * Class for managing the stock of medicines
 * An array is used to store all records.
 */
class Estoque {
    private List<Registro> estoque;
    private int lastEstId;
    private CadastroMedicamentos cadastro;

    /**
     * Constructor for stock management.
     * @param cadastro The medicine registration system.
     */
    public Estoque(CadastroMedicamentos cadastro) {
        this.estoque = new ArrayList<>();
        this.lastEstId = 0;
        this.cadastro = cadastro;
    }

    //makes stock acessible to other parts of the program
    public List<Registro> getEstoque() {
        return estoque; }

    /**
     * Registers a new stock movement - could be entering, exit.
     * movement's added to stock's history - the program keeps a record of all stock movements.
     * guarantees inserted date is in expected format and that records are added in a cronologically coehrent order on a same place, so expiration dates are not messed up
     * for example: user cannot add
     * @param registro The stock record to add.
     * @param verbose Whether to show verbose output.
     */
    public void novoRegistro(Registro registro, boolean verbose) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date dataRegistro;
        try {
            dataRegistro = sdf.parse(registro.getEstData());
        } catch (ParseException e) {
            if (verbose) {
                JOptionPane.showMessageDialog(null, "Data do registro inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // Verifies that no existing record at the same location has a date after the new record's date
        for (Registro reg : estoque) {
            if (reg.getEstLocal().equals(registro.getEstLocal())) {
                Date dataExistente;
                try {
                    dataExistente = sdf.parse(reg.getEstData());
                } catch (ParseException e) {
                    continue; // Ignore records with invalid date
                }
                if (dataExistente.after(dataRegistro)) {
                    if (verbose) {
                        JOptionPane.showMessageDialog(null, "Não é permitido cadastrar registros com data anterior a registros já existentes no mesmo local.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                }
            }
        }

        // Checks the available quantity for "exit" operations
        if (registro.getEstNat().equals("Saída")) {
            Map<Integer, Map<String, Integer>> qntDisp = estoqueAtualPorLocal(List.of(registro.getMedId()));
            int quantidadeDisponivel = qntDisp.getOrDefault(registro.getMedId(), new HashMap<>()).getOrDefault(registro.getEstLocal(), 0);
            if (registro.getEstQnt() > quantidadeDisponivel) {
                if (verbose) {
                    JOptionPane.showMessageDialog(null, "Quantidade não disponível no local especificado. Registro não realizado.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }
        }

        this.lastEstId++;
        registro.setEstId(this.lastEstId);
        this.estoque.add(registro);
        saveCSV("src/hist_estoque.csv"); // Save after adding a new registro
        if (verbose) {
            JOptionPane.showMessageDialog(null, "Registro realizado com sucesso! (ID " + registro.getEstId() + ")");
        }
    }

    /**
     * Checks if the location is allowed based on the last "entry" nature record for the same medicine and validity.
     * @param local The stock location.
     * @param medId The ID of the medicine.
     * @param validade The validity of the medicine.
     * @param nat The nature of the stock movement.
     * @return true if the location is permitted, false otherwise.
     */
    public boolean isLocalPermitido(String local, int medId, String validade, String nat) {
        if (nat.equals("Entrada")) {
            Registro last_reg = estoque.get(0);
            for (Registro reg : estoque) {
                if (reg.getEstLocal().equals(local) && reg.getEstNat().equals("Entrada")) {
                    last_reg = reg;
                }
            }
            if (last_reg.getMedId() == medId && last_reg.getEstValid().equals(validade)) {
                return true;
            }
        }
        int quantidadeDisponivel = 0;
        for (Registro reg : estoque) {
            if (reg.getEstLocal().equals(local)) {
                if (reg.getEstNat().equals("Entrada")) {
                    quantidadeDisponivel += reg.getEstQnt();
                } else if (reg.getEstNat().equals("Saída")) {
                    quantidadeDisponivel -= reg.getEstQnt();
                }
            }
        }
        return quantidadeDisponivel == 0;
    }

    /**
     * Computes the current stock by medicine ID.
     * @return A map of medicine IDs to their respective stock quantities.
     */
    public Map<Integer, Integer> getEstoqueAtualPorMedicamento() {
        Map<Integer, Integer> estoquePorMedicamento = new HashMap<>();

        for (Registro reg : estoque) {
            int medId = reg.getMedId();
            int quantidade = reg.getEstQnt() * (reg.getEstNat().equals("Saída") ? -1 : 1);

            estoquePorMedicamento.put(medId, estoquePorMedicamento.getOrDefault(medId, 0) + quantidade);
        }

        return estoquePorMedicamento;
    }

    /**
     * Computes the current stock by location.
     * @return A map of locations to their respective medicine stock counts.
     */
    public Map<String, Map<Integer, Integer>> getEstoqueAtualPorLocal() {
        Map<String, Map<Integer, Integer>> estoquePorLocal = new HashMap<>();

        for (Registro reg : estoque) {
            String local = reg.getEstLocal();
            int medId = reg.getMedId();
            int quantidade = reg.getEstQnt() * (reg.getEstNat().equals("Saída") ? -1 : 1);

            estoquePorLocal.putIfAbsent(local, new HashMap<>());
            estoquePorLocal.get(local).put(medId, estoquePorLocal.get(local).getOrDefault(medId, 0) + quantidade);
        }

        return estoquePorLocal;
    }

    /**
     * Computes the validity of stock by location.
     * @return A map of locations to a map of medicine IDs and their latest validity dates.
     */
    public Map<String, Map<Integer, String>> getValidadePorLocal() {
        Map<String, Map<Integer, String>> validadePorLocal = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (Registro reg : estoque) {
            if (reg.getEstNat().equals("Entrada")) {
                String local = reg.getEstLocal();
                int medId = reg.getMedId();
                String validade = reg.getEstValid();
                String data = reg.getEstData();

                validadePorLocal.putIfAbsent(local, new HashMap<>());
                validadePorLocal.get(local).putIfAbsent(medId, validade);

                try {
                    Date dataRegistro = sdf.parse(data);
                    Date dataExistente = sdf.parse(validadePorLocal.get(local).get(medId));

                    if (dataRegistro.after(dataExistente)) {
                        validadePorLocal.get(local).put(medId, validade);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return validadePorLocal;
    }

    /**
     * Uploads a CSV file to replace current stock data.
     * @param filePath The path of the CSV file to be read.
     */
    public void uploadCSV(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();

            if (!estoque.isEmpty()) {
                System.out.println(estoque.size() + " registros foram apagados para upload desse arquivo.");
            }

            estoque.clear();
            lastEstId = 0;

            for (String[] row : rows) {
                if (rows.indexOf(row) == 0) continue; // Skip header row
                int estId = Integer.parseInt(row[0]);
                String estData = row[1];
                int medId = Integer.parseInt(row[2]);
                String estNat = row[3];
                String estLocal = row[4];
                String estValid = row[5];
                int estQnt = Integer.parseInt(row[6]);

                Registro reg = new Registro(estData, medId, estNat, estLocal, estValid, estQnt);
                novoRegistro(reg, false); // Pass false for verbose
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the current stock data to a CSV file.
     * @param filePath The path where the CSV file will be saved.
     */
    public void saveCSV(String filePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            String[] header = {"ID", "Data", "ID Medicamento", "Natureza", "Local", "Validade", "Quantidade"};
            writer.writeNext(header);

            for (Registro reg : estoque) {
                String[] data = {
                        String.valueOf(reg.getEstId()),
                        reg.getEstData(),
                        String.valueOf(reg.getMedId()),
                        reg.getEstNat(),
                        reg.getEstLocal(),
                        reg.getEstValid(),
                        String.valueOf(reg.getEstQnt())
                };
                writer.writeNext(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Computes the current stock by medicine for specified medicine IDs.
     * @param medIdList List of medicine IDs to filter the stock.
     * @return A map of medicine IDs to a map of their quantities by location.
     */
    public Map<Integer, Map<String, Integer>> estoqueAtualPorLocal(List<Integer> medIdList) {
        List<Registro> filtro = new ArrayList<>(estoque);
        if (medIdList != null && !medIdList.isEmpty()) {
            filtro.removeIf(registro -> !medIdList.contains(registro.getMedId()));
        }

        Map<Integer, Map<String, Integer>> medDic = new HashMap<>();
        for (Registro reg : filtro) {
            medDic.putIfAbsent(reg.getMedId(), new HashMap<>());
            medDic.get(reg.getMedId()).putIfAbsent(reg.getEstLocal(), 0);
            medDic.get(reg.getMedId()).compute(reg.getEstLocal(), (k, v) -> v + (reg.getEstQnt() * (reg.getEstNat().equals("Saída") ? -1 : 1)));
        }

        return medDic;
    }

    /**
     * Verifies the validity of the stock based on the current date.
     * @return A list of expired stock records.
     */
    public List<Registro> verificarValidade() {
        List<Registro> registrosVencidos = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date hoje = new Date();

        Map<Integer, Map<String, Integer>> estoqueAtual = estoqueAtualPorLocal(null);

        for (Registro reg : estoque) {
            if (reg.getEstNat().equals("Entrada")) {
                try {
                    Date validade = sdf.parse(reg.getEstValid());
                    if (validade.before(sdf.parse(sdf.format(hoje)))) {
                        if (estoqueAtual.containsKey(reg.getMedId())) {
                            if (estoqueAtual.get(reg.getMedId()).containsKey(reg.getEstLocal())) {
                                if (estoqueAtual.get(reg.getMedId()).get(reg.getEstLocal()) > 0) {
                                    registrosVencidos.add(reg);
                                }
                            }
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return registrosVencidos;
    }
}
