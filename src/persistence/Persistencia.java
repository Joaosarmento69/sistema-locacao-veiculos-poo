package persistence;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Funcionario;
import model.Locacao;
import model.Usuario;
import model.Veiculo;

public class Persistencia {
    private static final String ARQUIVO_DADOS = "dados.dat";

    private static class DadosSistema implements Serializable {
        private static final long serialVersionUID = 1L;
        private ArrayList<Usuario> usuarios;
        private ArrayList<Veiculo> veiculos;
        private ArrayList<Locacao> locacoes;
        private HashSet<String> placasComLocacao;
        private int proximoId;
        private int proximaMatricula;
        private int proximoCodigo;

        private DadosSistema(
                List<Usuario> usuarios,
                List<Veiculo> veiculos,
                List<Locacao> locacoes,
                Set<String> placasComLocacao,
                int proximoId,
                int proximaMatricula,
                int proximoCodigo) {
            this.usuarios = new ArrayList<>(usuarios);
            this.veiculos = new ArrayList<>(veiculos);
            this.locacoes = new ArrayList<>(locacoes);
            this.placasComLocacao = new HashSet<>(placasComLocacao);
            this.proximoId = proximoId;
            this.proximaMatricula = proximaMatricula;
            this.proximoCodigo = proximoCodigo;
        }
    }

    private static final class AcessoUsuario extends Usuario {
        private static final long serialVersionUID = 1L;

        private static List<Usuario> obterLista() {
            return Usuario.obterListaUsuarios();
        }

        private static int obterContadorId() {
            return Usuario.obterProximoId();
        }

        private static void restaurar(List<Usuario> lista, int proximoId) {
            Usuario.restaurarUsuarios(lista, proximoId);
        }
    }

    private static final class AcessoFuncionario extends Funcionario {
        private static final long serialVersionUID = 1L;

        private static int obterContadorMatricula() {
            return Funcionario.obterProximaMatricula();
        }

        private static void restaurar(int proximaMatricula) {
            Funcionario.restaurarEstadoFuncionario(proximaMatricula);
        }
    }

    private static final class AcessoVeiculo extends Veiculo {
        private static final long serialVersionUID = 1L;

        private static List<Veiculo> obterLista() {
            return Veiculo.obterListaVeiculos();
        }

        private static Set<String> obterPlacas() {
            return Veiculo.obterPlacasComLocacao();
        }

        private static void restaurar(List<Veiculo> lista, Set<String> placas) {
            Veiculo.restaurarVeiculos(lista, placas);
        }
    }

    private static final class AcessoLocacao extends Locacao {
        private static final long serialVersionUID = 1L;

        private static List<Locacao> obterLista() {
            return Locacao.obterListaLocacoes();
        }

        private static int obterContadorCodigo() {
            return Locacao.obterProximoCodigo();
        }

        private static void restaurar(List<Locacao> lista, int proximoCodigo) {
            Locacao.restaurarLocacoes(lista, proximoCodigo);
        }
    }

    public static void salvarDados() {
        DadosSistema dados = new DadosSistema(
                AcessoUsuario.obterLista(),
                AcessoVeiculo.obterLista(),
                AcessoLocacao.obterLista(),
                AcessoVeiculo.obterPlacas(),
                AcessoUsuario.obterContadorId(),
                AcessoFuncionario.obterContadorMatricula(),
                AcessoLocacao.obterContadorCodigo()
        );
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO_DADOS))) {
            oos.writeObject(dados);
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados no arquivo " + ARQUIVO_DADOS + ": " + e.getMessage());
        }
    }

    public static void carregarDados() {
        AcessoLocacao.obterContadorCodigo();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARQUIVO_DADOS))) {
            Object objeto = ois.readObject();
            if (objeto instanceof DadosSistema) {
                DadosSistema dados = (DadosSistema) objeto;
                List<Usuario> usuarios = (dados.usuarios != null) ? dados.usuarios : new ArrayList<>();
                List<Veiculo> veiculos = (dados.veiculos != null) ? dados.veiculos : new ArrayList<>();
                List<Locacao> locacoes = (dados.locacoes != null) ? dados.locacoes : new ArrayList<>();
                Set<String> placas = (dados.placasComLocacao != null) ? new HashSet<>(dados.placasComLocacao) : new HashSet<>();

                int proximoId = Math.max(1, dados.proximoId);
                int proximaMatricula = Math.max(1, dados.proximaMatricula);
                for (Usuario usuario : usuarios) {
                    if (usuario != null) {
                        if (usuario.getId() >= proximoId) {
                            proximoId = usuario.getId() + 1;
                        }
                        if (usuario instanceof Funcionario) {
                            Funcionario funcionario = (Funcionario) usuario;
                            if (funcionario.getMatricula() >= proximaMatricula) {
                                proximaMatricula = funcionario.getMatricula() + 1;
                            }
                        }
                    }
                }

                int proximoCodigo = Math.max(1, dados.proximoCodigo);
                for (Locacao locacao : locacoes) {
                    if (locacao != null) {
                        if (locacao.getCodigo() >= proximoCodigo) {
                            proximoCodigo = locacao.getCodigo() + 1;
                        }
                        if (locacao.getVeiculo() != null && locacao.getVeiculo().getPlaca() != null) {
                            placas.add(locacao.getVeiculo().getPlaca().trim().toUpperCase());
                        }
                    }
                }

                AcessoUsuario.restaurar(usuarios, proximoId);
                AcessoFuncionario.restaurar(proximaMatricula);
                AcessoVeiculo.restaurar(veiculos, placas);
                AcessoLocacao.restaurar(locacoes, proximoCodigo);
            }
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar dados do arquivo " + ARQUIVO_DADOS + ": " + e.getMessage());
        }
    }
}
