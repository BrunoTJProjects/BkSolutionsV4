package br.com.bksolutionsdomotica.modelo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SocketBase {
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private Hardware hardware;
	private Cliente cliente;
//	private ObjectInputStream inObj;
//	private ObjectOutputStream outObj;

	public SocketBase(Socket socket) throws IOException {
		this.socket = socket;
		if (socket != null) {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		}
	}

	public void closeResouces() throws IOException {
		if (in != null) {
			in.close();
		}
		if (out != null) {
			out.close();
		}
		if (socket != null) {
			socket.close();
		}
	}

	public void sendCommand(String comando) throws IOException {
		if (comando != null && !comando.isEmpty()) {
			if (out != null) {
				out.write(comando);
				out.flush();
			}
		}
	}

	public String commandReceiver() throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		if (in != null && in.ready()) {
			while (in.ready()) {
				int retorno = in.read();
				stringBuilder.append(Character.toChars(retorno));
			}
		}
		return stringBuilder.toString();
	}

	public Hardware getHardware() {
		return hardware;
	}

	public void setHardware(Hardware hardware) {
		this.hardware = hardware;
		this.cliente = null;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
		this.hardware = null;
	}
	
	public boolean isCliente() {
		return cliente != null && hardware == null;
	}
	
	public boolean isHardware() {
		return hardware != null && cliente == null;
	}
	
	public boolean isLogado() {
		return hardware != null || cliente != null;
	}

//	public void sendObject(Object obj) throws IOException {
//		if (obj != null) {
//			outObj = new ObjectOutputStream(socket.getOutputStream());
//			outObj.writeObject(obj);
//			outObj.flush();
//			outObj = null;
//
//		}
//	}
//
//	public Object objectReceiver() throws IOException, ClassNotFoundException {
//		Object obj = null;
//		if (inObj != null) {
//			while (inObj.available() > -1) {
//				obj = inObj.readObject();
//			}
//		}
//		return obj;
//	}
}