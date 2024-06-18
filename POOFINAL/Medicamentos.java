import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Medicamento {
    private int medId;
    private String medNome;
    private List<String> medPrincAtvs;
    private boolean medRefri;
    private String medFunc;
    private String medRisco;
    private List<Double> medDosag;
    private String medUndDosag;
    private int medQntDoses;
    private String medMarca;
    private String medEnvase;

    public Medicamento(String medNome, String medPrincAtvs, boolean medRefri, String medFunc,
                       String medRisco, String medDosag, String medUndDosag, int medQntDoses,
                       String medMarca, String medEnvase) {
        this.medNome = medNome;
        this.medPrincAtvs = List.of(medPrincAtvs.split(";\\s*"));
        this.medRefri = medRefri;
        this.medFunc = medFunc;
        this.medRisco = medRisco;
        this.medDosag = new ArrayList<>();
        for (String dosagem : medDosag.split(";\\s*")) {
            this.medDosag.add(Double.parseDouble(dosagem.trim()));
        }
        this.medUndDosag = medUndDosag;
        this.medQntDoses = medQntDoses;
        this.medMarca = medMarca;
        this.medEnvase = medEnvase;
    }

    // Getters and Setters

    public int getMedId() {
        return medId;
    }

    public void setMedId(int medId) {
        this.medId = medId;
    }

    public String getMedNome() {
        return medNome;
    }

    public List<String> getMedPrincAtvs() {
        return medPrincAtvs;
    }

    public boolean isMedRefri() {
        return medRefri;
    }

    public String getMedFunc() {
        return medFunc;
    }

    public String getMedRisco() {
        return medRisco;
    }

    public List<Double> getMedDosag() {
        return medDosag;
    }

    public String getMedUndDosag() {
        return medUndDosag;
    }

    public int getMedQntDoses() {
        return medQntDoses;
    }

    public String getMedMarca() {
        return medMarca;
    }

    public String getMedEnvase() {
        return medEnvase;
    }

    public String getPrincAtvsAsString() {
        return String.join("; ", medPrincAtvs)
    }

    public String getDosagensAsString() {
        StringBuilder sb = new StringBuilder();
        for (Double dosagem : medDosag) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(dosagem);
        }
        return sb.toString();
    }
}



class CadastroMedicamentos {
    private List<Medicamento> medicamentos;
    private int lastMedId;

    public CadastroMedicamentos() {
        this.medicamentos = new ArrayList<>();
        this.lastMedId = 0;
    }

    public void adicionarMedicamento(Medicamento medicamento) {
        this.lastMedId++;
        medicamento.setMedId(this.lastMedId);
        this.medicamentos.add(medicamento);
        saveCSV("src/med_cadastro.csv");
    }

    public Medicamento getMedicamento(int id) {
        for (Medicamento med : medicamentos) {
            if (med.getMedId() == id) {
                return med;
            }
        }
        return null;
    }

    public List<Medicamento> getMedicamentos() {
        return medicamentos;
    }

    public void uploadCSV(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();
            medicamentos.clear();
            lastMedId = 0;

            for (String[] row : rows) {
                if (rows.indexOf(row) == 0) continue; // Skip header row
                Medicamento med = new Medicamento(row[1], row[2], Boolean.parseBoolean(row[3]), row[4],
                        row[5], row[6].replaceAll(",", ";"), row[7], Integer.parseInt(row[8]), row[9], row[10]);
                med.setMedId(Integer.parseInt(row[0]));
                adicionarMedicamento(med);
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public void saveCSV(String filePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            String[] header = {"ID", "Nome", "Princípios Ativos", "Refrigerado", "Função", "Risco", "Dosagens", "Unidade", "Quantidade de Doses", "Marca", "Envase"};
            writer.writeNext(header);

            for (Medicamento med : medicamentos) {
                String[] data = {
                        String.valueOf(med.getMedId()),
                        med.getMedNome(),
                        med.getPrincAtvsAsString(),
                        String.valueOf(med.isMedRefri()),
                        med.getMedFunc(),
                        med.getMedRisco(),
                        med.getDosagensAsString(),
                        med.getMedUndDosag(),
                        String.valueOf(med.getMedQntDoses()),
                        med.getMedMarca(),
                        med.getMedEnvase()
                };
                writer.writeNext(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}