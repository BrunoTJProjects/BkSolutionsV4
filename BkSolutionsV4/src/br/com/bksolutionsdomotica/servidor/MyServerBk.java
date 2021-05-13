package br.com.bksolutionsdomotica.servidor;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONObject;

import br.com.bksolutionsdomotica.conexaobd.BKClienteDAO;
import br.com.bksolutionsdomotica.conexaobd.BKHardwareDAO;
import br.com.bksolutionsdomotica.manager.ClientsManager;
import br.com.bksolutionsdomotica.modelo.Cliente;
import br.com.bksolutionsdomotica.modelo.Hardware;
import br.com.bksolutionsdomotica.modelo.SocketBase;

public class MyServerBk implements ServerCoreBK.InterfaceCommand {

	private ServerCoreBK server;
	private static BKHardwareDAO bkHardwareDAO = new BKHardwareDAO();
	private static BKClienteDAO bkClienteDAO = new BKClienteDAO();
	private static ClientsManager gerenciador = new ClientsManager();

	public MyServerBk(int port) {
		server = new ServerCoreBK(port, this);
		try {
			server.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	 Método criado para Debug juntamente com o Servidor.java
	public static ClientsManager getGerenciador() {
		return gerenciador;
	}

	@Override
	public void onHardwareSignIn(SocketBase socketBase, String login, String password)
			throws ClassNotFoundException, SQLException, IOException {
		Hardware hardware = bkHardwareDAO.getHardware(login, password);
		if (hardware != null) {
			socketBase.setHardware(hardware);
			socketBase.sendCommand("Hardware login was Successful");
			gerenciador.addHardware(socketBase);
		}
	}

	@Override
	public void onHardwareSignOut(SocketBase hardware) throws ClassNotFoundException, SQLException, IOException {
		gerenciador.removeHardware(hardware);
	}

	@Override
	public void onHardwareCommand(SocketBase socketBase, JSONObject jsonObject) {
		try {
			gerenciador.onHardwareCommand(socketBase, jsonObject);
		} catch (ClassNotFoundException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onClienteSignIn(SocketBase socketBase, String login, String password)
			throws ClassNotFoundException, SQLException, IOException {
		Cliente cliente = bkClienteDAO.logarCliente(login, password);
		if (cliente != null) {
			socketBase.setCliente(cliente);
			socketBase.sendCommand("Cliente login was Successful");
			gerenciador.addCliente(socketBase);
		}
	}

	@Override
	public void onClienteSignOut(SocketBase cliente) throws ClassNotFoundException, SQLException, IOException {
		gerenciador.removeCliente(cliente);
	}

	@Override
	public void onClienteCommand(SocketBase socketBase, JSONObject comando) {
		try {
			gerenciador.onClienteCommand(socketBase, comando);
		} catch (ClassNotFoundException | IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static synchronized int getCodCliente(Hardware hardware) throws SQLException, ClassNotFoundException {
		int codCliente = bkHardwareDAO.getCodCliente(hardware);
		return codCliente;
	}

}