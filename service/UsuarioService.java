package service;

import repository.UsuarioRepository;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class UsuarioService {

    private MotoristaService motortistaService = new MotoristaService();
    private UsuarioRepository usuarioRepository = new UsuarioRepository();
    private VeiculoService veiculoService = new VeiculoService();
    private ManutencaoService manutencaoService = new ManutencaoService();
    private RegistroUsoService registroUsoService = new RegistroUsoService();

    public void cadastrarAdministrador(String nome, String username, String senha, String cargo) {
        Usuario usuarioExistente = usuarioRepository.buscarPorUsername(username);
        if (usuarioExistente != null) {
            System.err.println("ERRO no Cadastro: usuario " + username + " ja existe");
            return;
        }

        Administrador novoAdmin = new Administrador(nome, username, senha, cargo);

        usuarioRepository.salvarAdmin(novoAdmin);
        System.out.println("Administrador " + nome + " cadastrado com sucesso!");
    }

    public Usuario autenticar(String username, String senha) {
        Usuario usuarioEncontrado = usuarioRepository.buscarPorUsername(username);

        if (usuarioEncontrado == null) {
            System.out.println("Falha na autenticação: Usuário '" + username + "' não existe.");
            return null;
        }

        if (usuarioEncontrado.getSenha().equals(senha)) {
            System.out.println("Autenticacao bem-sucedida:" + username);
            return usuarioEncontrado;
        } else {
            System.out.println("Falha na autenticacao: senha incorreta.");
            return null;
        }
    }

    public List<Usuario> listarTodosUsuarios() {
        return usuarioRepository.listarTodosUsuarios();
    }
}
