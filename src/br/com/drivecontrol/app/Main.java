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

        private static void menuControleDeManutencao(Scanner input) {
            int opcao;
            do {
                limparTela();
                imprimirCabecalho("CONTROLE DE MANUTENÇÃO");
                System.out.println("  [1] Iniciar Manutenção (Enviar p/ Oficina)");
                System.out.println("  [2] Finalizar Manutenção (Receber da Oficina)");
                System.out.println("  [3] Listar Histórico de Manutenções");
                System.out.println("  [4] Cancelar/Excluir Manutenção");
                System.out.println("  [0] Voltar");

                opcao = lerInteiro("Opção", input);

                switch(opcao) {
                    case 1:
                        String placa = lerTextoObrigatorio("Placa do Veículo", input);
                        String desc = lerTextoObrigatorio("Descrição do Serviço", input);
                        String oficina = lerTextoObrigatorio("Nome da Oficina", input);
                        double custoPrev = lerDouble("Custo Previsto (R$)", input);
                        LocalDate dataPrev = lerData("Previsão de Saída (dd/MM/yyyy)", input);

                        usuarioService.iniciarManutencao(placa, desc, oficina, java.sql.Date.valueOf(dataPrev), custoPrev);
                        break;
                    case 2:
                        String placaFim = lerTextoObrigatorio("Placa do Veículo", input);
                        double custoReal = lerDouble("Custo Final Real (R$)", input);
                        usuarioService.concluirManutencao(placaFim, custoReal);
                        break;
                    case 3:
                        listarManutencoesFormatado();
                        break;
                    case 4:
                        String placaDel = lerTextoObrigatorio("Placa do Veículo em Manutenção", input);
                        usuarioService.excluirManutencao(placaDel);
                        break;
                    case 0: break;
                    default: imprimirErro("Opção inválida.");
                }
                if(opcao != 0) esperarEnter(input);
            } while(opcao != 0);
        }

        private static void menuHistoricoViagens(Scanner input) {
            int opcao;
            do {
                limparTela();
                imprimirCabecalho("AUDITORIA DE VIAGENS");
                System.out.println("  [1] Histórico Completo");
                System.out.println("  [2] Filtrar por Veículo");
                System.out.println("  [3] Filtrar por Motorista");
                System.out.println("  [4] Excluir Registro (Correção)");
                System.out.println("  [0] Voltar");

                opcao = lerInteiro("Opção", input);

                switch(opcao) {
                    case 1:
                        imprimirCabecalho("HISTÓRICO GERAL");
                        listarHistorico(usuarioService.visualizarHistoricoCompleto());
                        break;
                    case 2:
                        String placa = lerTextoObrigatorio("Digite a Placa", input);
                        listarHistorico(usuarioService.visualizarHistoricoPorVeiculo(placa));
                        break;
                    case 3:
                        String cnh = lerTextoObrigatorio("Digite a CNH", input);
                        listarHistorico(usuarioService.visualizarHistoricoPorMotorista(cnh));
                        break;
                    case 4:
                        int idReg = lerInteiro("ID do Registro para excluir", input);
                        if(confirmarAcao("Isso apagará o registro de viagem. Confirma?", input)) {
                            if(usuarioService.excluiRegistroUso(idReg)) {
                                imprimirSucesso("Registro excluído.");
                            } else {
                                imprimirErro("Falha ao excluir. Verifique o ID.");
                            }
                        }
                        break;
                    case 0: break;
                    default: imprimirErro("Opção inválida.");
                }
                if(opcao != 0) esperarEnter(input);
            } while(opcao != 0);
        }

        // ==================================================================================
        // MÉTODOS DE SUPORTE ÀS FUNCIONALIDADES (FLUXOS)
        // ==================================================================================

        private static void fluxoIniciarViagem(Motorista motorista, Scanner input) {
            imprimirCabecalho("NOVA VIAGEM");
            String placa = lerTextoObrigatorio("Placa do Veículo", input);
            String destino = lerTextoObrigatorio("Destino ou Finalidade", input);

            System.out.println("Solicitando liberação do veículo...");
            pausar(500);
            motoristaService.iniciarViagem(motorista, placa, destino);
        }

        private static void fluxoFinalizarViagem(Scanner input) {
            imprimirCabecalho("FINALIZAR VIAGEM");
            int idRegistro = lerInteiro("ID do Registro de Uso", input);
            double kmFinal = lerDouble("Quilometragem no Painel (Chegada)", input);

            System.out.println("Processando devolução...");
            pausar(500);

            if (registroUsoService.finalizarUsoVeiculo(idRegistro, kmFinal)) {
                imprimirSucesso("Veículo devolvido e quilometragem atualizada!");
            } else {
                imprimirErro("Não foi possível finalizar. Verifique ID e KM.");
            }
        }

        private static void listarMotoristasFormatado() {
            imprimirCabecalho("LISTA DE MOTORISTAS");
            List<Motorista> lista = usuarioService.listarMotoristas();

            if (lista.isEmpty()) {
                imprimirAviso("Nenhum motorista encontrado.");
                return;
            }

            System.out.printf(" %-5s | %-20s | %-15s | %-15s | %s\n", "ID", "NOME", "CNH", "SETOR", "USERNAME");
            System.out.println(" ──────────────────────────────────────────────────────────────────────────");
            for(Motorista m : lista) {
                System.out.printf(" %-5d | %-20s | %-15s | %-15s | %s\n",
                        m.getUsuarioId(), m.getNome(), m.getCnh(), m.getSetor(), m.getUsername());
            }
            System.out.println(" ──────────────────────────────────────────────────────────────────────────");
        }

        private static void listarFrotaCompleta() {
            imprimirCabecalho("FROTA DE VEÍCULOS");
            List<Veiculo> frota = usuarioService.listarTodosVeiculos();

            // Ordena por placa (usando lambda)
            frota.sort((v1, v2) -> v1.getPlaca().compareTo(v2.getPlaca()));

            if (frota.isEmpty()) {
                imprimirAviso("Nenhum veículo cadastrado.");
                return;
            }

            for (Veiculo v : frota) {
                System.out.printf(" >> [%s] %s %s (%d)\n", v.getStatus(), v.getMarca(), v.getModelo(), v.getAno());
                System.out.printf("    Placa: %s | KM: %.1f\n", v.getPlaca(), v.getQuilometragemAtual());
                System.out.println("    ────────────────────────────────");
            }
        }

    }

    private static void listarVeiculosDisponiveis() {
        imprimirCabecalho("VEÃCULOS DISPONÃVEIS");
        List<Veiculo> disponiveis = veiculoService.listarVeiculosDisponiveis();

        if (disponiveis.isEmpty()) {
            imprimirAviso("Nenhum veÃ­culo disponÃ­vel no pÃ¡tio.");
            return;
        }

        for (Veiculo v : disponiveis) {
            System.out.printf(" + %s - %s %s (KM: %.0f)\n", v.getPlaca(), v.getMarca(), v.getModelo(), v.getQuilometragemAtual());
        }
    }

    private static void listarManutencoesFormatado() {
        imprimirCabecalho("MANUTENÃÃES");
        List<Manutencao> lista = usuarioService.listarManutencao();

        if (lista.isEmpty()) {
            imprimirAviso("Nenhum registro de manutenÃ§Ã£o.");
            return;
        }

        for (Manutencao m : lista) {
            System.out.println(m); // Usa o toString() formatado da classe
            System.out.println(" ââââââââââââââââââââââââââââââââââââââââââââââââââ");
        }
    }

    private static void listarTodosUsuarios() {
        imprimirCabecalho("LISTAGEM GERAL DE USUÃRIOS");
        List<Usuario> usuarios = usuarioService.listarTodosUsuarios();

        if (usuarios.isEmpty()) {
            imprimirAviso("Base de dados vazia.");
            return;
        }

        for (Usuario u : usuarios) {
            System.out.println(u.getDetalhes()); // Detalhes de Admin ou Motorista
        }
        System.out.println(" ââââââââââââââââââââââââââââââââââââââââââââââââââ");
    }

    private static void listarHistorico(List<RegistroUso> registros) {
        if (registros.isEmpty()) {
            imprimirAviso("Nenhum registro encontrado para este filtro.");
            return;
        }
        for (RegistroUso r : registros) {
            System.out.println(formatarRegistroDetalhado(r));
            System.out.println(" ââââââââââââââââââââââââââââââââââââââââââââââââââ");
        }
    }

    // ==================================================================================
    //                     UTILITÃRIOS DE INTERFACE (UI HELPERS)
    // ==================================================================================

    /**
     * Imprime um cabeÃ§alho estilizado com bordas duplas.
     * Centraliza o texto automaticamente.
     * @param titulo O texto a ser exibido no centro.
     */
    public static void imprimirCabecalho(String titulo) {
        System.out.println("\nââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ");
        int larguraTotal = 66;
        int espacos = (larguraTotal - titulo.length()) / 2;
        System.out.print("â");
        for (int i = 0; i < espacos; i++) System.out.print(" ");
        System.out.print(titulo);
        for (int i = 0; i < espacos; i++) System.out.print(" ");
        if (titulo.length() % 2 != 0) System.out.print(" ");
        System.out.println("â");
        System.out.println("ââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ");
    }

    /**
     * Exibe o logotipo ASCII do sistema.
     */
    public static void imprimirLogo() {
        System.out.println("  ____       _                ____            _             _ ");
        System.out.println(" |  _ \\ _ __(_)_   _____     / ___|___  _ __ | |_ _ __ ___ | |");
        System.out.println(" | | | | '__| \\ \\ / / _ \\   | |   / _ \\| '_ \\| __| '__/ _ \\| |");
        System.out.println(" | |_| | |  | |\\ V /  __/   | |__| (_) | | | | |_| | | (_) | |");
        System.out.println(" |____/|_|  |_| \\_/ \\___|    \\____\\___/|_| |_|\\__|_|  \\___/|_|");
        System.out.println("                                                              ");
    }

    public static void imprimirSucesso(String mensagem) {
        System.out.println("\n [!SUCESSO!] " + mensagem);
    }

    public static void imprimirErro(String mensagem) {
        System.err.println("\n [!ERRO!] " + mensagem);
    }

    public static void imprimirAviso(String mensagem) {
        System.out.println("\n [!AVISO!] " + mensagem);
    }

    /**
     * Limpa o console do terminal.
     * Tenta usar comandos do sistema operacional para uma limpeza real.
     */
    public static void limparTela() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            // Fallback: imprime vÃ¡rias linhas se o comando do SO falhar
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }

    public static void pausar(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }

    public static void esperarEnter(Scanner input) {
        System.out.println("\n Pressione [ENTER] para continuar...");
        input.nextLine();
    }

    public static boolean confirmarAcao(String pergunta, Scanner input) {
        System.out.print("\n >> " + pergunta + " (S/N): ");
        String resposta = input.nextLine();
        return resposta.trim().equalsIgnoreCase("S");
    }

    private static void listarVeiculosDisponiveis() {
        imprimirCabecalho("VEÍCULOS DISPONÍVEIS");
        List<Veiculo> disponiveis = veiculoService.listarVeiculosDisponiveis();

        if (disponiveis.isEmpty()) {
            imprimirAviso("Nenhum veículo disponível no pátio.");
            return;
        }

        for (Veiculo v : disponiveis) {
            System.out.printf(" + %s - %s %s (KM: %.0f)\n", v.getPlaca(), v.getMarca(), v.getModelo(), v.getQuilometragemAtual());
        }
    }

    private static void listarManutencoesFormatado() {
        imprimirCabecalho("MANUTENÇÕES");
        List<Manutencao> lista = usuarioService.listarManutencao();

        if (lista.isEmpty()) {
            imprimirAviso("Nenhum registro de manutenção.");
            return;
        }

        for (Manutencao m : lista) {
            System.out.println(m); // Usa o toString() formatado da classe
            System.out.println(" ──────────────────────────────────────────────────");
        }
    }

    private static void listarTodosUsuarios() {
        imprimirCabecalho("LISTAGEM GERAL DE USUÁRIOS");
        List<Usuario> usuarios = usuarioService.listarTodosUsuarios();

        if (usuarios.isEmpty()) {
            imprimirAviso("Base de dados vazia.");
            return;
        }

        for (Usuario u : usuarios) {
            System.out.println(u.getDetalhes()); // Detalhes de Admin ou Motorista
        }
        System.out.println(" ──────────────────────────────────────────────────");
    }

    private static void listarHistorico(List<RegistroUso> registros) {
        if (registros.isEmpty()) {
            imprimirAviso("Nenhum registro encontrado para este filtro.");
            return;
        }
        for (RegistroUso r : registros) {
            System.out.println(formatarRegistroDetalhado(r));
            System.out.println(" ──────────────────────────────────────────────────");
        }
    }

    // ==================================================================================
    //                     UTILITÁRIOS DE INTERFACE (UI HELPERS)
    // ==================================================================================

    /**
     * Imprime um cabeçalho estilizado com bordas duplas.
     * Centraliza o texto automaticamente.
     * @param titulo O texto a ser exibido no centro.
     */
    public static void imprimirCabecalho(String titulo) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        int larguraTotal = 66;
        int espacos = (larguraTotal - titulo.length()) / 2;
        System.out.print("║");
        for (int i = 0; i < espacos; i++) System.out.print(" ");
        System.out.print(titulo);
        for (int i = 0; i < espacos; i++) System.out.print(" ");
        if (titulo.length() % 2 != 0) System.out.print(" ");
        System.out.println("║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Exibe o logotipo ASCII do sistema.
     */
    public static void imprimirLogo() {
        System.out.println("  ____       _                ____            _             _ ");
        System.out.println(" |  _ \\ _ __(_)_   _____     / ___|___  _ __ | |_ _ __ ___ | |");
        System.out.println(" | | | | '__| \\ \\ / / _ \\   | |   / _ \\| '_ \\| __| '__/ _ \\| |");
        System.out.println(" | |_| | |  | |\\ V /  __/   | |__| (_) | | | | |_| | | (_) | |");
        System.out.println(" |____/|_|  |_| \\_/ \\___|    \\____\\___/|_| |_|\\__|_|  \\___/|_|");
        System.out.println("                                                              ");
    }

    public static void imprimirSucesso(String mensagem) {
        System.out.println("\n [!SUCESSO!] " + mensagem);
    }

    public static void imprimirErro(String mensagem) {
        System.err.println("\n [!ERRO!] " + mensagem);
    }

    public static void imprimirAviso(String mensagem) {
        System.out.println("\n [!AVISO!] " + mensagem);
    }

    /**
     * Limpa o console do terminal.
     * Tenta usar comandos do sistema operacional para uma limpeza real.
     */
    public static void limparTela() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            // Fallback: imprime várias linhas se o comando do SO falhar
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }

    public static void pausar(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }

    public static void esperarEnter(Scanner input) {
        System.out.println("\n Pressione [ENTER] para continuar...");
        input.nextLine();
    }

    public static boolean confirmarAcao(String pergunta, Scanner input) {
        System.out.print("\n >> " + pergunta + " (S/N): ");
        String resposta = input.nextLine();
        return resposta.trim().equalsIgnoreCase("S");
    }
