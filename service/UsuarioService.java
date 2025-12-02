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
    public void cadastrarMotorista(String nome, String username, String senha, String setor, String cnh) {
        motortistaService.cadastrarMotorista(nome, username, senha, setor, cnh);
    }

    public void editarMotorista(String novoNome, String novoUsername, String novaSenha, String novoSetor, String cnh) {
        motortistaService.atualizarDadosDeMotorista(cnh, novoNome, novoSetor, novoUsername, novaSenha);
    }

    public List<Motorista> listarMotoristas() {
        List<Motorista> listaMotoristas = motortistaService.listarTodosMotoristas();
        return listaMotoristas;
    }

    public void excluirMotorista(String cnhParaRemover) {
        motortistaService.excluirMotorista(cnhParaRemover);
    }
    public boolean adicionarVeiculos(String placa, String modelo, String marca, int ano, String cor,
                                     double quilometragem) {
        veiculoService.cadastrarVeiculo(placa, modelo, marca, ano, cor, quilometragem);
        return true;
    }

    public void editarVeiculo(String placaParaEditar, String novoModelo, String novaMarca, int novoAno,
                              String novaCor) {
        veiculoService.atualizarVeiculo(placaParaEditar, novoModelo, novaMarca, novoAno, novaCor);
    }

    public void removerVeiculo(String placaParaRemover) {
        veiculoService.excluirVeiculo(placaParaRemover);
    }

    public List<Veiculo> listarTodosVeiculos() {
        return veiculoService.listarVeiculos();
    }

    public List<Manutencao> listarManutencao() {
        return manutencaoService.listarTodas();
    }

    public void iniciarManutencao(String placaVeiculo, String descricaoServico, String nomeOficina,
                                  Date dataSaidaPrevista, double custoPrevisto) {
        manutencaoService.iniciarManutencao(placaVeiculo, descricaoServico, nomeOficina, dataSaidaPrevista,
                custoPrevisto);
    }

    public void concluirManutencao(String placa, double custoReal) {
        Date horaSaida = new Date();
        manutencaoService.concluirManutencao(placa, horaSaida, custoReal);
    }

    public void excluirManutencao(String placaVeiculo) {
        manutencaoService.excluirManutencao(placaVeiculo);
    }

}
