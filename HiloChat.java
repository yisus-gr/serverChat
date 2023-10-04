import java.io.*;
import java.net.Socket;
import java.util.Vector;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.net.InetAddress;

public class HiloChat implements Runnable{

	private Vector<Socket> clients;
	private Socket socket;
	private DataInputStream netIn;
	private DataOutputStream netOut;
	private String alias;
	private HashMap<String, Socket> aliasToSocketMap = new HashMap<>();
	private StringBuilder listaAlias = new StringBuilder("Usuarios conectados: ");


	public HiloChat(Socket socket, Vector<Socket> clients, String alias, HashMap<String, Socket> aliasToSocketMap){
		this.socket = socket;
		this.clients = clients;
		this.alias = alias;
		this.aliasToSocketMap = aliasToSocketMap;
		aliasToSocketMap.put(alias, socket);
	}

	public void enviaListaDeUsuarios() {
		try {
			
			
			for (String alias : aliasToSocketMap.keySet()) {
				listaAlias.append(alias).append(", ");
			}
	
			// Elimina la última coma y espacio
			listaAlias.setLength(listaAlias.length() - 2);
	
			// Envia la lista de alias al cliente
			netOut = new DataOutputStream(socket.getOutputStream());
			netOut.writeUTF(listaAlias.toString());
		} catch (IOException ioe) {
			System.err.println("Problemas en el envío de la lista de usuarios");
		}
	}
	

	public void inicializa(){
		
		enviaListaDeUsuarios();
		System.out.println(listaAlias);
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

	public void eliminarSocketDelVector(String alias) {
        if (socket != null && clients != null) {
            clients.remove(socket);
			aliasToSocketMap.remove(alias);
			System.out.println(listaAlias);
			enviaListaDeUsuarios();
		}

    }

	public void run(){
		inicializa();
		try {
			
			while(true){
				String msg = netIn.readUTF();
				if (msg.equals("DESCONECTAR")) {
					// Cerrar la conexión y eliminar el cliente de la lista
					eliminarSocketDelVector(alias);
					socket.close();
					break; // Salir del bucle
				}


				StringTokenizer st = new StringTokenizer(msg, "^");
				//System.out.println(st.nextToken());
				if (st.countTokens() >=  4){
					String command = st.nextToken();
					if (command.equalsIgnoreCase("m")){
						enviaMensaje(msg);
					} else {
						String res = "m^Server@";
						InetAddress dir = InetAddress.getLocalHost();
						res += dir.getHostAddress();
						String alias2 = st.nextToken();
						res += alias2.substring(0,alias2.indexOf("@")); 
						if (command.equalsIgnoreCase("j")){
							res += "^_^joined from " + alias2.substring(alias.indexOf("@") + 1) + "^";
							System.out.println(res);
							enviaMensaje(res);
						} else {
							res += "^-^parted from " + alias2.substring(alias.indexOf("@") + 1) + "^";
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