package br.com.Veiculos_Empresarial.repository;

import br.com.Veiculos_Empresarial.model.Veiculo;
import br.com.Veiculos_Empresarial.database.DatabaseConnection;
import br.com.Veiculos_Empresarial.model.StatusVeiculo;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class VeiculoRepository {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public void salvar(Veiculo veiculo) {
        String sql = "INSERT INTO veiculos (placa, modelo, marca, ano, cor, quilometragem, status, ultima_data_revisao) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, veiculo.getPlaca());
            pstmt.setString(2, veiculo.getModelo());
            pstmt.setString(3, veiculo.getMarca());
            pstmt.setInt(4, veiculo.getAno());
            pstmt.setString(5, veiculo.getCor());
            pstmt.setDouble(6, veiculo.getQuilometragemAtual());
            pstmt.setString(7, veiculo.getStatus().name());

            if (veiculo.getUltimaDataDeRevisao() != null) {
                pstmt.setString(8, sdf.format(veiculo.getUltimaDataDeRevisao()));
            } else {
                pstmt.setNull(8, Types.VARCHAR);
            }

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    veiculo.setId(rs.getInt(1));
                }
            }
            System.out.println("VeÃ­culo com placa '" + veiculo.getPlaca() + "' salvo com sucesso!");

        } catch (SQLException e) {
            if (e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                System.err.println("Erro: A placa '" + veiculo.getPlaca() + "' jÃ¡ estÃ¡ cadastrada.");
            } else {
                System.err.println("Erro ao salvar veÃ­culo: " + e.getMessage());
            }
        }
    }

    public Veiculo buscarPorId(int id, Connection conn) {
        String sql = "SELECT * FROM veiculos WHERE id = ?";

        if (conn == null) {
            try (Connection connection = DatabaseConnection.getInstance().getConnection();
                    PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return criarVeiculoDoResultSet(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erro ao procurar veÃ­culo por ID: " + e.getMessage());
            }
        } else {

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return criarVeiculoDoResultSet(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erro ao procurar veÃ­culo por ID: " + e.getMessage());
            }
        }
        return null;
    }
     public Veiculo buscarVeiculoPorPlaca(String placa) {
        String sql = "SELECT * FROM veiculos WHERE placa = ?;";
        Veiculo veiculo = null;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, placa);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    veiculo = criarVeiculoDoResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar veÃ­culo por placa: " + e.getMessage());
        }
        return veiculo;
    }
