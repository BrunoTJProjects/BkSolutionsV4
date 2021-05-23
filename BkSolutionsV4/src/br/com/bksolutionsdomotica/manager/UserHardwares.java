package br.com.bksolutionsdomotica.manager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import br.com.bksolutionsdomotica.modelo.SocketBase;
import utils.Utils;

public class UserHardwares {

	private static final String LOG_OUT = "Voce foi desconectado";

	private static final String CODE_REQUEST_GETKEYS = "getKeys";
	private static final String CODE_REQUEST_GETKEY = "getKey";
	private static final String CODE_REQUEST_SETKEYS = "setKeys";
	private static final String CODE_REQUEST_SETKEY = "setKey";
	private static final String CODE_REQUEST_DELETEKEY = "deleteKey";
	private static final String CODE_REQUEST_CONFIRM = "confirm";

	private static final String REQUEST = "request";
	private static final String MAC_KEY = "mac";
	private static final String CHAVE_KEY = "key";
	private static final String VALUE_KEY = "value";
	private static final String CHAVE_KEYS = "keys";
	private static final String REQUEST_CODE = "codReq";

	private static final String HARDWARE_NOT_CONNECTED = "Este dispositivo nao esta conectado";
	private static final String SUCESS_MSG = "ok";

	private List<SocketBase> clientes = new ArrayList<SocketBase>();
	private HashMap<String, SocketBase> hardwares = new HashMap<String, SocketBase>();
	private HashMap<String, SocketBase> socketsWaitConfirmation = new HashMap<String, SocketBase>();

	public void onClienteCommand(SocketBase cliente, JSONObject comando)
			throws IOException, ClassNotFoundException, SQLException {
		String mac = comando.getString(MAC_KEY);

		if (mac == null || mac.isEmpty() || !contemHardware(mac)) {
			cliente.sendCommand(HARDWARE_NOT_CONNECTED);
			return;
		}

		String request = comando.getString(REQUEST);
		SocketBase hardware;
		String key;
		String codReq;

		switch (request) {
		case CODE_REQUEST_GETKEYS:
			comando.put(REQUEST, CODE_REQUEST_SETKEYS);
			comando.put(CHAVE_KEYS, cliente.getCliente().getChaves(mac));
			cliente.sendCommand(comando.toString());
			break;
		case CODE_REQUEST_GETKEY:
			comando.put(REQUEST, CODE_REQUEST_SETKEY);
			key = comando.getString(CHAVE_KEY);
			comando.put(VALUE_KEY, cliente.getCliente().getChave(mac, key));
			cliente.sendCommand(comando.toString());
			break;
		case CODE_REQUEST_SETKEYS:
			codReq = Utils.radomString();
			comando.put(REQUEST_CODE, codReq);
			hardware = hardwares.get(mac);
			hardware.sendCommand(comando.toString());
			waitConfirm(codReq, cliente);
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					long contador = System.currentTimeMillis();
					try {
						while (!wasConfimed(codReq)) {
							if ((System.currentTimeMillis() - contador) > 10000) {
								confirmReceived(codReq);
								return;
							}
						}
						JSONObject keys = comando.getJSONObject(CHAVE_KEYS);
						cliente.getCliente().setChaves(mac, keys);
						for (SocketBase sb : clientes) {
							if (sb == cliente)
								continue;
							sb.sendCommand(comando.toString());
						}

						cliente.sendCommand(SUCESS_MSG);

					} catch (ClassNotFoundException | SQLException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
			break;
		case CODE_REQUEST_SETKEY:
			codReq = Utils.radomString();
			comando.put(REQUEST_CODE, codReq);
			hardware = hardwares.get(mac);
			hardware.sendCommand(comando.toString());
			key = comando.getString(CHAVE_KEY);
			String value = comando.getString(VALUE_KEY);
			waitConfirm(codReq, cliente);
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					long contador = System.currentTimeMillis();
					try {
						while (!wasConfimed(codReq)) {
							if ((System.currentTimeMillis() - contador) > 10000) {
								confirmReceived(codReq);
								return;
							}
						}
						cliente.getCliente().setChave(mac, key, value);
						for (SocketBase sb : clientes) {
							if (sb == cliente)
								continue;
							sb.sendCommand(comando.toString());
						}
						cliente.sendCommand(SUCESS_MSG);
					} catch (ClassNotFoundException | SQLException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
			break;
		case CODE_REQUEST_DELETEKEY:
			key = comando.getString(CHAVE_KEY);
			cliente.getCliente().excluirChave(mac, key);
			cliente.sendCommand(SUCESS_MSG);
			break;
		}

	}

	public void onHardwareCommand(SocketBase hardware, JSONObject comando)
			throws IOException, ClassNotFoundException, SQLException {

		String request = comando.getString("request");
		String key;

		switch (request) {
		case CODE_REQUEST_GETKEYS:
			comando.put(REQUEST, CODE_REQUEST_SETKEYS);
			comando.put(CHAVE_KEYS, hardware.getHardware().getChaves());
			hardware.sendCommand(comando.toString());
			break;
		case CODE_REQUEST_GETKEY:
			comando.put(REQUEST, CODE_REQUEST_SETKEY);
			key = comando.getString(CHAVE_KEY);
			comando.put(VALUE_KEY, hardware.getHardware().getChave(key));
			hardware.sendCommand(comando.toString());
			break;
		case CODE_REQUEST_SETKEYS:
			JSONObject keys = comando.getJSONObject(CHAVE_KEYS);
			hardware.getHardware().setChaves(keys);
			comando.put(MAC_KEY, hardware.getHardware().getMac());
			for (SocketBase sb : clientes) {
				sb.sendCommand(comando.toString());
			}
			hardware.sendCommand(SUCESS_MSG);
			break;
		case CODE_REQUEST_SETKEY:
			key = comando.getString(CHAVE_KEY);
			String value = comando.getString(VALUE_KEY);
			hardware.getHardware().setChave(key, value);
			comando.put(MAC_KEY, hardware.getHardware().getMac());
			for (SocketBase sb : clientes) {
				sb.sendCommand(comando.toString());
			}
			hardware.sendCommand(SUCESS_MSG);
			break;
		case CODE_REQUEST_CONFIRM:
			String codReq = comando.getString(REQUEST_CODE);
			confirmReceived(codReq);
			break;
		}

	}

	public void addClientes(SocketBase cliente) {
		if (!clientes.contains(cliente)) {
			clientes.add(cliente);
		}
	}

	public void removeCliente(SocketBase cliente) {
		if (clientes.contains(cliente)) {
			try {
				cliente.sendCommand(LOG_OUT);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clientes.remove(cliente);
			cliente.setCliente(null);
		}
	}

	public boolean contemCliente(SocketBase cliente) {
		return clientes.contains(cliente);
	}

	public boolean naoContemClientes() {
		return clientes.isEmpty();
	}

	public void addHardware(SocketBase hardware) throws ClassNotFoundException, SQLException, IOException {
		if (!hardwares.containsKey(hardware.getHardware().getMac())) {
			hardwares.put(hardware.getHardware().getMac(), hardware);
		} else {
			hardware.closeResouces();
			hardware.setHardware(null);
		}
	}

	public void removeHardware(SocketBase hardware) {
		if (hardwares.containsKey(hardware.getHardware().getMac())) {
			try {
				hardware.sendCommand(LOG_OUT);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			hardwares.remove(hardware.getHardware().getMac());
			hardware.setHardware(null);
		}
	}

	public boolean contemHardware(SocketBase hardware) {
		return contemHardware(hardware.getHardware().getMac());
	}

	private boolean contemHardware(String mac) {
		return hardwares.containsKey(mac);
	}

	public boolean naoContemHardwares() {
		return hardwares.isEmpty();
	}

	@Override
	public String toString() {
		return "Numero de clientes: " + clientes.size() + "/ Numero de Hardwares: " + hardwares.size();
	}

	public synchronized void waitConfirm(String codReq, SocketBase socketBase) {
		socketsWaitConfirmation.put(codReq, socketBase);
	}

	public synchronized void confirmReceived(String codReq) {
		socketsWaitConfirmation.remove(codReq);
	}

	public synchronized boolean wasConfimed(String codReq) {
		return !socketsWaitConfirmation.containsKey(codReq);
	}

}