package br.com.Veiculos_Empresarial.service;

import br.com.Veiculos_Empresarial.model.*;
import br.com.Veiculos_Empresarial.repository.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class RegistroUsoService {

    private RegistroUsoRepository registroUsoRepository;
    private VeiculoRepository veiculoRepository;
    private MotoristaRepository motoristaRepository;

    public RegistroUsoService() {
        this.veiculoRepository = new VeiculoRepository();
        this.motoristaRepository = new MotoristaRepository();
        this.registroUsoRepository = new RegistroUsoRepository(veiculoRepository, motoristaRepository);
    }

    public boolean motoristaTemViagemAtiva(int motoristaId) {
        return registroUsoRepository.motoristaTemViagemAtiva(motoristaId);
    }

    public RegistroUso registrarSaida(Veiculo veiculo, Motorista motorista, String destino) {
        validarDadosInicioUso(
                veiculo != null ? veiculo.getPlaca() : null,
                motorista != null ? motorista.getCnh() : null,
                destino);
        motoristaEstaUsandoVeiculo(motorista.getId());
        try {
            if (registroUsoRepository.motoristaTemViagemAtiva(motorista.getId())) {
                System.err.println(" ERRO: O motorista " + motorista.getNome() + " jÃ¡ possui uma viagem em andamento!");
                System.err.println(" Finalize a viagem atual antes de iniciar uma nova.");
                return null;
            }

            RegistroUso novoRegistro = new RegistroUso(veiculo, motorista, new Date(), veiculo.getQuilometragemAtual(),
                    destino);

            registroUsoRepository.salvar(novoRegistro);

            System.out.println("SERVICE (RegistroUso): SaÃ­da registrada com sucesso. ID: " + novoRegistro.getId());

            return novoRegistro;

        } catch (Exception e) {
            System.err.println(" Erro no serviÃ§o ao registrar saÃ­da: " + e.getMessage());
            return null;
        }
    }

    public boolean finalizarUsoVeiculo(int idRegistro, double quilometragemFinal) {
        try {
            RegistroUso registro = registroUsoRepository.buscarPorIdRobusto(idRegistro);
            if (registro == null) {
                System.err.println(" Registro com ID " + idRegistro + " nÃ£o encontrado");
                return false;
            }

            if (registro.getDataHoraRetorno() != null) {
                System.err.println(" Registro jÃ¡ foi finalizado anteriormente");
                return false;
            }

            if (quilometragemFinal < registro.getKmSaida()) {
                System.err.println(" Quilometragem final (" + quilometragemFinal
                        + ") nÃ£o pode ser menor que a inicial (" + registro.getKmSaida() + ")");
                return false;
            }

            registro.setDataHoraRetorno(new Date());
            registro.setKmRetorno(quilometragemFinal);

            boolean atualizouRegistro = registroUsoRepository.atualizar(registro);

            if (atualizouRegistro) {
                try {
                    Veiculo veiculo = registro.getVeiculo();
                    if (veiculo != null && !veiculo.getPlaca().equals("PLACA_AUSENTE")) {
                        // Busca o veÃ­culo real do banco
                        Veiculo veiculoReal = veiculoRepository.buscarPorId(veiculo.getId(), null);
                        if (veiculoReal != null) {
                            veiculoReal.setQuilometragemAtual(quilometragemFinal);
                            veiculoReal.setStatus(StatusVeiculo.DISPONIVEL);
                            veiculoRepository.atualizar(veiculoReal);

                            double kmRodados = calcularKmRodados(quilometragemFinal, registro.getKmSaida());

                            System.out.println(" Uso do veÃ­culo finalizado com sucesso!");
                            System.out.println("   Registro ID: " + idRegistro);
                            System.out.println("   VeÃ­culo: " + veiculoReal.getPlaca());
                            System.out.println("   KM rodados: " + kmRodados + " km");
                            System.out.println("   DuraÃ§Ã£o: " + calcularDuracaoUso(registro));
                        } else {
                            System.out.println(" Registro finalizado! (VeÃ­culo nÃ£o encontrado no sistema)");
                        }
                    } else {
                        System.out.println(" Registro Ã³rfÃ£o finalizado com sucesso!");
                    }
                } catch (Exception e) {
                    System.out.println(" Registro finalizado! (Erro ao atualizar veÃ­culo: " + e.getMessage() + ")");
                }

                return true;
            }

        } catch (Exception e) {
            System.err.println(" Erro ao finalizar uso do veÃ­culo: " + e.getMessage());
        }

        return false;
    }

    public double calcularKmRodados(double quilometragemFinal, double kmSaida) {
        return quilometragemFinal - kmSaida;
    }

    public List<RegistroUso> listarRegistrosAtivos() {
        return registroUsoRepository.listarTodosRegistrosUso().stream()
                .filter(registro -> registro.getDataHoraRetorno() == null)
                .toList();
    }

    public List<RegistroUso> listarRegistrosFinalizados() {
        return registroUsoRepository.listarTodosRegistrosUso().stream()
                .filter(registro -> registro.getDataHoraRetorno() != null)
                .toList();
    }

    public List<RegistroUso> buscarRegistrosPorMotorista(String cnhMotorista) {
        Motorista motorista = motoristaRepository.buscarPorCnh(cnhMotorista);
        if (motorista == null) {
            System.err.println(" Motorista nÃ£o encontrado com CNH: " + cnhMotorista);
            return new ArrayList<>();
        }

        return registroUsoRepository.buscarPorMotoristaId(motorista.getUsuarioId());
    }

    public List<RegistroUso> buscarRegistros(String placaVeiculo) {
        Veiculo veiculo = veiculoRepository.buscarVeiculoPorPlaca(placaVeiculo);
        if (veiculo == null) {
            System.err.println(" VeÃ­culo nÃ£o encontrado com placa: " + placaVeiculo);
            return new ArrayList<>();
        }

        return registroUsoRepository.buscarPorVeiculoId(veiculo.getId());
    }

    private void validarDadosInicioUso(String placaVeiculo, String cnhMotorista, String destinoOuFinalidade) {
        if (placaVeiculo == null || placaVeiculo.trim().isEmpty()) {
            throw new IllegalArgumentException("Placa do veÃ­culo Ã© obrigatÃ³ria");
        }

        if (cnhMotorista == null || cnhMotorista.trim().isEmpty()) {
            throw new IllegalArgumentException("CNH do motorista Ã© obrigatÃ³ria");
        }

        if (destinoOuFinalidade == null || destinoOuFinalidade.trim().isEmpty()) {
            throw new IllegalArgumentException("Destino ou finalidade Ã© obrigatÃ³rio");
        }
    }

    private boolean motoristaEstaUsandoVeiculo(int idMotorista) {
        return registroUsoRepository.listarTodosRegistrosUso().stream()
                .anyMatch(registro -> registro.getMotorista().getId() == idMotorista &&
                        registro.getDataHoraRetorno() == null);
    }

    private String calcularDuracaoUso(RegistroUso registro) {
        if (registro.getDataHoraRetorno() == null) {
            return "Em andamento";
        }

        long duracaoMs = registro.getDataHoraRetorno().getTime() -
                registro.getDataHoraSaida().getTime();

        long segundos = duracaoMs / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;

        if (horas > 0) {
            return horas + "h " + (minutos % 60) + "min";
        } else if (minutos > 0) {
            return minutos + "min " + (segundos % 60) + "s";
        } else {
            return segundos + "s";
        }
    }

    public List<RegistroUso> listarTodosRegistrosUso() {
        return registroUsoRepository.listarTodosRegistrosUso();
    }
    }
}