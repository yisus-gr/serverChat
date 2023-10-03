import java.io.*;
import java.net.Socket;
import java.util.Vector;
import java.util.StringTokenizer;
import java.net.InetAddress;

public class HiloChat implements Runnable{

	private Vector<Socket> clients;
	private Socket socket;
	private DataInputStream netIn;
	private DataOutputStream netOut;
	private String[] listaAlias;


	public HiloChat(Socket socket, Vector<Socket> clients ){
		this.socket = socket;
		this.clients = clients;
	}

	public void inicializa(){
		enviaMensaje(clients.toString());
		System.out.println(clients.toString());
		try{			
			netIn = new DataInputStream(socket.getInputStream());
		}catch (IOException ioe){
			System.err.println("Problemas en la creación de flujos inicializa()");
		}
	}

	public void enviaMensaje(String msg){	
		//sSystem.out.println(msg);	
		try{
			for (Socket socketTmp : clients){
                
				netOut = new DataOutputStream(socketTmp.getOutputStream());
				netOut.writeUTF(msg);
			}
		}catch (IOException ioe){
			System.err.println("Problemas en el envio de datos enviaMensaje()");
		}
	}

	public void eliminarSocketDelVector() {
        if (socket != null && clients != null) {
            clients.remove(socket);
			System.out.println(clients.toString());
			enviaMensaje(clients.toString());
		}

    }

	public void run(){
		inicializa();
		try {
			
			while(true){
				String msg = netIn.readUTF();
				if (msg.equals("DESCONECTAR")) {
					// Cerrar la conexión y eliminar el cliente de la lista
					eliminarSocketDelVector();
					socket.close();
					break; // Salir del bucle
				}


				StringTokenizer st = new StringTokenizer(msg, "^");
				System.out.println(st.nextToken());
				if (st.countTokens() >=  4){
					String command = st.nextToken();
					if (command.equalsIgnoreCase("m")){
						enviaMensaje(msg);
					} else {
						String res = "m^Server@";
						InetAddress dir = InetAddress.getLocalHost();
						res += dir.getHostAddress();
						String alias = st.nextToken();
						res += alias.substring(0,alias.indexOf("@")); 
						if (command.equalsIgnoreCase("j")){
							res += "^_^joined from " + alias.substring(alias.indexOf("@") + 1) + "^";
							System.out.println(res);
							enviaMensaje(res);
						} else {
							res += "^-^parted from " + alias.substring(alias.indexOf("@") + 1) + "^";
							enviaMensaje(res);
							
							clients.remove(socket);
							
							return;
						}
					}
				} 
			}
				

		}catch (IOException ioe){
			System.err.println("error al ejecutar el hilo");
			
			System.err.println(ioe.getMessage());
		}
	}
}