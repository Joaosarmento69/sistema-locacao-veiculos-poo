package model;

import java.util.ArrayList;
import java.util.List;
import persistence.Persistencia;

public class Funcionario extends Usuario {
    private static final long serialVersionUID = 1L;
    private static int proximaMatricula = 1;
    private static Funcionario funcionarioLogado;

    private int matricula;
    private String email;
    private String senha;
    private boolean gerente;

    public Funcionario() {
        super();
    }

    public Funcionario(String nome, String cpf, String telefone, String email, String senha, boolean gerente) {
        super(nome, cpf, telefone);
        this.email = email;
        this.senha = senha;
        this.gerente = gerente;
    }

    public Funcionario(int id, String nome, String cpf, String telefone, boolean ativo, int matricula, String email, String senha, boolean gerente) {
        super(id, nome, cpf, telefone, ativo);
        this.matricula = matricula;
        this.email = email;
        this.senha = senha;
        this.gerente = gerente;
    }

    @Override
    public void cadastrarUsuario(Usuario usuario) {
        super.cadastrarUsuario(usuario);
        if (usuario instanceof Funcionario) {
            Funcionario funcionario = (Funcionario) usuario;
            if (funcionario.matricula <= 0) {
                funcionario.matricula = proximaMatricula++;
            } else if (funcionario.matricula >= proximaMatricula) {
                proximaMatricula = funcionario.matricula + 1;
            }
            Persistencia.salvarDados();
        }
    }

    private void garantirGerentePadrao() {
        if (this.listarFuncionarios().isEmpty()) {
            Funcionario admin = new Funcionario(
                    "Administrador",
                    "00000000000",
                    "00000000000",
                    "admin@locadora.com",
                    "admin",
                    true
            );
            this.cadastrarUsuario(admin);
        }
    }

    public Funcionario login(String email, String senha) {
        garantirGerentePadrao();
        if (email == null || senha == null) {
            return null;
        }
        String emailTratado = email.trim();
        for (Funcionario funcionario : listarFuncionarios()) {
            if (funcionario.email != null
                    && funcionario.email.equalsIgnoreCase(emailTratado)
                    && funcionario.senha != null
                    && funcionario.senha.equals(senha)) {
                if (funcionario.isAtivo()) {
                    funcionarioLogado = funcionario;
                    return funcionario;
                }
                return null;
            }
        }
        return null;
    }

    public void logout() {
        funcionarioLogado = null;
    }

    public Funcionario cadastrarFuncionario(String nome, String email, String senha, String cpf, String telefone, boolean gerente) {
        boolean autorizado = (funcionarioLogado != null) ? funcionarioLogado.isGerente() : this.gerente;
        if (!autorizado) {
            throw new IllegalStateException("Apenas gerentes podem cadastrar funcionarios.");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome nao pode ser vazio.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email nao pode ser vazio.");
        }
        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha nao pode ser vazia.");
        }
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF nao pode ser vazio.");
        }
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new IllegalArgumentException("Telefone nao pode ser vazio.");
        }
        String emailTratado = email.trim();
        String cpfTratado = cpf.trim();
        for (Funcionario existente : listarFuncionarios()) {
            if (existente.getCpf() != null && existente.getCpf().equals(cpfTratado)) {
                throw new IllegalArgumentException("Ja existe um funcionario cadastrado com este CPF.");
            }
            if (existente.email != null && existente.email.equalsIgnoreCase(emailTratado)) {
                throw new IllegalArgumentException("Ja existe um funcionario cadastrado com este email.");
            }
        }
        Funcionario novoFuncionario = new Funcionario(
                nome.trim(),
                cpfTratado,
                telefone.trim(),
                emailTratado,
                senha,
                gerente
        );
        cadastrarUsuario(novoFuncionario);
        return novoFuncionario;
    }

    public void visualizarFuncionario() {
        super.visualizarDados();
        System.out.println("Matricula: " + this.matricula);
        System.out.println("Email: " + this.email);
        System.out.println("Cargo: " + (this.gerente ? "Gerente" : "Funcionario"));
    }

    public void atualizarFuncionario(String nome, String email, String senha, String cpf, String telefone) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome nao pode ser vazio.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email nao pode ser vazio.");
        }
        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha nao pode ser vazia.");
        }
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF nao pode ser vazio.");
        }
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new IllegalArgumentException("Telefone nao pode ser vazio.");
        }
        String emailTratado = email.trim();
        String cpfTratado = cpf.trim();
        for (Funcionario existente : listarFuncionarios()) {
            if (existente != this) {
                if (existente.getCpf() != null && existente.getCpf().equals(cpfTratado)) {
                    throw new IllegalArgumentException("Ja existe outro funcionario com este CPF.");
                }
                if (existente.email != null && existente.email.equalsIgnoreCase(emailTratado)) {
                    throw new IllegalArgumentException("Ja existe outro funcionario com este email.");
                }
            }
        }
        super.atualizarDados(nome, telefone);
        setCpf(cpfTratado);
        this.email = emailTratado;
        this.senha = senha;
        Persistencia.salvarDados();
    }

    public List<Funcionario> listarFuncionarios() {
        List<Funcionario> funcionarios = new ArrayList<>();
        for (Usuario usuario : listarUsuarios()) {
            if (usuario instanceof Funcionario) {
                funcionarios.add((Funcionario) usuario);
            }
        }
        return funcionarios;
    }

    public void alterarCargo(boolean gerente) {
        if (funcionarioLogado != null && !funcionarioLogado.isGerente()) {
            throw new IllegalStateException("Apenas gerentes podem alterar o cargo de funcionarios.");
        }
        this.gerente = gerente;
        Persistencia.salvarDados();
    }

    public Funcionario buscarFuncionario(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return null;
        }
        String cpfTratado = cpf.trim();
        for (Funcionario funcionario : listarFuncionarios()) {
            if (funcionario.getCpf() != null && funcionario.getCpf().equals(cpfTratado)) {
                return funcionario;
            }
        }
        return null;
    }

    @Override
    public void desativar() {
        if (funcionarioLogado != null && !funcionarioLogado.isGerente()) {
            throw new IllegalStateException("Apenas gerentes podem desativar funcionarios.");
        }
        super.desativar();
    }

    @Override
    public void reativar() {
        if (funcionarioLogado != null && !funcionarioLogado.isGerente()) {
            throw new IllegalStateException("Apenas gerentes podem reativar funcionarios.");
        }
        super.reativar();
    }

    public int getMatricula() {
        return this.matricula;
    }

    public String getEmail() {
        return this.email;
    }

    public String getSenha() {
        return this.senha;
    }

    public boolean isGerente() {
        return this.gerente;
    }

    protected static int obterProximaMatricula() {
        return proximaMatricula;
    }

    protected static void restaurarEstadoFuncionario(int novaProximaMatricula) {
        proximaMatricula = Math.max(1, novaProximaMatricula);
        funcionarioLogado = null;
    }
}
