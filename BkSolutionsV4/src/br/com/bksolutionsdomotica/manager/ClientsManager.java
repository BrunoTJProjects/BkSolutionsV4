package br.com.bksolutionsdomotica.manager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.JSONObject;

import br.com.bksolutionsdomotica.modelo.Cliente;
import br.com.bksolutionsdomotica.modelo.Hardware;
import br.com.bksolutionsdomotica.modelo.SocketBase;
import br.com.bksolutionsdomotica.servidor.MyServerBk;

public class ClientsManager {

	private static HashMap<Integer, UserHardwares> userHardwares;

	public ClientsManager() {
		userHardwares = new HashMap<Integer, UserHardwares>();
	}

	public void addCliente(SocketBase cliente) {
		if (cliente == null)
			return;
		int id = cliente.getCliente().getId();
		if (!userHardwares.containsKey(id)) {
			UserHardwares userHard = new UserHardwares();
			userHard.addClientes(cliente);
			userHardwares.put(id, userHard);
		} else {
			UserHardwares userHard = userHardwares.get(id);
			userHard.addClientes(cliente);
		}
	}

	public void removeCliente(SocketBase cliente) {
		if (cliente == null)
			return;
		int id = cliente.getCliente().getId();
		if (userHardwares.containsKey(id)) {
			UserHardwares userHard = userHardwares.get(id);
			if (userHard.contemCliente(cliente)) {
				userHard.removeCliente(cliente);
				System.out.println("Cliente existe e foi removido");
				if (userHard.naoContemClientes()) {
					System.out.println("Esse fui o ultimo cliente");
					if (userHard.naoContemHardwares()) {
						userHardwares.remove(id);
						System.out.println(
								"Nenhum hardware guardado tamb�m e " + "por isso foi eliminado esse userHardware");
						System.out.println(userHardwares.size());
					}
				}
			}
		}
	}

	public void addHardware(SocketBase hardware) throws ClassNotFoundException, SQLException {
		if (hardware == null)
			return;
		int id = MyServerBk.getCodCliente(hardware.getHardware());
		if (!userHardwares.containsKey(id)) {
			UserHardwares userHard = new UserHardwares();
			userHard.addHardware(hardware);
			userHardwares.put(id, userHard);
		} else {
			UserHardwares userHard = userHardwares.get(id);
			userHard.addHardware(hardware);
		}
	}

	public void removeHardware(SocketBase hardware) throws ClassNotFoundException, SQLException, IOException {
		if (hardware == null)
			return;
		int id = MyServerBk.getCodCliente(hardware.getHardware());
		if (userHardwares.containsKey(id)) {
			UserHardwares userHard = userHardwares.get(id);
			if (userHard.contemHardware(hardware)) {
				userHard.removeHardware(hardware);
				if (userHard.naoContemHardwares() && userHard.naoContemClientes()) {
					userHardwares.remove(id);
				}
			}
		}
	}

	public void onClienteCommand(SocketBase socketBase, JSONObject comando) throws IOException, ClassNotFoundException, SQLException {
		Cliente cliente = socketBase.getCliente();
		if (cliente == null)
			return;
		UserHardwares userHardware = userHardwares.get(cliente.getId());
		JSONObject dados = comando.getJSONObject("dados");
		userHardware.onClienteCommand(socketBase, dados);
	}

	public void onHardwareCommand(SocketBase socketBase, JSONObject comando) throws ClassNotFoundException, SQLException, IOException {
		Hardware hardware = socketBase.getHardware();
		if (hardware == null)
			return;
		UserHardwares userHardware = userHardwares.get(MyServerBk.getCodCliente(hardware));
		JSONObject dados = comando.getJSONObject("dados");
		userHardware.onHardwareCommand(socketBase, dados);
	}

	@Override
	public String toString() {
		String string = new String(userHardwares.toString());
		return string;
	}

}
