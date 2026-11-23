package model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import persistence.Persistencia;

public class Locacao implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final double MILISSEGUNDOS_POR_DIA = 1000.0 * 60.0 * 60.0 * 24.0;
    private static List<Locacao> locacoes = new ArrayList<>();
    private static int proximoCodigo = 1;

    private int codigo;
    private Date dataRetirada;
    private Date dataDevolucaoPrevista;
    private Date dataDevolucaoReal;
    private boolean finalizada;
    private Cliente cliente;
    private Veiculo veiculo;

    static {
        Veiculo.definirVerificadorLocacao(Locacao::possuiLocacaoParaVeiculo);
    }

    public Locacao() {
    }

    public Locacao(int codigo, Cliente cliente, Veiculo veiculo, Date dataRetirada, Date dataDevolucaoPrevista) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.dataRetirada = (dataRetirada != null) ? new Date(dataRetirada.getTime()) : null;
        this.dataDevolucaoPrevista = (dataDevolucaoPrevista != null) ? new Date(dataDevolucaoPrevista.getTime()) : null;
        this.dataDevolucaoReal = null;
        this.finalizada = false;
    }

    public Locacao(int codigo, Cliente cliente, Veiculo veiculo, Date dataRetirada, Date dataDevolucaoPrevista, Date dataDevolucaoReal, boolean finalizada) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.dataRetirada = (dataRetirada != null) ? new Date(dataRetirada.getTime()) : null;
        this.dataDevolucaoPrevista = (dataDevolucaoPrevista != null) ? new Date(dataDevolucaoPrevista.getTime()) : null;
        this.dataDevolucaoReal = (dataDevolucaoReal != null) ? new Date(dataDevolucaoReal.getTime()) : null;
        this.finalizada = finalizada;
    }

    private static boolean mesmoVeiculo(Veiculo v1, Veiculo v2) {
        if (v1 == null || v2 == null) {
            return false;
        }
        if (v1 == v2) {
            return true;
        }
        return v1.getPlaca() != null
                && v2.getPlaca() != null
                && v1.getPlaca().trim().equalsIgnoreCase(v2.getPlaca().trim());
    }

    private static boolean possuiLocacaoParaVeiculo(Veiculo veiculo) {
        if (veiculo == null) {
            return false;
        }
        for (Locacao locacao : locacoes) {
            if (mesmoVeiculo(locacao.veiculo, veiculo)) {
                return true;
            }
        }
        return false;
    }

    public Locacao cadastrarLocacao(Cliente cliente, Veiculo veiculo, Date retirada, Date prevista) {
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente nao pode ser nulo.");
        }
        if (veiculo == null) {
            throw new IllegalArgumentException("Veiculo nao pode ser nulo.");
        }
        if (retirada == null) {
            throw new IllegalArgumentException("Data de retirada nao pode ser nula.");
        }
        if (prevista == null) {
            throw new IllegalArgumentException("Data de devolucao prevista nao pode ser nula.");
        }
        if (!cliente.isAtivo()) {
            throw new IllegalStateException("Cliente inativo nao pode realizar locacao.");
        }
        if (!cliente.cnhEstaValida()) {
            throw new IllegalStateException("Cliente com CNH vencida ou invalida nao pode realizar locacao.");
        }
        if (veiculo.getSituacao() != SituacaoVeiculo.DISPONIVEL) {
            throw new IllegalStateException("Veiculo nao esta disponivel para locacao.");
        }
        for (Locacao existente : locacoes) {
            if (!existente.finalizada && mesmoVeiculo(existente.veiculo, veiculo)) {
                throw new IllegalStateException("Veiculo ja possui uma locacao em andamento.");
            }
        }
        if (!prevista.after(retirada)) {
            throw new IllegalArgumentException("A data de devolucao prevista deve ser posterior a data de retirada.");
        }
        veiculo.marcarComoLocado();
        Veiculo.registrarLocacaoPlaca(veiculo.getPlaca());
        Locacao novaLocacao = new Locacao(proximoCodigo++, cliente, veiculo, retirada, prevista);
        locacoes.add(novaLocacao);
        Persistencia.salvarDados();
        return novaLocacao;
    }

    public Locacao buscarLocacao(int codigo) {
        for (Locacao locacao : locacoes) {
            if (locacao.codigo == codigo) {
                return locacao;
            }
        }
        return null;
    }

    public void atualizarLocacao(Date dataRetirada, Date dataDevolucaoPrevista) {
        if (this.finalizada) {
            throw new IllegalStateException("Nao e possivel atualizar uma locacao ja finalizada.");
        }
        if (dataRetirada == null) {
            throw new IllegalArgumentException("Data de retirada nao pode ser nula.");
        }
        if (dataDevolucaoPrevista == null) {
            throw new IllegalArgumentException("Data de devolucao prevista nao pode ser nula.");
        }
        if (!dataDevolucaoPrevista.after(dataRetirada)) {
            throw new IllegalArgumentException("A data de devolucao prevista deve ser posterior a data de retirada.");
        }
        this.dataRetirada = new Date(dataRetirada.getTime());
        this.dataDevolucaoPrevista = new Date(dataDevolucaoPrevista.getTime());
        Persistencia.salvarDados();
    }

    public void finalizarLocacao(Date dataReal) {
        if (this.finalizada) {
            throw new IllegalStateException("Locacao ja foi finalizada.");
        }
        if (dataReal == null) {
            throw new IllegalArgumentException("Data de devolucao real nao pode ser nula.");
        }
        if (this.dataRetirada != null && dataReal.before(this.dataRetirada)) {
            throw new IllegalArgumentException("A data de devolucao real nao pode ser anterior a data de retirada.");
        }
        this.dataDevolucaoReal = new Date(dataReal.getTime());
        this.finalizada = true;
        if (this.veiculo != null) {
            if (this.veiculo.getSituacao() == SituacaoVeiculo.LOCADO) {
                this.veiculo.liberarVeiculo();
            }
            Veiculo veiculoCadastrado = this.veiculo.buscarVeiculo(this.veiculo.getPlaca());
            if (veiculoCadastrado != null
                    && veiculoCadastrado != this.veiculo
                    && veiculoCadastrado.getSituacao() == SituacaoVeiculo.LOCADO) {
                veiculoCadastrado.liberarVeiculo();
            }
        }
        Persistencia.salvarDados();
    }

    public double calcularValor() {
        if (this.veiculo == null || this.dataRetirada == null) {
            return 0.0;
        }
        Date dataReferencia = (this.dataDevolucaoReal != null) ? this.dataDevolucaoReal : this.dataDevolucaoPrevista;
        if (dataReferencia == null) {
            return 0.0;
        }
        long diferencaMs = dataReferencia.getTime() - this.dataRetirada.getTime();
        double diarias = Math.ceil((double) diferencaMs / MILISSEGUNDOS_POR_DIA);
        if (diarias < 1.0) {
            diarias = 1.0;
        }
        return diarias * this.veiculo.getValorDiaria();
    }

    public List<Locacao> listarLocacoes() {
        return new ArrayList<>(locacoes);
    }

    public List<Locacao> buscarPorPeriodo(Date inicio, Date fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Datas de inicio e fim nao podem ser nulas.");
        }
        if (fim.before(inicio)) {
            throw new IllegalArgumentException("A data final nao pode ser anterior a data inicial.");
        }
        List<Locacao> resultado = new ArrayList<>();
        for (Locacao locacao : locacoes) {
            if (locacao.dataRetirada != null
                    && !locacao.dataRetirada.before(inicio)
                    && !locacao.dataRetirada.after(fim)) {
                resultado.add(locacao);
            }
        }
        return resultado;
    }

    public String gerarRelatorio(Date inicio, Date fim) {
        List<Locacao> filtradas = buscarPorPeriodo(inicio, fim);
        SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("=== RELATORIO DE LOCACOES ===\n");
        relatorio.append("Periodo: ")
                .append(formatoData.format(inicio))
                .append(" a ")
                .append(formatoData.format(fim))
                .append("\n");
        relatorio.append("----------------------------------------\n");

        double faturamentoTotal = 0.0;
        if (filtradas.isEmpty()) {
            relatorio.append("Nenhuma locacao encontrada no periodo informado.\n");
        } else {
            for (Locacao locacao : filtradas) {
                double valor = locacao.calcularValor();
                faturamentoTotal += valor;
                String nomeCliente = (locacao.cliente != null) ? locacao.cliente.getNome() : "Nao informado";
                String cpfCliente = (locacao.cliente != null) ? locacao.cliente.getCpf() : "Nao informado";
                String modeloVeiculo = (locacao.veiculo != null) ? locacao.veiculo.getModelo() : "Nao informado";
                String placaVeiculo = (locacao.veiculo != null) ? locacao.veiculo.getPlaca() : "Nao informado";
                String retiradaFmt = (locacao.dataRetirada != null) ? formatoData.format(locacao.dataRetirada) : "Nao informada";
                String previstaFmt = (locacao.dataDevolucaoPrevista != null) ? formatoData.format(locacao.dataDevolucaoPrevista) : "Nao informada";
                String realFmt = (locacao.dataDevolucaoReal != null) ? formatoData.format(locacao.dataDevolucaoReal) : "Em andamento";

                relatorio.append("Codigo: ").append(locacao.codigo).append("\n");
                relatorio.append("Cliente: ").append(nomeCliente).append(" (CPF: ").append(cpfCliente).append(")\n");
                relatorio.append("Veiculo: ").append(modeloVeiculo).append(" (Placa: ").append(placaVeiculo).append(")\n");
                relatorio.append("Retirada: ").append(retiradaFmt).append("\n");
                relatorio.append("Devolucao Prevista: ").append(previstaFmt).append("\n");
                relatorio.append("Devolucao Real: ").append(realFmt).append("\n");
                relatorio.append("Status: ").append(locacao.finalizada ? "Finalizada" : "Em andamento").append("\n");
                relatorio.append(String.format("Valor: R$ %.2f\n", valor));
                relatorio.append("----------------------------------------\n");
            }
        }

        relatorio.append("Total de locacoes: ").append(filtradas.size()).append("\n");
        relatorio.append(String.format("Faturamento total: R$ %.2f\n", faturamentoTotal));
        return relatorio.toString();
    }

    public int getCodigo() {
        return this.codigo;
    }

    public Date getDataRetirada() {
        return (this.dataRetirada != null) ? new Date(this.dataRetirada.getTime()) : null;
    }

    public Date getDataDevolucaoPrevista() {
        return (this.dataDevolucaoPrevista != null) ? new Date(this.dataDevolucaoPrevista.getTime()) : null;
    }

    public Date getDataDevolucaoReal() {
        return (this.dataDevolucaoReal != null) ? new Date(this.dataDevolucaoReal.getTime()) : null;
    }

    public boolean isFinalizada() {
        return this.finalizada;
    }

    public Cliente getCliente() {
        return this.cliente;
    }

    public Veiculo getVeiculo() {
        return this.veiculo;
    }

    protected static List<Locacao> obterListaLocacoes() {
        return new ArrayList<>(locacoes);
    }

    protected static int obterProximoCodigo() {
        return proximoCodigo;
    }

    protected static void restaurarLocacoes(List<Locacao> lista, int novoProximoCodigo) {
        locacoes = (lista != null) ? new ArrayList<>(lista) : new ArrayList<>();
        proximoCodigo = Math.max(1, novoProximoCodigo);
    }
}
