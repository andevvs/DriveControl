package model;

import app.Main;
import java.util.Scanner;

public class Administrador extends Usuario {

    private String cargo;

    public Administrador(String nome, String username, String senha, String cargo) {
        super(nome, username, senha);
        this.cargo = cargo;
    }

    public Administrador(int id, String nome, String username, String senha, boolean ativo, String cargo) {
        super(id, nome, username, senha, ativo);
        this.cargo = cargo;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    @Override
    public String getDetalhes() {
        return String.format("[ADMIN] ID: %d, Nome: %s, Cargo: %s",
                super.getId(), getNome(), getCargo());
    }

    @Override
    public void exibirMenuPrincipal(Scanner input) {
        Main.menuAdmin(this, input);
    }
}