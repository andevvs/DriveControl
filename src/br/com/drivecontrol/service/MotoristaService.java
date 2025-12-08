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
}

