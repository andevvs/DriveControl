package service;

import model.Manutencao;
import model.StatusVeiculo;
import model.Veiculo;
import repository.ManutencaoRepository;
import repository.VeiculoRepository;

import java.util.Date;

public class ManutencaoService {

    private ManutencaoRepository manutencaoRepository = new ManutencaoRepository();
    private VeiculoRepository veiculoRepository = new VeiculoRepository();

    public boolean iniciarManutencao(String placaVeiculo, String descricaoServico, String nomeOficina,
                                     Date dataSaidaPrevista, double custoPrevisto) {
        try {
            Veiculo veiculo = veiculoRepository.buscarVeiculoPorPlaca(placaVeiculo);
            if (veiculo == null) {
                throw new Exception("Veículo com placa " + placaVeiculo + " não encontrado.");
            }

            if (veiculo.getStatus() == StatusVeiculo.EM_USO) {
                throw new Exception("Veículo está em uso e não pode entrar em manutenção.");
            }
            if (veiculo.getStatus() == StatusVeiculo.MANUTENCAO) {
                throw new Exception("Veículo já está em manutenção.");
            }

            Manutencao novaManutencao = new Manutencao(veiculo, descricaoServico, nomeOficina, new Date(),
                    dataSaidaPrevista, custoPrevisto);

            manutencaoRepository.salvar(novaManutencao);

            veiculo.setStatus(StatusVeiculo.MANUTENCAO);
            veiculoRepository.atualizar(veiculo);

            System.out.println("Manutenção iniciada com sucesso para o veículo " + placaVeiculo);
            return true;

        } catch (Exception e) {
            System.err.println("Erro ao iniciar manutenção: " + e.getMessage());
            return false;
        }
    }
}