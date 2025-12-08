package br.com.drivecontrol.app;

import br.com.drivecontrol.service.*;
import br.com.drivecontrol.database.DatabaseConnection;
import br.com.drivecontrol.model.*;

import java.util.Scanner;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Classe principal da aplicação DriveControl.
 * Responsável pela interação com o usuário via terminal (Console UI).
 * * Esta classe orquestra os menus, captura entradas, realiza validações
 * de interface e delega as regras de negócio para as classes de Serviço.
 * * @version 2.0.0
 */
public class Main {

    private static UsuarioService usuarioService = new UsuarioService();
    private static MotoristaService motoristaService = new MotoristaService();
    private static VeiculoService veiculoService = new VeiculoService();
    private static RegistroUsoService registroUsoService = new RegistroUsoService();

    /**
     * Ponto de entrada da aplicação.
     * Inicializa o banco de dados e exibe o loop do menu principal.
     * * @param args Argumentos de linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        // Inicializa a conexão Singleton com o banco de dados
        DatabaseConnection.getInstance();
        Scanner input = new Scanner(System.in);
        int opcao;

        // Loop principal do programa
        do {
            limparTela();
            imprimirLogo();
            imprimirCabecalho("MENU DE ACESSO");
            
            System.out.println("  [1] Cadastrar Administrador (Primeiro Acesso)");
            System.out.println("  [2] Fazer Login no Sistema");
            System.out.println("  [0] Encerrar Aplicação");
            
            opcao = lerInteiro("Selecione uma opção", input);

            switch (opcao) {
                case 1:
                    realizarCadastroAdmin(input);
                    break;
                case 2:
                    realizarLogin(input);
                    break;
                case 0:
                    imprimirAviso("Encerrando o sistema DriveControl...");
                    pausar(1000);
                    break;
                default:
                    imprimirErro("Opção inválida! Tente novamente.");
                    break;
            }
        } while (opcao != 0);

        input.close();
        DatabaseConnection.getInstance().closeConnection();
        System.out.println("\nSistema finalizado. Até logo!");
    }

    // ==================================================================================
    // FUNCIONALIDADES PRINCIPAIS (LOGIN E CADASTRO)
    // ==================================================================================

    /**
     * Fluxo de cadastro de um novo Administrador.
     * Utiliza validadores robustos para impedir campos vazios.
     */
    private static void realizarCadastroAdmin(Scanner input) {
        limparTela();
        imprimirCabecalho("CADASTRO DE ADMINISTRADOR");

        String nome = lerTextoObrigatorio("Nome Completo", input);
        String username = lerTextoObrigatorio("Nome de Usuário (Login)", input);
        String senha = lerTextoObrigatorio("Senha de Acesso", input);
        String cargo = lerTextoObrigatorio("Cargo (ex: Gerente)", input);

        System.out.println("\nProcessando cadastro...");
        pausar(500);
        
        // Chama o serviço
        usuarioService.cadastrarAdministrador(nome, username, senha, cargo);
        
        esperarEnter(input);
    }

    /**
     * Fluxo de autenticação no sistema.
     * Implementa o Polimorfismo ao direcionar para o menu correto.
     */
    private static void realizarLogin(Scanner input) {
        limparTela();
        imprimirCabecalho("AUTENTICAÇÃO DO SISTEMA");

        String username = lerTextoObrigatorio("Usuário", input);
        
        System.out.print(" >> Senha: ");
        String senha = input.nextLine(); 

        System.out.println("\nVerificando credenciais...");
        pausar(800);

        Usuario usuarioLogado = usuarioService.autenticar(username, senha);

        if (usuarioLogado != null) {
            imprimirSucesso("Login realizado com sucesso! Bem-vindo, " + usuarioLogado.getNome());
            pausar(1000);
            
            // ==========================================================
            // POLIMORFISMO (O "Teste de Ouro")
            // O sistema não sabe se é Admin ou Motorista aqui.
            // O objeto decide qual menu exibir.
            // ==========================================================
            usuarioLogado.exibirMenuPrincipal(input);
            
        } else {
            esperarEnter(input);
        }
    }

    // ==================================================================================
    // MENUS POLIMÓRFICOS (CHAMADOS PELAS CLASSES MODELO)
    // ==================================================================================

    /**
     * Exibe o painel de controle completo para Administradores.
     * Permite gerenciar todas as entidades do sistema.
     * * @param admin O objeto Administrador logado.
     * @param input Scanner para entrada de dados.
     */
    public static void menuAdmin(Administrador admin, Scanner input) {
        int opcao;
        do {
            limparTela();
            imprimirCabecalho("PAINEL DO ADMINISTRADOR");
            System.out.println(" Usuário: " + admin.getNome() + " | Cargo: " + admin.getCargo());
            System.out.println("──────────────────────────────────────────────────────────────────");
            
            System.out.println("  [1] Gestão de Motoristas");
            System.out.println("  [2] Gestão de Veículos");
            System.out.println("  [3] Controle de Manutenção");
            System.out.println("  [4] Auditoria de Viagens (Histórico)");
            System.out.println("  [5] Relatório Geral de Usuários");
            System.out.println("  [0] Sair (Logout)");
            
            opcao = lerInteiro("Selecione uma opção", input);

            switch (opcao) {
                case 1:
                    menuGerenciamentoDeMotorista(input);
                    break;
                case 2:
                    menuGerenciamentoVeiculos(input);
                    break;
                case 3:
                    menuControleDeManutencao(input);
                    break;
                case 4:
                    menuHistoricoViagens(input);
                    break;
                case 5:
                    listarTodosUsuarios();
                    esperarEnter(input);
                    break;
                case 0:
                    imprimirAviso("Realizando logout...");
                    pausar(800);
                    break;
                default:
                    imprimirErro("Opção inválida.");
                    break;
            }
        } while (opcao != 0);
    }

    /**
     * Exibe o painel operacional para Motoristas.
     * Focado em registro de uso de veículos.
     * * @param motorista O objeto Motorista logado.
     * @param input Scanner para entrada de dados.
     */
    public static void menuMotorista(Motorista motorista, Scanner input) {
        int opcao;
        do {
            limparTela();
            imprimirCabecalho("ÁREA DO MOTORISTA");
            System.out.println(" Bem-vindo, " + motorista.getNome());
            System.out.println(" CNH: " + motorista.getCnh() + " | Setor: " + motorista.getSetor());
            System.out.println("──────────────────────────────────────────────────────────────────");
            
            System.out.println("  [1] Consultar Veículos Disponíveis");
            System.out.println("  [2] Iniciar Nova Viagem");
            System.out.println("  [3] Finalizar Viagem Atual");
            System.out.println("  [0] Sair (Logout)");
            
            opcao = lerInteiro("Selecione uma ação", input);

            switch (opcao) {
                case 1:
                    listarVeiculosDisponiveis();
                    break;
                case 2:
                    fluxoIniciarViagem(motorista, input);
                    break;
                case 3:
                    fluxoFinalizarViagem(input);
                    break;
                case 0:
                    imprimirAviso("Até logo, " + motorista.getNome() + "!");
                    pausar(800);
                    break;
                default:
                    imprimirErro("Opção inválida.");
                    break;
            }
            if (opcao != 0) esperarEnter(input);
            
        } while (opcao != 0);
    }

    // ==================================================================================
    // SUB-MENUS DE GESTÃO (ADMINISTRADOR)
    // ==================================================================================

    private static void menuGerenciamentoDeMotorista(Scanner input) {
        int opcao;
        do {
            limparTela();
            imprimirCabecalho("GESTÃO DE MOTORISTAS");
            System.out.println("  [1] Cadastrar Novo Motorista");
            System.out.println("  [2] Editar Dados de Motorista");
            System.out.println("  [3] Listar Todos os Motoristas");
            System.out.println("  [4] Remover Motorista");
            System.out.println("  [0] Voltar ao Menu Anterior");

            opcao = lerInteiro("Opção", input);

            if (opcao == 1) {
                imprimirCabecalho("NOVO MOTORISTA");
                String nome = lerTextoObrigatorio("Nome", input);
                String user = lerTextoObrigatorio("Username", input);
                String pass = lerTextoObrigatorio("Senha", input);
                String setor = lerTextoObrigatorio("Setor", input);
                String cnh = lerTextoObrigatorio("CNH (11 dígitos)", input);

                usuarioService.cadastrarMotorista(nome, user, pass, setor, cnh);
                esperarEnter(input);

            } else if (opcao == 2) {
                String buscaCnh = lerTextoObrigatorio("Digite a CNH do motorista para editar", input);
                // Aqui poderíamos buscar o motorista antes para mostrar os dados atuais
                System.out.println(">> Insira os novos dados:");
                String nome = lerTextoObrigatorio("Novo Nome", input);
                String user = lerTextoObrigatorio("Novo Username", input);
                String pass = lerTextoObrigatorio("Nova Senha", input);
                String setor = lerTextoObrigatorio("Novo Setor", input);

                usuarioService.editarMotorista(nome, user, pass, setor, buscaCnh);
                esperarEnter(input);

            } else if (opcao == 3) {
                listarMotoristasFormatado();
                esperarEnter(input);

            } else if (opcao == 4) {
                String cnh = lerTextoObrigatorio("CNH do Motorista a remover", input);
                if(confirmarAcao("Tem certeza que deseja excluir este motorista?", input)) {
                    usuarioService.excluirMotorista(cnh);
                }
                esperarEnter(input);

            } else if (opcao != 0) {
                imprimirErro("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private static void menuGerenciamentoVeiculos(Scanner input) {
        int opcao;
        do {
            limparTela();
            imprimirCabecalho("GESTÃO DE VEÍCULOS");
            System.out.println("  [1] Cadastrar Veículo");
            System.out.println("  [2] Editar Veículo");
            System.out.println("  [3] Remover Veículo");
            System.out.println("  [4] Listar Toda a Frota");
            System.out.println("  [0] Voltar");

            opcao = lerInteiro("Opção", input);

            switch (opcao) {
                case 1:
                    imprimirCabecalho("NOVO VEÍCULO");
                    String placa = lerTextoObrigatorio("Placa", input);
                    String modelo = lerTextoObrigatorio("Modelo", input);
                    String marca = lerTextoObrigatorio("Marca", input);
                    int ano = lerInteiro("Ano Fabricação", input);
                    String cor = lerTextoObrigatorio("Cor", input);
                    double km = lerDouble("Quilometragem Inicial", input);

                    if(usuarioService.adicionarVeiculos(placa, modelo, marca, ano, cor, km)) {
                        imprimirSucesso("Veículo cadastrado na frota!");
                    }
                    break;
                case 2:
                    String buscaPlaca = lerTextoObrigatorio("Placa do veículo para editar", input);
                    String novoModelo = lerTextoObrigatorio("Novo Modelo", input);
                    String novaMarca = lerTextoObrigatorio("Nova Marca", input);
                    int novoAno = lerInteiro("Novo Ano", input);
                    String novaCor = lerTextoObrigatorio("Nova Cor", input);

                    usuarioService.editarVeiculo(buscaPlaca, novoModelo, novaMarca, novoAno, novaCor);
                    break;
                case 3:
                    String placaDel = lerTextoObrigatorio("Placa do veículo a remover", input);
                    if(confirmarAcao("Remover veículo permanentemente?", input)) {
                        usuarioService.removerVeiculo(placaDel);
                    }
                    break;
                case 4:
                    listarFrotaCompleta();
                    break;
                case 0: break;
                default: imprimirErro("Opção inválida.");
            }
            if(opcao != 0) esperarEnter(input);
        } while (opcao != 0);
    }