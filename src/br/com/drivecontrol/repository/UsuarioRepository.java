package br.com.drivecontrol.repository;

import br.com.drivecontrol.model.Usuario;
import br.com.drivecontrol.model.Administrador;
import br.com.drivecontrol.model.Motorista;
import br.com.drivecontrol.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    public void salvarAdmin(Administrador admin) {
        String sql = "INSERT INTO usuarios (nome, user_name, senha, tipo, cargo) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, admin.getNome());
            pstmt.setString(2, admin.getUsername());
            pstmt.setString(3, admin.getSenha());
            pstmt.setString(4, "ADMIN");
            pstmt.setString(5, admin.getCargo());

            pstmt.executeUpdate();
            System.out.println("Administrador " + admin.getNome() + " salvo com sucesso.");

        } catch (SQLException e) {
            if (e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                System.err.println("Erro: O nome de usuÃ¡rio '" + admin.getUsername() + "' jÃ¡ existe.");
            } else {
                System.err.println("Erro ao salvar administrador: " + e.getMessage());
            }
        }
    }

  public List<Usuario> listarTodosUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();

        String sql = "SELECT u.id as usuario_id, u.nome, u.user_name, u.senha, u.tipo,u.cargo, u.ativo as usuario_ativo, "
                +
                "m.id as motorista_id, m.setor, m.cnh, m.ativo as motorista_ativo " +
                "FROM usuarios u " +
                "LEFT JOIN motoristas m ON u.id = m.usuario_id " +
                "ORDER BY u.tipo ASC, u.nome ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            // Itera sobre TODOS os resultados
            while (rs.next()) {
                try {
                    int usuarioId = rs.getInt("usuario_id");
                    String nomeDb = rs.getString("nome");
                    String usernameDb = rs.getString("user_name");
                    String senhaDb = rs.getString("senha");
                    String tipoDb = rs.getString("tipo");
                    String cargoDb = rs.getString("cargo");
                    boolean usuarioAtivo = rs.getBoolean("usuario_ativo");

                    if ("ADMIN".equals(tipoDb)) {
                        Administrador admin = new Administrador(usuarioId, nomeDb, usernameDb, senhaDb, usuarioAtivo,
                                cargoDb);
                        usuarios.add(admin); // Adiciona na lista Ãºnica

                    } else if ("FUNCIONARIO".equals(tipoDb)) {
                        int motoristaId = rs.getInt("motorista_id");
                        String setor = rs.getString("setor");
                        String cnh = rs.getString("cnh");
                        boolean motoristaAtivo = rs.getBoolean("motorista_ativo");

                        Motorista motorista = new Motorista(motoristaId, nomeDb, usernameDb, senhaDb, usuarioAtivo,
                                setor, cnh, usuarioId, motoristaAtivo);
                        usuarios.add(motorista); // Adiciona na lista Ãºnica
                    }
                } catch (Exception e) {
                    System.err
                            .println("Erro ao processar usuÃ¡rio ID " + rs.getInt("usuario_id") + ": " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar todos os usuÃ¡rios: " + e.getMessage());
        }

        return usuarios;
    }
  public Usuario buscarPorUsername(String username) {

        // SQL que faz JOIN para jÃ¡ buscar dados de motorista, se existirem
        String sql = "SELECT u.id as usuario_id, u.nome, u.user_name, u.senha,u.cargo, u.tipo, u.ativo as usuario_ativo, "
                +
                "m.id as motorista_id, m.setor, m.cnh, m.ativo as motorista_ativo " +
                "FROM usuarios u " +
                "LEFT JOIN motoristas m ON u.id = m.usuario_id " +
                "WHERE u.user_name = ?";

        Usuario usuario = null;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Dados comuns da tabela 'usuarios'
                    int usuarioId = rs.getInt("usuario_id");
                    String nomeDb = rs.getString("nome");
                    String usernameDb = rs.getString("user_name");
                    String senhaDb = rs.getString("senha");
                    String tipoDb = rs.getString("tipo");
                    String cargoDb = rs.getString("cargo");
                    boolean usuarioAtivo = rs.getBoolean("usuario_ativo");

                    if ("ADMIN".equals(tipoDb)) {
                        // ConstrÃ³i um Administrador
                        // (Assume um "cargo" padrÃ£o, jÃ¡ que nÃ£o temos no banco)
                        usuario = new Administrador(usuarioId, nomeDb, usernameDb, senhaDb, usuarioAtivo, cargoDb);

                    } else if ("FUNCIONARIO".equals(tipoDb)) {
                        // ConstrÃ³i um Motorista usando os dados do JOIN
                        int motoristaId = rs.getInt("motorista_id");
                        String setor = rs.getString("setor");
                        String cnh = rs.getString("cnh");
                        boolean motoristaAtivo = rs.getBoolean("motorista_ativo");

                        usuario = new Motorista(motoristaId, nomeDb, usernameDb, senhaDb, usuarioAtivo,
                                setor, cnh, usuarioId, motoristaAtivo);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuÃ¡rio por username: " + e.getMessage());
        }

        // Retorna a instÃ¢ncia concreta (Admin ou Motorista) ou null
        return usuario;
    }
}
