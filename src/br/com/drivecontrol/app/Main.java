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
