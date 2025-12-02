package service;

import model.Manutencao;
import model.StatusVeiculo;
import model.Veiculo;
import repository.ManutencaoRepository;
import repository.VeiculoRepository;

public class ManutencaoService {

    private ManutencaoRepository manutencaoRepository = new ManutencaoRepository();
    private VeiculoRepository veiculoRepository = new VeiculoRepository();
}