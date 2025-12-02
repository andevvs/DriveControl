package service;

import repository.UsuarioRepository;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class UsuarioService {

    private MotoristaService motortistaService = new MotoristaService();
    private UsuarioRepository usuarioRepository = new UsuarioRepository();
    private VeiculoService veiculoService = new VeiculoService();
    private ManutencaoService manutencaoService = new ManutencaoService();
    private RegistroUsoService registroUsoService = new RegistroUsoService();
}