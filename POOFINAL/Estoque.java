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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class Registro {
    private String estData;
    private String estNat;
    private String estLocal;
    private String estValid;
    private int estQnt;
    private int medId;
    private int estId;

    public Registro(String estData, int medId, String estNat, String estLocal, String estValid, int estQnt) {
        this.estData = estData;
        this.estNat = estNat;
        this.estLocal = estLocal;
        this.estValid = estValid;
        this.estQnt = estQnt;
        this.medId = medId;
    }

    public String getEstData() {
        return estData;
    }

    public void setEstData(String estData) {
        this.estData = estData;
    }

    public String getEstNat() {
        return estNat;
    }

    public void setEstNat(String estNat) {
        this.estNat = estNat;
    }

    public String getEstLocal() {
        return estLocal;
    }

    public void setEstLocal(String estLocal) {
        this.estLocal = estLocal;
    }

    public String getEstValid() {
        return estValid;
    }

    public void setEstValid(String estValid) {
        this.estValid = estValid;
    }

    public int getEstQnt() {
        return estQnt;
    }

    public void setEstQnt(int estQnt) {
        this.estQnt = estQnt;
    }

    public int getMedId() {
        return medId;
    }

    public void setMedId(int medId) {
        this.medId = medId;
    }

    public int getEstId() {
        return estId;
    }

    public void setEstId(int estId) {
        this.estId = estId;
    }
}

class Estoque {
    private List<Registro> estoque;
    private int lastEstId;
    private CadastroMedicamentos cadastro;

    public Estoque(CadastroMedicamentos cadastro) {
        this.estoque = new ArrayList<>();
        this.lastEstId = 0;
        this.cadastro = cadastro;
    }

    public List<Registro> getEstoque() {
        return estoque;
    }

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

        for (Registro reg : estoque) {
            if (reg.getEstLocal().equals(registro.getEstLocal())) {
                Date dataExistente;
                try {
                    dataExistente = sdf.parse(reg.getEstData());
                } catch (ParseException e) {
                    continue;
                }
                if (dataExistente.after(dataRegistro)) {
                    if (verbose) {
                        JOptionPane.showMessageDialog(null, "Não é permitido cadastrar registros com data anterior a registros já existentes no mesmo local.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                }
            }
        }

        if (registro.getEstNat().equals("Saída")) {
            Map<Integer, Map<String, Integer>> qntDisp = estoqueAtualPorLocal(registro.getEstData(), List.of(registro.getMedId()));
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
        saveCSV("src/hist_estoque.csv");
        if (verbose) {
            JOptionPane.showMessageDialog(null, "Registro realizado com sucesso! (ID " + registro.getEstId() + ")");
        }
    }

    public boolean isLocalPermitido(String local, int medId, String validade, String nat) {
        if (nat.equals("Entrada")) {
            Registro last_reg = estoque.getFirst();
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

    public Map<Integer, Integer> getEstoqueAtualPorMedicamento() {
        Map<Integer, Integer> estoquePorMedicamento = new HashMap<>();

        for (Registro reg : estoque) {
            int medId = reg.getMedId();
            int quantidade = reg.getEstQnt() * (reg.getEstNat().equals("Saída") ? -1 : 1);

            estoquePorMedicamento.put(medId, estoquePorMedicamento.getOrDefault(medId, 0) + quantidade);
        }

        return estoquePorMedicamento;
    }

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


    public void uploadCSV(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();

            if (!estoque.isEmpty()) {
                System.out.println(estoque.size() + " registros foram apagados para upload desse arquivo.");
            }

            estoque.clear();
            lastEstId = 0;

            for (String[] row : rows) {
                if (rows.indexOf(row) == 0) continue;
                int estId = Integer.parseInt(row[0]);
                String estData = row[1];
                int medId = Integer.parseInt(row[2]);
                String estNat = row[3];
                String estLocal = row[4];
                String estValid = row[5];
                int estQnt = Integer.parseInt(row[6]);

                Registro reg = new Registro(estData, medId, estNat, estLocal, estValid, estQnt);
                novoRegistro(reg, false);
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

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

    public Map<Integer, Map<String, Integer>> estoqueAtualPorLocal(String dt, List<Integer> medIdList) {
        List<Registro> filtro = new ArrayList<>(estoque);
        filtro.removeIf(registro -> registro.getEstData().compareTo(dt) > 0);
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

    public List<Registro> getEstoquePorLote(int medId, String local, String validade) {
        List<Registro> lotes = new ArrayList<>();
        for (Registro reg : estoque) {
            if (reg.getMedId() == medId && reg.getEstLocal().equals(local) && reg.getEstValid().equals(validade) && reg.getEstNat().equals("Entrada")) {
                lotes.add(reg);
            }
        }
        return lotes;
    }

    public Map<String, Map<Integer, Map<String, Integer>>> getEstoqueAtualDetalhado() {
        Map<String, Map<Integer, Map<String, Integer>>> estoqueDetalhado = new HashMap<>();

        for (Registro reg : estoque) {
            if (!estoqueDetalhado.containsKey(reg.getEstLocal())) {
                estoqueDetalhado.put(reg.getEstLocal(), new HashMap<>());
            }
            if (!estoqueDetalhado.get(reg.getEstLocal()).containsKey(reg.getMedId())) {
                estoqueDetalhado.get(reg.getEstLocal()).put(reg.getMedId(), new HashMap<>());
            }
            if (!estoqueDetalhado.get(reg.getEstLocal()).get(reg.getMedId()).containsKey(reg.getEstValid())) {
                estoqueDetalhado.get(reg.getEstLocal()).get(reg.getMedId()).put(reg.getEstValid(), 0);
            }

            int quantidade = estoqueDetalhado.get(reg.getEstLocal()).get(reg.getMedId()).get(reg.getEstValid());
            if (reg.getEstNat().equals("Entrada")) {
                quantidade += reg.getEstQnt();
            } else if (reg.getEstNat().equals("Saída")) {
                quantidade -= reg.getEstQnt();
            }

            estoqueDetalhado.get(reg.getEstLocal()).get(reg.getMedId()).put(reg.getEstValid(), quantidade);
        }

        return estoqueDetalhado;
    }

    public List<Registro> verificarValidade() {
        List<Registro> registrosVencidos = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date hoje = new Date();

        Map<Integer, Map<String, Integer>> estoqueAtual = estoqueAtualPorLocal(sdf.format(hoje), null);

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

    public void removerRegistroVencido(int medId, String local, String validade) {
        estoque.removeIf(reg -> reg.getMedId() == medId && reg.getEstLocal().equals(local) && reg.getEstValid().equals(validade) && reg.getEstNat().equals("Entrada"));
        saveCSV("src/hist_estoque.csv");
    }
}

