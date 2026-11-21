package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class Veiculo implements Serializable {
    private static final long serialVersionUID = 1L;
    private static List<Veiculo> veiculos = new ArrayList<>();
    private static Set<String> placasComLocacao = new HashSet<>();
    private static Predicate<Veiculo> verificadorLocacao;

    private String placa;
    private String modelo;
    private double valorDiaria;
    private SituacaoVeiculo situacao;

    public Veiculo() {
        this.situacao = SituacaoVeiculo.DISPONIVEL;
    }

    public Veiculo(String placa, String modelo, double valorDiaria) {
        this.placa = placa;
        this.modelo = modelo;
        this.valorDiaria = valorDiaria;
        this.situacao = SituacaoVeiculo.DISPONIVEL;
    }

    public Veiculo(String placa, String modelo, double valorDiaria, SituacaoVeiculo situacao) {
        this.placa = placa;
        this.modelo = modelo;
        this.valorDiaria = valorDiaria;
        this.situacao = (situacao != null) ? situacao : SituacaoVeiculo.DISPONIVEL;
    }

    static void definirVerificadorLocacao(Predicate<Veiculo> verificador) {
        verificadorLocacao = verificador;
    }

    static void registrarLocacaoPlaca(String placa) {
        if (placa != null && !placa.trim().isEmpty()) {
            placasComLocacao.add(placa.trim().toUpperCase());
        }
    }

    public Veiculo cadastrarVeiculo(String placa, String modelo, double valorDiaria) {
        if (placa == null || placa.trim().isEmpty()) {
            throw new IllegalArgumentException("Placa nao pode ser vazia.");
        }
        if (modelo == null || modelo.trim().isEmpty()) {
            throw new IllegalArgumentException("Modelo nao pode ser vazio.");
        }
        if (valorDiaria <= 0) {
            throw new IllegalArgumentException("Valor da diaria deve ser maior que zero.");
        }
        String placaTratada = placa.trim().toUpperCase();
        if (buscarVeiculo(placaTratada) != null) {
            throw new IllegalArgumentException("Ja existe um veiculo cadastrado com esta placa.");
        }
        Veiculo novoVeiculo = new Veiculo(placaTratada, modelo.trim(), valorDiaria);
        veiculos.add(novoVeiculo);
        return novoVeiculo;
    }

    public Veiculo buscarVeiculo(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            return null;
        }
        String placaTratada = placa.trim().toUpperCase();
        for (Veiculo veiculo : veiculos) {
            if (veiculo.placa != null && veiculo.placa.equalsIgnoreCase(placaTratada)) {
                return veiculo;
            }
        }
        return null;
    }

    public void atualizarVeiculo(String modelo, double valorDiaria, String placa) {
        if (modelo == null || modelo.trim().isEmpty()) {
            throw new IllegalArgumentException("Modelo nao pode ser vazio.");
        }
        if (valorDiaria <= 0) {
            throw new IllegalArgumentException("Valor da diaria deve ser maior que zero.");
        }
        if (placa == null || placa.trim().isEmpty()) {
            throw new IllegalArgumentException("Placa nao pode ser vazia.");
        }
        String placaTratada = placa.trim().toUpperCase();
        for (Veiculo existente : veiculos) {
            if (existente != this && existente.placa != null && existente.placa.equalsIgnoreCase(placaTratada)) {
                throw new IllegalArgumentException("Ja existe outro veiculo com esta placa.");
            }
        }
        if (this.placa != null) {
            String placaAnterior = this.placa.trim().toUpperCase();
            if (placasComLocacao.remove(placaAnterior)) {
                placasComLocacao.add(placaTratada);
            }
        }
        this.modelo = modelo.trim();
        this.valorDiaria = valorDiaria;
        this.placa = placaTratada;
    }

    public boolean excluirVeiculo(String placa) {
        Veiculo veiculo = buscarVeiculo(placa);
        if (veiculo == null) {
            return false;
        }
        if (veiculo.situacao == SituacaoVeiculo.LOCADO) {
            return false;
        }
        String placaChave = (veiculo.placa != null) ? veiculo.placa.trim().toUpperCase() : "";
        if (placasComLocacao.contains(placaChave)) {
            return false;
        }
        if (verificadorLocacao != null && verificadorLocacao.test(veiculo)) {
            return false;
        }
        return veiculos.remove(veiculo);
    }

    public List<Veiculo> listarVeiculos() {
        return new ArrayList<>(veiculos);
    }

    public List<Veiculo> buscarPorSituacao(SituacaoVeiculo situacao) {
        List<Veiculo> resultado = new ArrayList<>();
        if (situacao == null) {
            return resultado;
        }
        for (Veiculo veiculo : veiculos) {
            if (veiculo.situacao == situacao) {
                resultado.add(veiculo);
            }
        }
        return resultado;
    }

    public void marcarComoLocado() {
        if (this.situacao != SituacaoVeiculo.DISPONIVEL) {
            throw new IllegalStateException("Apenas veiculos disponiveis podem ser marcados como locados.");
        }
        this.situacao = SituacaoVeiculo.LOCADO;
        if (this.placa != null) {
            placasComLocacao.add(this.placa.trim().toUpperCase());
        }
    }

    public void liberarVeiculo() {
        if (this.situacao != SituacaoVeiculo.LOCADO) {
            throw new IllegalStateException("Apenas veiculos locados podem ser liberados.");
        }
        this.situacao = SituacaoVeiculo.DISPONIVEL;
    }

    public void colocarEmManutencao() {
        if (this.situacao != SituacaoVeiculo.DISPONIVEL) {
            throw new IllegalStateException("Apenas veiculos disponiveis podem ser colocados em manutencao.");
        }
        this.situacao = SituacaoVeiculo.MANUTENCAO;
    }

    public void liberarManutencao() {
        if (this.situacao != SituacaoVeiculo.MANUTENCAO) {
            throw new IllegalStateException("Apenas veiculos em manutencao podem ter a manutencao liberada.");
        }
        this.situacao = SituacaoVeiculo.DISPONIVEL;
    }

    public String getPlaca() {
        return this.placa;
    }

    public String getModelo() {
        return this.modelo;
    }

    public double getValorDiaria() {
        return this.valorDiaria;
    }

    public SituacaoVeiculo getSituacao() {
        return this.situacao;
    }
}
