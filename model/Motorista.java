package model;

import Main;
import java.util.Scanner;

public class Motorista extends Usuario {
    private String setor;
    private String cnh;
    private boolean ativoMotorista;
    private int id;

    public Motorista(String nome, String username, String senha, String setor, String cnh) {
        super(nome, username, senha);
        this.setor = setor;
        this.cnh = cnh;
        this.ativoMotorista = true;

    }

    public Motorista(int motoristaId, String nome, String username, String senha, boolean ativoUsuario,
            String setor, String cnh, int usuarioId, boolean ativoMotorista) {
        super(usuarioId, nome, username, senha, ativoUsuario);
        this.id = motoristaId;
        this.setor = setor;
        this.cnh = cnh;
        this.ativoMotorista = ativoMotorista;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return super.getId();
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getCnh() {
        return cnh;
    }

    public void setCnh(String cnh) {
        this.cnh = cnh;
    }

    public boolean isAtivoMotorista() {
        return ativoMotorista;
    }

    public void setAtivoMotorista(boolean ativoMotorista) {
        this.ativoMotorista = ativoMotorista;
    }

    public Usuario getUsuario() {
        return this;
    }

    @Override
    public String toString() {
        return "Motorista{" +
                "id_motorista=" + id +
                ", setor='" + setor + '\'' +
                ", cnh='" + cnh + '\'' +
                ", " + super.toString() +
                '}';
    }

    @Override
    public String getDetalhes() {
        return String.format("[MOTORISTA] ID: %d, Nome: %s, CNH: %s, Setor: %s",
                super.getId(), getNome(), getCnh(), getSetor());
    }

    @Override
    public void exibirMenuPrincipal(Scanner input) {
        Main.menuMotorista(this, input);
    }
}