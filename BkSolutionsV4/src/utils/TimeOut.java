package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.bksolutionsdomotica.modelo.SocketBase;

public class TimeOut extends Thread {
	private volatile List<SocketBase> socketsBase;

	public TimeOut(List<SocketBase> socketsBase) {
		this.socketsBase = socketsBase;
	}

	@Override
	public void run() {
		long contador = System.currentTimeMillis();
		
		do {
			System.out.println(System.currentTimeMillis() - contador);
			contador = System.currentTimeMillis();
			try {
				sleep(6000);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			List<SocketBase> listaTemporaria = new ArrayList<SocketBase>(socketsBase);

			for (SocketBase sb : listaTemporaria) {
				try {
					sb.sendCommand("\0");
				} catch (IOException e) {

					e.printStackTrace();
					System.out.println("Primeiro erro");
					try {
						sb.closeResouces();
					} catch (IOException e1) {
						e1.printStackTrace();
						System.out.println("Segundo Erro");
					}
				}
			}
		} while (true);
	}
}

//package utils;
//
//import java.io.IOException;
//
//import br.com.bksolutionsdomotica.modelo.SocketBase;
//
//public class TimeOut extends Thread {
//	SocketBase socketBase = null;
//
//	public TimeOut(SocketBase socketBase) {
//		this.socketBase = socketBase;
//	}
//
//	@Override
//	public void run() {
//		do {
//			try {
//				sleep(6000);
//				socketBase.sendCommand("\0");
//				sleep(1000);
//			} catch (IOException | InterruptedException e) {
//
//				e.printStackTrace();
//				try {
//					socketBase.closeResouces();
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
//				break;
//			}
//		} while (true);
//	}
//}
