package br.com.Veiculos_Empresarial.repository;

import br.com.Veiculos_Empresarial.model.Usuario;
import br.com.Veiculos_Empresarial.model.Administrador;
import br.com.Veiculos_Empresarial.model.Motorista;
import br.com.Veiculos_Empresarial.database.DatabaseConnection;

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