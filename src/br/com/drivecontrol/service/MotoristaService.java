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
}
