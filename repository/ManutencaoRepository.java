package repository;

import model.Manutencao;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ManutencaoRepository {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    private VeiculoRepository veiculoRepository = new VeiculoRepository();

    public void salvar(Manutencao manutencao) {
        String sql = "INSERT INTO manutencoes (veiculo_id, data_inicio, data_saida_prevista, descricao_servico, custo_previsto, nome_oficina, custo_real) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, manutencao.getVeiculo().getId());

            pstmt.setString(2, sdf.format(manutencao.getDataEntrada()));
            if (manutencao.getDataSaidaPrevista() != null) {
                pstmt.setString(3, sdf.format(manutencao.getDataSaidaPrevista()));
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }

            pstmt.setString(4, manutencao.getDescricaoServico());
            pstmt.setDouble(5, manutencao.getCustoPrevisto());
            pstmt.setString(6, manutencao.getNomeOficina());
            pstmt.setDouble(7, manutencao.getCustoReal());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    manutencao.setId(generatedKeys.getInt(1));
                }
            }
            System.out.println("Manutenção salva com sucesso para o veículo " + manutencao.getVeiculo().getPlaca());

        } catch (SQLException e) {
            System.err.println("Erro ao salvar manutenção: " + e.getMessage());
        }
    }

    public void atualizar(Manutencao manutencao) {
        String sql = "UPDATE manutencoes SET data_saida_real = ?, custo_real = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (manutencao.getDataSaidaReal() != null) {
                pstmt.setString(1, sdf.format(manutencao.getDataSaidaReal()));
            } else {
                pstmt.setNull(1, Types.VARCHAR);
            }

            pstmt.setDouble(2, manutencao.getCustoReal());
            pstmt.setInt(3, manutencao.getId());

            pstmt.executeUpdate();
            System.out.println("Manutenção ID " + manutencao.getId() + " atualizada com sucesso.");

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar manutenção: " + e.getMessage());
        }
    }

    public Manutencao buscarManutencaoAtivaPorVeiculoId(int veiculoId) {
        String sql = "SELECT * FROM manutencoes WHERE veiculo_id = ? AND data_saida_real IS NULL";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, veiculoId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return criarManutencaoDoResultSet(rs, conn);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar manutenção ativa por ID do veículo: " + e.getMessage());
        }
        return null;
    }
