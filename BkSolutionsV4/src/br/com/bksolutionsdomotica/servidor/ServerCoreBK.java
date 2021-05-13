package br.com.bksolutionsdomotica.servidor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import br.com.bksolutionsdomotica.modelo.SocketBase;

public class ServerCoreBK {
	private static final String LOGIN_REQUEST		= "login_request";
	private static final String LOGOUT_REQUEST		= "logout_request";
	private static final String COMMAND_REQUEST		= "command_request";
	private static final String IS_LOGGED_REQUEST	= "is_logged_request";

	public static final String TYPE_CLIENTE = "cliente";
	public static final String TYPE_HARDWARE = "hardware";

	private int port;
	private boolean threadStart = true;
	private volatile ServerCore serverCore;
	private volatile List<SocketBase> socketsBase;

	public ServerCoreBK(int port, InterfaceCommand listener) {
		this.port = port;
		serverCore = new ServerCore();
		serverCore.setInterfaceConnectionListener(listener);
		socketsBase = new ArrayList<SocketBase>();
	}

	public void init() throws IOException {

		serverCore.start();
		new Thread(new ServidorBK(port, socketsBase)).start();
	}

	public void restartThreadServer() {
		threadStart = true;
		if (!serverCore.isAlive()) {
			serverCore = null;
			serverCore = new ServerCore();
			serverCore.start();
		}
	}

	public void stopThread() {
		threadStart = false;
		while (serverCore.isAlive()) {

		}
		return;
	}

//	public void enviaComando(SocketBase sb, String command) throws IOException {
//		if (sb != null && command != null && !command.isEmpty()) {
//			serverCore.comando = command;
//			serverCore.sb = sb;
//		} else {
//			serverCore.comando = null;
//			serverCore.sb = null;
//		}
//	}

	public void removeSocketBase(SocketBase sb) throws IOException {
		sb.setHardware(null);
		sb.setCliente(null);
		sb.closeResouces();
		socketsBase.remove(sb);
	}

	private class ServerCore extends Thread {
		private String comando;
		private SocketBase sb;
		private InterfaceCommand interfaceCommand;

		public ServerCore() {

		}

		@Override
		public void run() {
			while (threadStart) {

				List<SocketBase> listaTemporaria = new ArrayList<SocketBase>(socketsBase);

				for (SocketBase sb : listaTemporaria) {
					try {
						runOnce(sb);
//						System.out.println(MyServerBk.getGerenciador().toString() + "THREADS: " + Thread.activeCount());// Retirar isso depois
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void runOnce(SocketBase sb) throws ClassNotFoundException, SQLException, IOException {

			try {
				String string = sb.commandReceiver();

				if (string != null && !string.isEmpty()) {

					JSONObject jsonObject;

					if (JSONObject.isJSONValid(string)) {

						jsonObject = new JSONObject(string);
						JSONObject requisicao = jsonObject.getJSONObject("requisicao");
						String deviceType = requisicao.getString("deviceType");
						String tipoReq = requisicao.getString("tipoReq");
						
						if (sb.isLogado()) {
							if (IS_LOGGED_REQUEST.equals(tipoReq)) {
								sb.sendCommand("yes");
							}
							if (sb.isHardware()) {
								switch (tipoReq) {
								case LOGOUT_REQUEST:
									interfaceCommand.onHardwareSignOut(sb);
									break;

								case COMMAND_REQUEST:									
									interfaceCommand.onHardwareCommand(sb, requisicao);
									break;
								}
							} else if (sb.isCliente()) {
								switch (tipoReq) {
								case LOGOUT_REQUEST:
									interfaceCommand.onClienteSignOut(sb);
									break;

								case COMMAND_REQUEST:
									interfaceCommand.onClienteCommand(sb, requisicao);
									break;
								}
							}
						} else {
							if (IS_LOGGED_REQUEST.equals(tipoReq)) {
								sb.sendCommand("no");
							}
							if (deviceType != null && !deviceType.isEmpty()) {
								if (deviceType.equals(TYPE_HARDWARE)) {

									switch (tipoReq) {
									case LOGIN_REQUEST:
										interfaceCommand.onHardwareSignIn(sb, requisicao.getString("login"),
												requisicao.getString("password"));
										break;

									}

								} else if (deviceType.equals(TYPE_CLIENTE)) {

									switch (tipoReq) {
									case LOGIN_REQUEST:
										interfaceCommand.onClienteSignIn(sb, requisicao.getString("login"),
												requisicao.getString("password"));
										break;
									}
								}
							}
						}
					}
				}
				enviarComando(sb);

			} catch (IOException | NullPointerException e) {
				if(sb.isLogado()) {
					if(sb.isCliente()) {
						interfaceCommand.onClienteSignOut(sb);
					}
					if(sb.isHardware()) {
						interfaceCommand.onHardwareSignOut(sb);
					}
				}
				removeSocketBase(sb);
				e.printStackTrace();
			}

		}

		private void enviarComando(SocketBase sb) throws IOException {
			if (comando != null && !comando.isEmpty()) {
				if (this.sb != null && sb != null) {
					if (this.sb.equals(sb)) {
						sb.sendCommand(comando);
						this.comando = null;
						this.sb = null;
					}
				}
			}
		}

		public void setInterfaceConnectionListener(InterfaceCommand interfaceCommand) {
			this.interfaceCommand = interfaceCommand;
		}

	}

	public interface InterfaceCommand {

		public void onHardwareSignIn(SocketBase socketBase, String login, String password)
				throws ClassNotFoundException, SQLException, IOException;

		public void onHardwareSignOut(SocketBase socketBase) throws ClassNotFoundException, SQLException, IOException;

		public void onHardwareCommand(SocketBase socketBase, JSONObject jsonObject) throws IOException, ClassNotFoundException, SQLException;

		public void onClienteSignIn(SocketBase socketBase, String login, String password)
				throws ClassNotFoundException, SQLException, IOException;

		public void onClienteSignOut(SocketBase socketBase) throws ClassNotFoundException, SQLException, IOException;

		public void onClienteCommand(SocketBase socketBase, JSONObject jsonObject)
				throws IOException, ClassNotFoundException, SQLException;
		
	}
}