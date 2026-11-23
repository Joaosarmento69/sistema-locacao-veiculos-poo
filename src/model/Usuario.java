package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import persistence.Persistencia;

public abstract class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    private static List<Usuario> usuarios = new ArrayList<>();
    private static int proximoId = 1;

    private int id;
    private String nome;
    private String cpf;
    private String telefone;
    private boolean ativo;

    public Usuario() {
        this.ativo = true;
    }

    public Usuario(String nome, String cpf, String telefone) {
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.ativo = true;
    }

    public Usuario(int id, String nome, String cpf, String telefone, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.ativo = ativo;
    }

    public void cadastrarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario nao pode ser nulo.");
        }
        for (Usuario existente : usuarios) {
            if (existente == usuario) {
                return;
            }
            if (existente.getClass().equals(usuario.getClass())
                    && existente.cpf != null
                    && usuario.cpf != null
                    && existente.cpf.equals(usuario.cpf)) {
                throw new IllegalArgumentException("Ja existe um usuario cadastrado com este CPF.");
            }
        }
        if (usuario.id <= 0) {
            usuario.id = proximoId++;
        } else if (usuario.id >= proximoId) {
            proximoId = usuario.id + 1;
        }
        usuarios.add(usuario);
        Persistencia.salvarDados();
    }

    public void visualizarDados() {
        System.out.println("ID: " + this.id);
        System.out.println("Nome: " + this.nome);
        System.out.println("CPF: " + this.cpf);
        System.out.println("Telefone: " + this.telefone);
        System.out.println("Status: " + (this.ativo ? "Ativo" : "Inativo"));
    }

    public void atualizarDados(String nome, String telefone) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome nao pode ser vazio.");
        }
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new IllegalArgumentException("Telefone nao pode ser vazio.");
        }
        this.nome = nome.trim();
        this.telefone = telefone.trim();
        Persistencia.salvarDados();
    }

    public void desativar() {
        this.ativo = false;
        Persistencia.salvarDados();
    }

    public void reativar() {
        this.ativo = true;
        Persistencia.salvarDados();
    }

    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios);
    }

    public int getId() {
        return this.id;
    }

    public String getNome() {
        return this.nome;
    }

    public String getCpf() {
        return this.cpf;
    }

    public String getTelefone() {
        return this.telefone;
    }

    public boolean isAtivo() {
        return this.ativo;
    }

    protected void setId(int id) {
        this.id = id;
    }

    protected void setNome(String nome) {
        this.nome = nome;
    }

    protected void setCpf(String cpf) {
        this.cpf = cpf;
    }

    protected void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    protected void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    protected static List<Usuario> obterListaUsuarios() {
        return new ArrayList<>(usuarios);
    }

    protected static int obterProximoId() {
        return proximoId;
    }

    protected static void restaurarUsuarios(List<Usuario> lista, int novoProximoId) {
        usuarios = (lista != null) ? new ArrayList<>(lista) : new ArrayList<>();
        proximoId = Math.max(1, novoProximoId);
    }
}
