
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