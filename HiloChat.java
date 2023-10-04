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
	


	public HiloChat(Socket socket, Vector<Socket> clients, String alias, HashMap<String, Socket> aliasToSocketMap){
		this.socket = socket;
		this.clients = clients;
		this.alias = alias;
		this.aliasToSocketMap = aliasToSocketMap;
		aliasToSocketMap.put(alias, socket);
	}

	public void enviaListaDeUsuarios() {
		try {
			StringBuilder listaAlias = new StringBuilder();
	
			for (String alias : aliasToSocketMap.keySet()) {
				listaAlias.append(alias).append(",");
			}
	
			// envia la lista  a todos los clientes 
			for (Socket socketTmp : clients) {
                DataOutputStream tmpNetOut = new DataOutputStream(socketTmp.getOutputStream());
                tmpNetOut.writeUTF(listaAlias.toString());
            }
	
			// Envia la lista de alias al cliente
			
		} catch (IOException ioe) {
			System.err.println("Problemas en el envío de la lista de usuarios");
		}
	}
	
	

	public void inicializa(){
		
		enviaListaDeUsuarios();
		//System.out.println(listaAlias);
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

	public void enviaMensaje(String msg, Socket dest){
		try {
			netOut = new DataOutputStream(dest.getOutputStream());
			netOut.writeUTF(msg);
		} catch (IOException ioe) {
			System.err.println("Problemas en el envio de mensaje privado");
		}

	}

	public void eliminarSocketDelVector(String alias) {
        if (socket != null && clients != null) {
            
			aliasToSocketMap.remove(alias);
			
			//System.out.println(listaAlias);
			clients.remove(socket);
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
					} else if (command.equalsIgnoreCase("p")){
						String aliasR = st.nextToken();
						String aliasD = st.nextToken();
						Socket destinatarioSocket = aliasToSocketMap.get(aliasD);
						enviaMensaje(msg, destinatarioSocket);
						
					}
				} 
			}
				

		}catch (IOException ioe){
			System.err.println("error al ejecutar el hilo");
			
			System.err.println(ioe.getMessage());
		}
	}
}