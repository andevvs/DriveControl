package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MotoristaRepository {
    public void salvar(Motorista motorista) {
        String sqlUsuario = "INSERT INTO usuarios (nome, user_name, senha, tipo) VALUES (?, ?, ?, ?)";
        String sqlMotorista = "INSERT INTO motoristas (setor, cnh, usuario_id) VALUES (?, ?, ?)";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        long usuarioId = -1;

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtUsuario = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                pstmtUsuario.setString(1, motorista.getNome());
                pstmtUsuario.setString(2, motorista.getUsername());
                pstmtUsuario.setString(3, motorista.getSenha());
                pstmtUsuario.setString(4, "FUNCIONARIO");

                int affectedRows = pstmtUsuario.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmtUsuario.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            usuarioId = generatedKeys.getLong(1);
                        }
                    }
                }
            }

            if (usuarioId == -1) {
                throw new SQLException("Falha ao criar o usuário, ID não obtido.");
            }

            try (PreparedStatement pstmtMotorista = conn.prepareStatement(sqlMotorista)) {
                pstmtMotorista.setString(1, motorista.getSetor());
                pstmtMotorista.setString(2, motorista.getCnh());
                pstmtMotorista.setLong(3, usuarioId);
                pstmtMotorista.executeUpdate();
            }

            conn.commit();
            System.out.println(
                    "Motorista '" + motorista.getNome() + "' salvo com sucesso (ID do usuário: " + usuarioId + ")");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar motorista, revertendo a transação: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Erro crítico ao reverter a transação: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException finalEx) {
                System.err.println("Erro ao restaurar auto-commit: " + finalEx.getMessage());
            }
        }
    }

    public Motorista buscarPorCnh(String cnh) {
        String sql = "SELECT u.nome, u.user_name, u.senha, m.setor, m.cnh "
                + "FROM motoristas m "
                + "JOIN usuarios u ON m.usuario_id = u.id "
                + "WHERE m.cnh = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cnh);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return criarMotoristaDoResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar motorista por CNH: " + e.getMessage());
        }
        return null;
    }

    public List<Motorista> listarTodos() {
        String sql = "SELECT u.id as usuario_id, u.nome, u.user_name, u.senha, u.tipo, u.ativo as usuario_ativo, " +
                "m.id as motorista_id, m.setor, m.cnh, m.ativo as motorista_ativo " +
                "FROM motoristas m " +
                "JOIN usuarios u ON m.usuario_id = u.id " +
                "WHERE m.ativo = 1 AND u.ativo = 1 " +
                "ORDER BY u.nome ASC";

        List<Motorista> motoristas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                motoristas.add(criarMotoristaCompletoDoResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar motoristas: " + e.getMessage());
        }
        return motoristas;
    }

    public List<Motorista> buscarPorNome(String nome) {
        String sql = "SELECT u.id as usuario_id, u.nome, u.user_name, u.senha, u.tipo, u.ativo as usuario_ativo, " +
                "m.id as motorista_id, m.setor, m.cnh, m.ativo as motorista_ativo " +
                "FROM motoristas m " +
                "JOIN usuarios u ON m.usuario_id = u.id " +
                "WHERE u.nome LIKE ? AND m.ativo = 1 AND u.ativo = 1";

        List<Motorista> motoristas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nome + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    motoristas.add(criarMotoristaCompletoDoResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar motorista por nome: " + e.getMessage());
        }
        return motoristas;
    }

    private Motorista criarMotoristaDoResultSet(ResultSet rs) throws SQLException {
        String nome = rs.getString("nome");
        String username = rs.getString("user_name");
        String senha = rs.getString("senha");
        String setor = rs.getString("setor");
        String cnh = rs.getString("cnh");

        return new Motorista(nome, username, senha, setor, cnh);
    }

    public Motorista buscarPorId(int usuarioId) {
        String sql = "SELECT u.id as usuario_id, u.nome, u.user_name, u.senha, u.tipo, u.ativo as usuario_ativo, " +
                "m.id as motorista_id, m.setor, m.cnh, m.ativo as motorista_ativo " +
                "FROM motoristas m " +
                "JOIN usuarios u ON m.usuario_id = u.id " +
                "WHERE m.usuario_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return criarMotoristaCompletoDoResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar motorista por ID de usuário: " + e.getMessage());
        }
        return null;
    }
    private Motorista criarMotoristaCompletoDoResultSet(ResultSet rs) throws SQLException {
        int motoristaId = rs.getInt("motorista_id");
        int usuarioId = rs.getInt("usuario_id");
        String nome = rs.getString("nome");
        String username = rs.getString("user_name");
        String senha = rs.getString("senha");

        boolean usuarioAtivo = rs.getBoolean("usuario_ativo");
        String setor = rs.getString("setor");
        String cnh = rs.getString("cnh");
        boolean motoristaAtivo = rs.getBoolean("motorista_ativo");

        return new Motorista(
                motoristaId,
                nome,
                username,
                senha,
                usuarioAtivo,
                setor,
                cnh,
                usuarioId,
                motoristaAtivo);
    }

    public void atualizar(Motorista motorista) {
        String sqlUsuario = "UPDATE usuarios SET nome = ?, user_name = ?, senha = ? "
                + "WHERE id = (SELECT usuario_id FROM motoristas WHERE cnh = ?)";

        String sqlMotorista = "UPDATE motoristas SET setor = ? WHERE cnh = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtUsuario = conn.prepareStatement(sqlUsuario)) {
                pstmtUsuario.setString(1, motorista.getNome());
                pstmtUsuario.setString(2, motorista.getUsername());
                pstmtUsuario.setString(3, motorista.getSenha());
                pstmtUsuario.setString(4, motorista.getCnh());
                pstmtUsuario.executeUpdate();
            }

            try (PreparedStatement pstmtMotorista = conn.prepareStatement(sqlMotorista)) {
                pstmtMotorista.setString(1, motorista.getSetor());
                pstmtMotorista.setString(2, motorista.getCnh());
                pstmtMotorista.executeUpdate();
            }

            conn.commit();
            System.out.println("Dados do motorista com CNH " + motorista.getCnh() + " atualizados com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar motorista, revertendo a transação.");
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Erro crítico ao reverter a transação: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException finalEx) {
                System.err.println("Erro ao restaurar auto-commit: " + finalEx.getMessage());
            }
        }
    }

    public void remover(String cnh) {
        String sqlSelectId = "SELECT usuario_id FROM motoristas WHERE cnh = ?";
        String sqlDeleteMotorista = "DELETE FROM motoristas WHERE cnh = ?";
        String sqlDeleteUsuario = "DELETE FROM usuarios WHERE id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        long usuarioId = -1;

        try {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelectId)) {
                pstmtSelect.setString(1, cnh);
                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    if (rs.next()) {
                        usuarioId = rs.getLong("usuario_id");
                    }
                }
            }

            if (usuarioId == -1) {
                throw new SQLException("Motorista com CNH " + cnh + " não encontrado, impossível remover.");
            }

            try (PreparedStatement pstmtMotorista = conn.prepareStatement(sqlDeleteMotorista)) {
                pstmtMotorista.setString(1, cnh);
                pstmtMotorista.executeUpdate();
            }

            try (PreparedStatement pstmtUsuario = conn.prepareStatement(sqlDeleteUsuario)) {
                pstmtUsuario.setLong(1, usuarioId);
                pstmtUsuario.executeUpdate();
            }

            conn.commit();
            System.out.println("Motorista com CNH " + cnh + " removido com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao remover motorista, revertendo a transação: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Erro crítico ao reverter a transação: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException finalEx) {
                System.err.println("Erro ao restaurar auto-commit: " + finalEx.getMessage());
            }
        }
    }

    public Motorista buscarPorCnhComId(String cnh) {
        String sql = "SELECT u.id as usuario_id, u.nome, u.user_name, u.senha, u.tipo, u.ativo as usuario_ativo, " +
                "m.id as motorista_id, m.setor, m.cnh, m.ativo as motorista_ativo " +
                "FROM motoristas m " +
                "JOIN usuarios u ON m.usuario_id = u.id " +
                "WHERE m.cnh = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cnh);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return criarMotoristaCompletoDoResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar motorista por CNH com ID: " + e.getMessage());
        }
        return null;
    }
}