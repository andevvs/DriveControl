package br.com.Veiculos_Empresarial.service;

import br.com.Veiculos_Empresarial.repository.MotoristaRepository;
import br.com.Veiculos_Empresarial.repository.RegistroUsoRepository;
import br.com.Veiculos_Empresarial.repository.VeiculoRepository;
import br.com.Veiculos_Empresarial.model.Motorista;
import br.com.Veiculos_Empresarial.model.RegistroUso;

import java.util.List;

public class MotoristaService {

    private MotoristaRepository motoristaRepository;
    private RegistroUsoRepository registroUsoRepository;
    private VeiculoRepository veiculoRepository;
    private VeiculoService veiculoService;

    public MotoristaService() {
        this.motoristaRepository = new MotoristaRepository();
        this.veiculoRepository = new VeiculoRepository();
        this.registroUsoRepository = new RegistroUsoRepository(this.veiculoRepository, this.motoristaRepository);
        this.veiculoService = new VeiculoService();
    }

    public boolean cadastrarMotorista(String nome, String userName, String senha, String setor, String cnh) {
        try {
            validarDadosMotorista(nome, userName, senha, setor, cnh);

            if (motoristaRepository.buscarPorCnh(cnh) != null) {
                System.err.println(" ERRO: CNH " + cnh + " já está cadastrada");
                return false;
            }

            Motorista novoMotorista = new Motorista(nome, userName, senha, setor, cnh);
            motoristaRepository.salvar(novoMotorista);

            System.out.println(" Motorista cadastrado com sucesso!");
            System.out.println("   Nome: " + nome);
            System.out.println("   CNH: " + cnh);
            System.out.println("   Setor: " + setor);
            System.out.println("   Username: " + userName);

            return true;

        } catch (Exception e) {
            System.err.println(" Erro ao cadastrar motorista: " + e.getMessage());
        }

        return false;
    }
    public boolean atualizarDadosDeMotorista(String cnhDoMotorista, String novoNome, String novoSetor,
                                             String novoUsername, String novaSenha) {
        try {
            Motorista motoristaParaAtualizar = motoristaRepository.buscarPorCnh(cnhDoMotorista);

            if (motoristaParaAtualizar == null) {
                System.err
                        .println(" ERRO: Motorista com CNH " + cnhDoMotorista + " não encontrado. Atualização falhou");
                return false;
            }

            validarDadosMotorista(novoNome, novoUsername, novaSenha, novoSetor, cnhDoMotorista);

            System.out.println("Dados antigos: " + motoristaParaAtualizar);

            motoristaParaAtualizar.setNome(novoNome);
            motoristaParaAtualizar.setUsername(novoUsername);
            motoristaParaAtualizar.setSetor(novoSetor);
            motoristaParaAtualizar.setSenha(novaSenha);

            System.out.println("Dados novos: " + motoristaParaAtualizar);

            motoristaRepository.atualizar(motoristaParaAtualizar);

            System.out.println(" Motorista atualizado com sucesso!");
            return true;

        } catch (Exception e) {
            System.err.println(" Erro ao atualizar motorista: " + e.getMessage());
        }

        return false;
    }
}
