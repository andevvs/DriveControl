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

public List<Manutencao> listarTodos() {
        String sql = "SELECT * FROM manutencoes";
        List<Manutencao> manutencoes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                manutencoes.add(criarManutencaoDoResultSet(rs, conn));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar manutenções: " + e.getMessage());
        }
        return manutencoes;
    }

    public boolean excluir(int idManutencao) {
        String sql = "DELETE FROM manutencoes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idManutencao);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Manutenção ID " + idManutencao + " removida com sucesso.");
            } else {
                System.out.println("Nenhuma manutenção encontrada com o ID " + idManutencao + " para remover.");
            }

            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao remover manutenção: " + e.getMessage());
            return false;
        }
    }

    private Manutencao criarManutencaoDoResultSet(ResultSet rs, Connection conn) throws SQLException {
        int id = rs.getInt("id");
        int veiculoId = rs.getInt("veiculo_id");
        String descricao = rs.getString("descricao_servico");
        String oficina = rs.getString("nome_oficina");
        double custoPrevisto = rs.getDouble("custo_previsto");
        double custo = rs.getDouble("custo_real");

        Date dataEntrada = null;
        Date dataSaidaPrevista = null;
        Date dataSaidaReal = null;
        try {
            if (rs.getString("data_inicio") != null)
                dataEntrada = sdf.parse(rs.getString("data_inicio"));
            if (rs.getString("data_saida_prevista") != null)
                dataSaidaPrevista = sdf.parse(rs.getString("data_saida_prevista"));
            if (rs.getString("data_saida_real") != null)
                dataSaidaReal = sdf.parse(rs.getString("data_saida_real"));
        } catch (ParseException e) {
            System.err.println("Erro ao converter data da manutenção ID " + id + ": " + e.getMessage());
        }

        Veiculo veiculo = veiculoRepository.buscarPorId(veiculoId, conn);

        return new Manutencao(id, veiculo, descricao, oficina, dataEntrada, dataSaidaPrevista, dataSaidaReal,
                custoPrevisto, custo);
    }
}
