package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegistroUsoRepository {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private VeiculoRepository veiculoRepository;
    private MotoristaRepository motoristaRepository;

    public RegistroUsoRepository(VeiculoRepository veiculoRepository, MotoristaRepository motoristaRepository) {
        this.veiculoRepository = veiculoRepository;
        this.motoristaRepository = motoristaRepository;
    }

    public int salvar(RegistroUso registro) {
        String sql = "INSERT INTO registros_uso(veiculo_id, motorista_id, usuario_id, data_inicio, quilometragem_inicial, destino_ou_finalidade) "
                +
                "VALUES(?, ?, ?, ?, ?, ?)";
        int idGerado = -1;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, registro.getVeiculo().getId());
            // Coluna 2 (motorista_id) usa o ID da tabela 'motoristas'
            pstmt.setInt(2, registro.getMotorista().getId());
            // Coluna 3 (usuario_id) usa o ID da tabela 'usuarios'
            pstmt.setInt(3, registro.getMotorista().getUsuarioId());

            pstmt.setString(4, sdf.format(registro.getDataHoraSaida()));
            pstmt.setDouble(5, registro.getKmSaida());
            pstmt.setString(6, registro.getDestinoOuFinalidade());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGerado = rs.getInt(1);
                        registro.setId(idGerado);
                        System.out.println("Registro de uso salvo com ID: " + idGerado);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao salvar registro de uso: " + e.getMessage());
        }
        return idGerado;
    }

    public boolean atualizar(RegistroUso registro) {
        String sql = "UPDATE registros_uso SET data_fim = ?, quilometragem_final = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (registro.getDataHoraRetorno() != null) {
                pstmt.setString(1, sdf.format(registro.getDataHoraRetorno()));
            } else {
                pstmt.setNull(1, Types.VARCHAR);
            }
            pstmt.setDouble(2, registro.getKmRetorno());
            pstmt.setInt(3, registro.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar registro de uso: " + e.getMessage());
            return false;
        }
    }

    public RegistroUso buscarPorId(int id, Connection conn) {
        String sql = "SELECT * FROM registros_uso WHERE id = ? ORDER BY data_inicio DESC";
        RegistroUso registro = null;

        if (conn == null) {
            try (Connection connection = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {

                pstmt.setInt(1, id);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        registro = criarRegistroUsoDoResultSet(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erro ao buscar registro de uso por ID: " + e.getMessage());
            }
        } else {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        registro = criarRegistroUsoDoResultSet(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erro ao buscar registro de uso por ID: " + e.getMessage());
            }
        }
        return registro;
    }

    private RegistroUso criarRegistroUsoDoResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int veiculoId = rs.getInt("veiculo_id");

        int usuarioIdDoRegistro = rs.getInt("usuario_id");

        double quilometragemInicial = rs.getDouble("quilometragem_inicial");
        double quilometragemFinal = rs.getDouble("quilometragem_final");
        String destinoOuFinalidade = rs.getString("destino_ou_finalidade");

        Date dataHoraSaida = null;
        Date dataHoraRetorno = null;
        try {
            if (rs.getString("data_inicio") != null)
                dataHoraSaida = sdf.parse(rs.getString("data_inicio"));
            if (rs.getString("data_fim") != null)
                dataHoraRetorno = sdf.parse(rs.getString("data_fim"));
        } catch (ParseException e) {
            System.err.println("Erro ao converter data do registo de uso ID " + id + ": " + e.getMessage());
        }

        Veiculo veiculo = veiculoRepository.buscarPorId(veiculoId, null);
        Motorista motorista = motoristaRepository.buscarPorId(usuarioIdDoRegistro);
        Usuario usuario = (motorista != null) ? motorista.getUsuario() : null;

        if (veiculo != null && motorista != null && usuario != null) {
            return new RegistroUso(
                    id,
                    veiculo,
                    motorista,
                    usuario,
                    dataHoraSaida,
                    dataHoraRetorno,
                    quilometragemInicial,
                    quilometragemFinal,
                    destinoOuFinalidade);
        } else {
            return null;
        }
    }

    public RegistroUso buscarPorIdRobusto(int id) {
        String sql = "SELECT * FROM registros_uso WHERE id = ? ORDER BY data_inicio DESC";

        int registroId = -1;
        int veiculoId = -1;
        int motoristaUsuarioId = -1;
        double quilometragemInicial = 0.0;
        double quilometragemFinal = 0.0;
        String destinoOuFinalidade = "";
        Date dataHoraSaida = null;
        Date dataHoraRetorno = null;

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    registroId = rs.getInt("id");
                    veiculoId = rs.getInt("veiculo_id");
                    motoristaUsuarioId = rs.getInt("usuario_id");
                    quilometragemInicial = rs.getDouble("quilometragem_inicial");
                    quilometragemFinal = rs.getDouble("quilometragem_final");
                    destinoOuFinalidade = rs.getString("destino_ou_finalidade");

                    try {
                        if (rs.getString("data_inicio") != null)
                            dataHoraSaida = sdf.parse(rs.getString("data_inicio"));
                        if (rs.getString("data_fim") != null)
                            dataHoraRetorno = sdf.parse(rs.getString("data_fim"));
                    } catch (ParseException e) {
                        System.err
                                .println("Erro ao converter data do registro ID " + registroId + ": " + e.getMessage());
                    }
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar registro de uso por ID: " + e.getMessage());
            return null;
        }

        try {
            Veiculo veiculo = veiculoRepository.buscarPorId(veiculoId, null);
            Motorista motorista = motoristaRepository.buscarPorId(motoristaUsuarioId);
            Usuario usuario = (motorista != null) ? motorista.getUsuario() : null;

            if (veiculo != null && motorista != null && usuario != null) {
                // Caminho feliz
                return new RegistroUso(registroId, veiculo, motorista, usuario, dataHoraSaida,
                        dataHoraRetorno, quilometragemInicial, quilometragemFinal, destinoOuFinalidade);
            } else {
                // Caminho triste (Registro Ã“rfÃ£o)
                System.err.println(" Registro Ã³rfÃ£o ID " + registroId + ": criando objetos substitutos");

                if (veiculo == null) {
                    veiculo = new Veiculo(veiculoId, "PLACA_AUSENTE", "MODELO_AUSENTE", "MARCA_AUSENTE", 2000,
                            "COR_AUSENTE",
                            StatusVeiculo.DISPONIVEL, 0.0, null);
                }

                if (motorista == null || usuario == null) {
                    // 1. Cria o Motorista
                    motorista = new Motorista(motoristaUsuarioId, "MOTORISTA_AUSENTE", "usuario_ausente", "senha", true,
                            "SETOR_AUSENTE", "CNH_AUSENTE", motoristaUsuarioId, true);
                    // 2. O 'usuario' Ã© o prÃ³prio 'motorista'
                    usuario = motorista;
                }

                return new RegistroUso(registroId, veiculo, motorista, usuario, dataHoraSaida,
                        dataHoraRetorno, quilometragemInicial, quilometragemFinal, destinoOuFinalidade);
            }
        } catch (Exception e) {
            System.err.println(" Erro ao criar registro robusto ID " + registroId + ": " + e.getMessage());
            return null;
        }
    }

    public List<RegistroUso> listarTodosRegistrosUso() {
        String sql = "SELECT * FROM registros_uso ORDER BY data_inicio DESC";
        List<RegistroUso> registros = new ArrayList<>();
        List<DadosRegistro> dadosExtraidos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DadosRegistro dados = new DadosRegistro();
                    dados.id = rs.getInt("id");
                    dados.veiculoId = rs.getInt("veiculo_id");
                    dados.motoristaUsuarioId = rs.getInt("usuario_id");
                    dados.quilometragemInicial = rs.getDouble("quilometragem_inicial");
                    dados.quilometragemFinal = rs.getDouble("quilometragem_final");
                    dados.destinoOuFinalidade = rs.getString("destino_ou_finalidade");

                    try {
                        if (rs.getString("data_inicio") != null)
                            dados.dataHoraSaida = sdf.parse(rs.getString("data_inicio"));
                        if (rs.getString("data_fim") != null)
                            dados.dataHoraRetorno = sdf.parse(rs.getString("data_fim"));
                    } catch (ParseException e) {
                        System.err.println("Erro ao converter data do registro ID " + dados.id + ": " + e.getMessage());
                    }
                    dadosExtraidos.add(dados);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar todos os registros de uso: " + e.getMessage());
            return registros;
        }

        for (DadosRegistro dados : dadosExtraidos) {
            try {
                Veiculo veiculo = veiculoRepository.buscarPorId(dados.veiculoId, null);
                Motorista motorista = motoristaRepository.buscarPorId(dados.motoristaUsuarioId);
                Usuario usuario = (motorista != null) ? motorista.getUsuario() : null;

                if (veiculo != null && motorista != null && usuario != null) {
                    RegistroUso registro = new RegistroUso(dados.id, veiculo, motorista, usuario, dados.dataHoraSaida,
                            dados.dataHoraRetorno, dados.quilometragemInicial, dados.quilometragemFinal,
                            dados.destinoOuFinalidade);
                    registros.add(registro);
                } else {
                    System.err.println(" Registro Ã³rfÃ£o ID " + dados.id + ": criando objetos substitutos");

                    if (veiculo == null) {
                        veiculo = new Veiculo(dados.veiculoId, "PLACA_AUSENTE", "MODELO_AUSENTE", "MARCA_AUSENTE", 2000,
                                "COR_AUSENTE",
                                StatusVeiculo.DISPONIVEL, 0.0, null);
                    }

                    if (motorista == null || usuario == null) {
                        // 1. Cria Motorista
                        motorista = new Motorista(dados.motoristaUsuarioId, "MOTORISTA_AUSENTE", "usuario_ausente",
                                "senha", true,
                                "SETOR_AUSENTE", "CNH_AUSENTE", dados.motoristaUsuarioId, true);
                        // 2. 'usuario' Ã© o prÃ³prio 'motorista'
                        usuario = motorista;
                    }

                    RegistroUso registro = new RegistroUso(dados.id, veiculo, motorista, usuario, dados.dataHoraSaida,
                            dados.dataHoraRetorno, dados.quilometragemInicial, dados.quilometragemFinal,
                            dados.destinoOuFinalidade);
                    registros.add(registro);
                }
            } catch (Exception e) {
                System.err.println(" Erro ao processar registro ID " + dados.id + ": " + e.getMessage());
            }
        }
        return registros;
    }

    public boolean remover(int id) {
        String sql = "DELETE FROM registros_uso WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir registro de uso: " + e.getMessage());
            return false;
        }
    }

    public boolean existsByVeiculoId(int veiculoId) {
        String sql = "SELECT COUNT(*) FROM registros_uso WHERE veiculo_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, veiculoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar existÃªncia de registros de uso para veÃ­culo ID " + veiculoId + ": "
                    + e.getMessage());
        }
        return false;
    }
  public boolean existsByMotoristaId(int motoristaId) {
        // Este mÃ©todo verifica o 'motorista_id' (ID da tabela motorista), nÃ£o o
        // 'usuario_id'
        String sql = "SELECT COUNT(*) FROM registros_uso WHERE motorista_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, motoristaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar existÃªncia de registros de uso para motorista ID " + motoristaId
                    + ": " + e.getMessage());
        }
        return false;
    }
  public boolean existsUnfinishedByVeiculoId(int veiculoId) {
        String sql = "SELECT COUNT(*) FROM registros_uso WHERE veiculo_id = ? AND data_fim IS NULL";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, veiculoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar registros de uso nÃ£o finalizados para veÃ­culo ID " + veiculoId + ": "
                    + e.getMessage());
        }
        return false;
    }
