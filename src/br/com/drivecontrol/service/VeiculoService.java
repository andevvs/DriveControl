package br.com.drivecontrol.service;

import br.com.Veiculos_Empresarial.model.Veiculo;
import br.com.Veiculos_Empresarial.model.Motorista;
import br.com.Veiculos_Empresarial.model.RegistroUso;
import br.com.Veiculos_Empresarial.model.StatusVeiculo;
import br.com.Veiculos_Empresarial.repository.MotoristaRepository;
import br.com.Veiculos_Empresarial.repository.RegistroUsoRepository;
import br.com.Veiculos_Empresarial.repository.VeiculoRepository;
import java.util.Calendar;
import java.util.List;

public class VeiculoService {

    private VeiculoRepository veiculoRepository;
    private RegistroUsoRepository registroUsoRepository;
    private RegistroUsoService registroUsoService;

    public VeiculoService() {
        this.veiculoRepository = new VeiculoRepository();
        this.registroUsoRepository = new RegistroUsoRepository(new VeiculoRepository(), new MotoristaRepository());
        this.registroUsoService = new RegistroUsoService();
    }

    public boolean cadastrarVeiculo(String placa, String modelo, String marca, int ano, String cor,
                                    double quilometragemInicial) {
        try {
            Veiculo novoVeiculo = new Veiculo(placa, modelo, marca, ano, cor, StatusVeiculo.DISPONIVEL,
                    quilometragemInicial, null);

            validarVeiculo(novoVeiculo);

            Veiculo veiculoExistente = veiculoRepository.buscarVeiculoPorPlaca(placa);
            if (veiculoExistente != null) {
                System.err.println(" Erro: Placa '" + placa + "' jÃ¡ estÃ¡ cadastrada");
                return false;
            }

            veiculoRepository.salvar(novoVeiculo);

            System.out.println("   VeÃ­culo cadastrado com sucesso!");
            System.out.println("   Placa: " + placa);
            System.out.println("   Modelo: " + modelo + " " + marca);
            System.out.println("   Ano: " + ano);
            System.out.println("   Status: DISPONÃVEL");

            return true;

        } catch (Exception e) {
            System.err.println(" Erro ao cadastrar veÃ­culo: " + e.getMessage());
        }

        return false;
    }

    public RegistroUso usarVeiculo(String placa, Motorista motorista, String destino) {
        try {
            Veiculo veiculo = veiculoRepository.buscarVeiculoPorPlaca(placa);
            if (veiculo == null) {
                throw new Exception("VeÃ­culo com placa '" + placa + "' nÃ£o encontrado.");
            }

            if (veiculo.getStatus() != StatusVeiculo.DISPONIVEL) {
                throw new Exception("VeÃ­culo nÃ£o estÃ¡ disponÃ­vel. Status atual: " + veiculo.getStatus());
            }

            veiculo.setStatus(StatusVeiculo.EM_USO);
            veiculoRepository.atualizar(veiculo);
            System.out.println("SERVICE (Veiculo): Status do veÃ­culo " + placa + " atualizado para EM_USO.");

            RegistroUso novoRegistro = registroUsoService.registrarSaida(veiculo, motorista, destino);
            if (novoRegistro == null) {
                throw new Exception("Falha ao registrar a saÃ­da do veÃ­culo.");
            }
            return novoRegistro;
        } catch (Exception e) {
            System.err.println(" Erro no processo de usar veÃ­culo: " + e.getMessage());
            return null;
        }
    }

    public Veiculo buscarVeiculoPorPlaca(String placa) {
        return veiculoRepository.buscarVeiculoPorPlaca(placa);
    }

    public Veiculo buscarVeiculoPorId(int id) {
        return veiculoRepository.buscarPorId(id, null);
    }

    public List<Veiculo> listarVeiculos() {
        try {
            return veiculoRepository.findAll();
        } catch (Exception e) {
            System.err.println("Erro ao listar todos os veÃ­culos: " + e.getMessage());
            return List.of(); // Retorna lista vazia em caso de erro
        }
    }

    public List<Veiculo> listarVeiculosDisponiveis() {
        return veiculoRepository.listarVeiculosDisponiveis();
    }

    public boolean atualizarVeiculo(String placaParaBuscar, String novoModelo, String novaMarca, int novoAno,
                                    String novaCor) {
        Veiculo veiculoExistente = veiculoRepository.buscarVeiculoPorPlaca(placaParaBuscar);
        if (veiculoExistente == null) {
            System.err.println("SERVIÃO: Erro! VeÃ­culo com placa " + placaParaBuscar + " nÃ£o encontrado.");
            return false;
        }

        veiculoExistente.setModelo(novoModelo);
        veiculoExistente.setMarca(novaMarca);
        veiculoExistente.setAno(novoAno);
        veiculoExistente.setCor(novaCor);
        veiculoRepository.atualizar(veiculoExistente);
        return true;
    }

    public boolean atualizarStatus(String placa, StatusVeiculo novoStatus) {
        try {
            Veiculo veiculo = veiculoRepository.buscarVeiculoPorPlaca(placa);
            if (veiculo == null) {
                System.err.println(" VeÃ­culo com placa '" + placa + "' nÃ£o encontrado");
                return false;
            }

            StatusVeiculo statusAnterior = veiculo.getStatus();
            veiculo.setStatus(novoStatus);

            boolean atualizado = veiculoRepository.atualizar(veiculo);

            if (atualizado) {
                System.out.println(" Status do veÃ­culo atualizado!");
                System.out.println("   Placa: " + placa);
                System.out.println("   Status anterior: " + statusAnterior);
                System.out.println("   Novo status: " + novoStatus);
                return true;
            }

        } catch (Exception e) {
            System.err.println("Erro ao atualizar status do veÃ­culo: " + e.getMessage());
        }

        return false;
    }

    public boolean atualizarQuilometragem(String placa, double novaQuilometragem) {
        try {
            Veiculo veiculo = veiculoRepository.buscarVeiculoPorPlaca(placa);
            if (veiculo == null) {
                System.err.println(" VeÃ­culo com placa '" + placa + "' nÃ£o encontrado");
                return false;
            }

            validarNovaQuilometragem(veiculo.getQuilometragemAtual(), novaQuilometragem);

            double quilometragemAnterior = veiculo.getQuilometragemAtual();
            veiculo.setQuilometragemAtual(novaQuilometragem);

            boolean atualizado = veiculoRepository.atualizar(veiculo);

            if (atualizado) {
                double diferenca = novaQuilometragem - quilometragemAnterior;
                System.out.println("   Quilometragem atualizada!");
                System.out.println("   Placa: " + placa);
                System.out.println("   KM anterior: " + quilometragemAnterior);
                System.out.println("   KM atual: " + novaQuilometragem);
                System.out.println("   DiferenÃ§a: +" + diferenca + " km");
                return true;
            }

        } catch (Exception e) {
            System.err.println(" Erro ao atualizar quilometragem: " + e.getMessage());
        }

        return false;
    }

    public boolean excluirVeiculo(String placa) {
        Veiculo veiculo = veiculoRepository.buscarVeiculoPorPlaca(placa);
        if (veiculo == null) {
            System.err.println("ERRO: Veiculo com placa " + placa + " nao encontrado");
            return false;
        }

        boolean temRegistrosDeUso = registroUsoRepository.existsByMotoristaId(veiculo.getId());
        if (temRegistrosDeUso) {
            System.err.println(
                    "ERRO: Veiculo " + placa + " nao pode ser excluido, pois existe registros de uso associados");
            return false;
        }

        veiculoRepository.delete(veiculo.getId());
        return true;
    }

    public void validarVeiculo(Veiculo veiculo) {
        if (veiculo == null) {
            throw new IllegalArgumentException("VeÃ­culo nÃ£o pode ser nulo");
        }

        if (veiculo.getPlaca() == null || veiculo.getPlaca().trim().isEmpty()) {
            throw new IllegalArgumentException("Placa Ã© obrigatÃ³ria");
        }

        if (veiculo.getModelo() == null || veiculo.getModelo().trim().isEmpty()) {
            throw new IllegalArgumentException("Modelo Ã© obrigatÃ³rio");
        }

        if (veiculo.getMarca() == null || veiculo.getMarca().trim().isEmpty()) {
            throw new IllegalArgumentException("Marca Ã© obrigatÃ³ria");
        }

        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
        if (veiculo.getAno() < 1900 || veiculo.getAno() > anoAtual + 1) {
            throw new IllegalArgumentException("Ano deve estar entre 1900 e " + (anoAtual + 1));
        }

        if (veiculo.getQuilometragemAtual() < 0) {
            throw new IllegalArgumentException("Quilometragem nÃ£o pode ser negativa");
        }
    }

    public void validarNovaQuilometragem(double quilometragemAtual, double novaQuilometragem) {
        if (novaQuilometragem < 0) {
            throw new IllegalArgumentException("Quilometragem nÃ£o pode ser negativa");
        }

        if (novaQuilometragem < quilometragemAtual) {
            throw new IllegalArgumentException("Nova quilometragem nÃ£o pode ser menor que a atual");
        }
    }
}