package model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Cliente extends Usuario {
    private static final long serialVersionUID = 1L;

    private String numeroCnh;
    private Date validadeCnh;

    public Cliente() {
        super();
    }

    public Cliente(String nome, String cpf, String telefone, String numeroCnh, Date validadeCnh) {
        super(nome, cpf, telefone);
        this.numeroCnh = numeroCnh;
        this.validadeCnh = (validadeCnh != null) ? new Date(validadeCnh.getTime()) : null;
    }

    public Cliente(int id, String nome, String cpf, String telefone, boolean ativo, String numeroCnh, Date validadeCnh) {
        super(id, nome, cpf, telefone, ativo);
        this.numeroCnh = numeroCnh;
        this.validadeCnh = (validadeCnh != null) ? new Date(validadeCnh.getTime()) : null;
    }

    public Cliente cadastrarCliente(String nome, String cpf, String telefone, String numeroCnh, Date validadeCnh) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome nao pode ser vazio.");
        }
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF nao pode ser vazio.");
        }
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new IllegalArgumentException("Telefone nao pode ser vazio.");
        }
        if (numeroCnh == null || numeroCnh.trim().isEmpty()) {
            throw new IllegalArgumentException("Numero da CNH nao pode ser vazio.");
        }
        if (validadeCnh == null) {
            throw new IllegalArgumentException("Validade da CNH nao pode ser nula.");
        }
        String cpfTratado = cpf.trim();
        String cnhTratada = numeroCnh.trim();
        for (Cliente existente : listarClientes()) {
            if (existente.getCpf() != null && existente.getCpf().equals(cpfTratado)) {
                throw new IllegalArgumentException("Ja existe um cliente cadastrado com este CPF.");
            }
            if (existente.numeroCnh != null && existente.numeroCnh.equals(cnhTratada)) {
                throw new IllegalArgumentException("Ja existe um cliente cadastrado com esta CNH.");
            }
        }
        Cliente novoCliente = new Cliente(
                nome.trim(),
                cpfTratado,
                telefone.trim(),
                cnhTratada,
                validadeCnh
        );
        cadastrarUsuario(novoCliente);
        return novoCliente;
    }

    public void visualizarCliente() {
        SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");
        String dataFormatada = (this.validadeCnh != null) ? formatoData.format(this.validadeCnh) : "Nao informada";
        super.visualizarDados();
        System.out.println("Numero CNH: " + this.numeroCnh);
        System.out.println("Validade CNH: " + dataFormatada);
        System.out.println("Situacao CNH: " + (cnhEstaValida() ? "Valida" : "Vencida"));
    }

    public void atualizarCliente(String nome, String telefone, String cpf, String senha) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome nao pode ser vazio.");
        }
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new IllegalArgumentException("Telefone nao pode ser vazio.");
        }
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF nao pode ser vazio.");
        }
        String cpfTratado = cpf.trim();
        for (Cliente existente : listarClientes()) {
            if (existente != this && existente.getCpf() != null && existente.getCpf().equals(cpfTratado)) {
                throw new IllegalArgumentException("Ja existe outro cliente com este CPF.");
            }
        }
        super.atualizarDados(nome, telefone);
        setCpf(cpfTratado);
    }

    public List<Cliente> listarClientes() {
        List<Cliente> clientes = new ArrayList<>();
        for (Usuario usuario : listarUsuarios()) {
            if (usuario instanceof Cliente) {
                clientes.add((Cliente) usuario);
            }
        }
        return clientes;
    }

    public void atualizarCnh(String numeroCnh, Date validadeCnh) {
        if (numeroCnh == null || numeroCnh.trim().isEmpty()) {
            throw new IllegalArgumentException("Numero da CNH nao pode ser vazio.");
        }
        if (validadeCnh == null) {
            throw new IllegalArgumentException("Validade da CNH nao pode ser nula.");
        }
        String cnhTratada = numeroCnh.trim();
        for (Cliente existente : listarClientes()) {
            if (existente != this && existente.numeroCnh != null && existente.numeroCnh.equals(cnhTratada)) {
                throw new IllegalArgumentException("Ja existe outro cliente com esta CNH.");
            }
        }
        this.numeroCnh = cnhTratada;
        this.validadeCnh = new Date(validadeCnh.getTime());
    }

    public boolean cnhEstaValida() {
        if (this.validadeCnh == null) {
            return false;
        }
        return !this.validadeCnh.before(new Date());
    }

    public Cliente buscarCliente(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return null;
        }
        String cpfTratado = cpf.trim();
        for (Cliente cliente : listarClientes()) {
            if (cliente.getCpf() != null && cliente.getCpf().equals(cpfTratado)) {
                return cliente;
            }
        }
        return null;
    }

    public String getNumeroCnh() {
        return this.numeroCnh;
    }

    public Date getValidadeCnh() {
        return (this.validadeCnh != null) ? new Date(this.validadeCnh.getTime()) : null;
    }
}
