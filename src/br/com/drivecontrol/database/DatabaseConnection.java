package br.com.drivecontrol.database;

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
    
    //Cria as tabelas necessárias no banco de dados
    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            
            // Tabela de usuários
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    user_name TEXT UNIQUE NOT NULL,
                    senha TEXT NOT NULL,
                    tipo TEXT NOT NULL CHECK (tipo IN ('ADMIN', 'FUNCIONARIO')),
                    cargo TEXT,
                    ativo BOOLEAN DEFAULT TRUE,
                    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Tabela de motoristas
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS motoristas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    setor TEXT NOT NULL,
                    cnh TEXT UNIQUE NOT NULL,
                    usuario_id INTEGER NOT NULL,
                    ativo BOOLEAN DEFAULT TRUE,
                    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
                )
            """);
            
            // Tabela de veículos
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS veiculos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    placa TEXT UNIQUE NOT NULL,
                    modelo TEXT NOT NULL,
                    marca TEXT NOT NULL,
                    ano INTEGER NOT NULL,
                    cor TEXT,
                    quilometragem REAL DEFAULT 0,
                    status TEXT NOT NULL DEFAULT 'DISPONIVEL' CHECK (status IN ('DISPONIVEL', 'EM_USO', 'MANUTENCAO', 'INDISPONIVEL')),
                    ultima_data_revisao TEXT,
                    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Tabela de registros de uso
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS registros_uso (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    veiculo_id INTEGER NOT NULL,
                    motorista_id INTEGER NOT NULL,
                    usuario_id INTEGER NOT NULL,
                    data_inicio TEXT NOT NULL,
                    data_fim TEXT,
                    quilometragem_inicial REAL NOT NULL,
                    quilometragem_final REAL,
                    destino_ou_finalidade TEXT NOT NULL,
                    FOREIGN KEY (veiculo_id) REFERENCES veiculos(id),
                    FOREIGN KEY (motorista_id) REFERENCES motoristas(id),
                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
                )
            """);
            
            // Tabela de manutenções
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS manutencoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    veiculo_id INTEGER NOT NULL,
                    data_inicio TEXT NOT NULL,
                    data_saida_prevista TEXT,
                    data_saida_real TEXT,
                    descricao_servico TEXT NOT NULL,
                    custo_previsto REAL DEFAULT 0,
                    custo_real REAL,
                    nome_oficina TEXT,
                    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (veiculo_id) REFERENCES veiculos(id)
                )
            """);
            
            System.out.println("Tabelas criadas/verificadas com sucesso!");
            
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabelas: " + e.getMessage());
            throw new RuntimeException("Erro ao criar estrutura do banco de dados", e);
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
