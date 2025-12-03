package model;

import java.util.Scanner;

public abstract class Usuario {
    private int id;
    private String nome;
    private String username;
    private String senha;
    private boolean ativo;

    public Usuario(String nome, String username, String senha) {
        this.nome = nome;
        this.username = username;
        this.senha = senha;
        this.ativo = true;
    }

    public Usuario(int id, String nome, String username, String senha, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.username = username;
        this.senha = senha;
        this.ativo = ativo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean autenticar(String senha) {
        // A lógica de autenticação permanece a mesma
        return senha != null && senha.equals(this.senha);
    }

    @Override
    public String toString() {
        return "ID: " + this.id + ", Nome:" + this.nome + ", Username:" + this.username + ", Ativo: "
                + (ativo ? "sim" : "nao");
    }

    public abstract void exibirMenuPrincipal(Scanner input);

    public abstract String getDetalhes();
}