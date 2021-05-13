package br.com.bksolutionsdomotica.modelo;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONObject;

import br.com.bksolutionsdomotica.conexaobd.BKClienteDAO;
import br.com.bksolutionsdomotica.conexaobd.DataAccess;


public class Cliente {
	private int id;
	private String nome;
	private String sexo;
	private Date nasc;
	private String rua;
	private String numero;
	private String bairro;
	private String cidade;
	private String estado;
	private String cpf;
	private String email;
	private String password;
	private DataAccess dataAccess;
	private BKClienteDAO bkClienteDAO;

	public Cliente(int id, String nome, String sexo, Date nasc, String rua, String numero, String bairro, String cidade,
			String estado, String cpf, String email, String password) {
		super();
		this.id = id;
		this.nome = nome;
		this.sexo = sexo;
		this.nasc = nasc;
		this.rua = rua;
		this.numero = numero;
		this.bairro = bairro;
		this.cidade = cidade;
		this.estado = estado;
		this.cpf = cpf;
		this.email = email;
		this.password = password;
		if (dataAccess == null) {
			dataAccess = new DataAccess();
			bkClienteDAO = new BKClienteDAO();
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSexo() {
		return sexo;
	}

	public void setSexo(String sexo) {
		this.sexo = sexo;
	}

	public Date getNasc() {
		return nasc;
	}

	public void setNasc(Date nasc) {
		this.nasc = nasc;
	}

	public String getRua() {
		return rua;
	}

	public void setRua(String rua) {
		this.rua = rua;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int setChave(String mac, String chave, String valor)
			throws ClassNotFoundException, SQLException {
		int linhasafetadas = dataAccess.setChave(this, mac, chave, valor);
		return linhasafetadas;
	}

	public String getChave(String mac, String chave) throws ClassNotFoundException, SQLException {
		String valor = dataAccess.getChave(this, mac, chave);
		return valor;
	}

	public int excluirChave(String mac, String chave) throws ClassNotFoundException, SQLException {
		int linhasafetadas = dataAccess.excluirChave(this, mac, chave);
		return linhasafetadas;
	}

	public int setChaves(String mac, JSONObject jsonObj) throws ClassNotFoundException, SQLException {
		int linhasafetadas = dataAccess.setChaves(this, mac, jsonObj);
		return linhasafetadas;
	}

	public JSONObject getChaves(String mac) throws ClassNotFoundException, SQLException {
		JSONObject jsonObj = dataAccess.getChaves(this, mac);
		return jsonObj;
	}

	public List<Hardware> getHardwares() throws ClassNotFoundException, SQLException {
		List<Hardware> hardwares = bkClienteDAO.getHardwares(this);
		return hardwares;
	}
	
	@Override
	public String toString() {
		String info = null;
		info = "Contrato: " + id + "\r\n" + "Nome: " + nome + "\r\n" + "Sexo: " + sexo + "\r\n" + "Data de Nascimento: "
				+ nasc + "\r\n" + "Rua: " + rua + "\r\n" + "N�: " + numero + "\r\n" + "Bairro: " + bairro + "\r\n"
				+ "Cidade: " + cidade + "\r\n" + "Estado: " + estado + "\r\n" + "CPF: " + cpf + "\r\n" + "E-mail: "
				+ email + "\r\n" + "Senha: " + password;

		return info;
	}

}
