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
    public boolean iniciarViagem(Motorista motoristaUsuario, String placa, String destino) {
        if (motoristaUsuario == null) {
            System.out.println(" Erro: Usuário não é um motorista válido!");
            return false;
        }

        RegistroUsoService registroUsoService = new RegistroUsoService();
        if (registroUsoService.motoristaTemViagemAtiva(motoristaUsuario.getId())) {
            System.err.println(" ERRO: Você já possui uma viagem em andamento!");
            System.err.println(" Finalize a viagem atual antes de iniciar uma nova.");
            System.err.println(" Vá para a opção 'FINALIZAR USO DE VEÍCULO' para concluir sua viagem atual.");
            return false;
        }

        RegistroUso novoRegistro = veiculoService.usarVeiculo(placa, motoristaUsuario, destino);

        return novoRegistro != null;
    }
    public Motorista buscarMotorista(String cnh) {
        if (cnh == null || cnh.trim().isEmpty()) {
            System.err.println(" CNH é obrigatória para busca");
            return null;
        }

        Motorista motorista = motoristaRepository.buscarPorCnh(cnh);

        if (motorista == null) {
            System.err.println(" Motorista com CNH " + cnh + " não encontrado");
        }

        return motorista;
    }

    public Motorista buscarMotoristaPorId(int id) {
        return motoristaRepository.buscarPorId(id);
    }

    public List<Motorista> listarTodosMotoristas() {
        List<Motorista> listaMotoristas = motoristaRepository.listarTodos();

        if (listaMotoristas.isEmpty()) {
            System.out.println(" Nenhum motorista cadastrado no sistema");
        } else {
            System.out.println(" Total de motoristas: " + listaMotoristas.size());
        }

        return listaMotoristas;
    }
    public void excluirMotorista(String cnh) {
        Motorista motorista = motoristaRepository.buscarPorCnh(cnh);
        if (motorista == null) {
            System.err.println("ERRO: Motorista com CNH " + cnh + " não encontrado.");
            return;
        }

        boolean temRegistros = registroUsoRepository.existsByMotoristaId(motorista.getId());

        if (temRegistros) {
            System.err.println("ERRO: O motorista '" + motorista.getNome()
                    + "' não pode ser excluído, pois possui registros de uso associados.");
            return;
        }

        System.out.println("SERVICE: Motorista pode ser excluído. Solicitando remoção ao repositório...");
        motoristaRepository.remover(cnh);
    }
}
