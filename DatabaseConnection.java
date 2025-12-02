package br.com.Veiculos_Empresarial.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private Connection connection;
    private static final String DATABASE_URL = "jdbc:sqlite:veiculos_empresarial.db";
    
    private DatabaseConnection() {
        try {
            // Carrega o driver SQLite
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DATABASE_URL);
            
            // Habilita foreign keys no SQLite
            enableForeignKeys();
            
            // Cria as tabelas se não existirem
            createTables();
            
            System.out.println("Conexão com SQLite estabelecida com sucesso!");
            
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite não encontrado: " + e.getMessage());
            throw new RuntimeException("Erro ao carregar driver SQLite", e);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar com o banco de dados: " + e.getMessage());
            throw new RuntimeException("Erro ao conectar com o banco de dados", e);
        }
    }
    
    /**
     * Retorna a instância única da conexão (Singleton)
     * @return instância única de DatabaseConnection
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Retorna a conexão com o banco de dados
     * @return Connection objeto de conexão
     */
    public Connection getConnection() {
        try {
            // Verifica se a conexão ainda está válida
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DATABASE_URL);
                enableForeignKeys();
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar conexão: " + e.getMessage());
            throw new RuntimeException("Erro na conexão com o banco de dados", e);
        }
        return connection;
    }
    
    /**
     * Habilita o suporte a foreign keys no SQLite
     */
    private void enableForeignKeys() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            System.err.println("Erro ao habilitar foreign keys: " + e.getMessage());
        }
    }
    
    /**
     * Fecha a conexão com o banco de dados
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexão com SQLite fechada com sucesso!");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }
    
    /**
     * Verifica se a conexão está ativa
     * @return true se a conexão está ativa, false caso contrário
     */
    public boolean isConnectionActive() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Executa uma query de teste para verificar se a conexão está funcionando
     * @return true se a conexão está funcionando, false caso contrário
     */
    public boolean testConnection() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SELECT 1");
            return true;
        } catch (SQLException e) {
            System.err.println("Erro no teste de conexão: " + e.getMessage());
            return false;
        }
    }
}
