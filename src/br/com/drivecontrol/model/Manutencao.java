package br.com.drivecontrol.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Manutencao {

    private int id;
    private Veiculo veiculo;
    private String descricaoServico;
    private String nomeOficina;
    private Date dataEntrada;
    private Date dataSaidaPrevista;
    private Date dataSaidaReal;
    private double custoPrevisto;
    private double custoReal;

    public Manutencao(Veiculo veiculo, String descricaoServico, String nomeOficina, Date dataEntrada,
            Date dataSaidaPrevista, double custoPrevisto) {
        this.veiculo = veiculo;
        this.descricaoServico = descricaoServico;
        this.nomeOficina = nomeOficina;
        this.dataEntrada = dataEntrada;
        this.dataSaidaPrevista = dataSaidaPrevista;
        this.custoPrevisto = custoPrevisto;
        this.dataSaidaReal = null;
        this.custoReal = 0.0;
    }

    public Manutencao(int id, Veiculo veiculo, String descricaoServico, String nomeOficina, Date dataEntrada,
            Date dataSaidaPrevista, Date dataSaidaReal, double custoPrevisto, double custoReal) {
        this.id = id;
        this.veiculo = veiculo;
        this.descricaoServico = descricaoServico;
        this.nomeOficina = nomeOficina;
        this.dataEntrada = dataEntrada;
        this.dataSaidaPrevista = dataSaidaPrevista;
        this.dataSaidaReal = dataSaidaReal;
        this.custoPrevisto = custoPrevisto;
        this.custoReal = custoReal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public String getDescricaoServico() {
        return descricaoServico;
    }

    public void setDescricaoServico(String descricaoServico) {
        this.descricaoServico = descricaoServico;
    }

    public String getNomeOficina() {
        return nomeOficina;
    }

    public void setNomeOficina(String nomeOficina) {
        this.nomeOficina = nomeOficina;
    }

    public Date getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(Date dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public Date getDataSaidaPrevista() {
        return dataSaidaPrevista;
    }

    public void setDataSaidaPrevista(Date dataSaidaPrevista) {
        this.dataSaidaPrevista = dataSaidaPrevista;
    }

    public Date getDataSaidaReal() {
        return dataSaidaReal;
    }

    public void setDataSaidaReal(Date dataSaidaReal) {
        this.dataSaidaReal = dataSaidaReal;
    }

    public double getCustoReal() {
        return custoReal;
    }

    public void setCustoReal(double custoReal) {
        this.custoReal = custoReal;
    }

    public double getCustoPrevisto() {
        return custoPrevisto;
    }

    public void setCustoPrevisto(double custoPrevisto) {
        this.custoPrevisto = custoPrevisto;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        String veiculoPlaca = (veiculo != null) ? veiculo.getPlaca() : "N/A";
        String dataEntradaF = (dataEntrada != null) ? sdf.format(dataEntrada) : "N/A";
        String dataSaidaPrevF = (dataSaidaPrevista != null) ? sdf.format(dataSaidaPrevista) : "N/A";
        String dataSaidaRealF = (dataSaidaReal != null) ? sdf.format(dataSaidaReal) : "Pendente";
        
        String custoPrevistoF = (this.custoPrevisto > 0) ? String.format("R$%.2f", this.custoPrevisto) : "N/A";
        String custoRealF = (dataSaidaReal != null) ? String.format("R$%.2f", custoReal) : "Pendente";

        return String.format(
            "--- Manutenção ID: %d ---\n" +
            "  Veículo: \t%s\n" +
            "  Oficina: \t%s\n" +
            "  Descrição: \t%s\n" +
            "  Entrada: \t%s\n" +
            "  Prev. Saída: \t%s\n" +
            "  Saída Real: \t%s\n" +
            "  Custo Prev.: \t%s\n" +
            "  Custo Real: \t%s",
            id,
            veiculoPlaca,
            nomeOficina,
            descricaoServico,
            dataEntradaF,
            dataSaidaPrevF,
            dataSaidaRealF,
            custoPrevistoF,
            custoRealF
        );
    }
}