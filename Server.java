import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {

    private static final int PORT = 2099;
    private static Vector<Socket> clients = new Vector<Socket>();

    private ServerSocket inicializaServer(){
        try{
            ServerSocket sSocket = new ServerSocket(PORT);            
            return sSocket;
        } catch (IOException ioe){
            System.err.println("No se puede abrir el puerto" + ioe.getMessage());
        }
        return null;
    }

 

    public Server(){
        ServerSocket welcomeSocket = inicializaServer();
        System.out.println("Servidor iniciado en el puerto: " + PORT);
        System.out.println("Ctrl + C para detener");
        if (welcomeSocket != null){
            while (true){
                try{
                    Socket socket = welcomeSocket.accept();
                    clients.add(socket);
                    System.out.println("Conexión iniciada" + socket);
                    
                    HiloChat hiloChat = new HiloChat(socket, clients);
                    
                    Thread thread = new Thread(hiloChat);
                    thread.start();

                    if(socket.isClosed()){
                        clients.remove(socket);
                        break;
                    }
                } catch (IOException ioe){
                    System.err.println("Hay un error en la creación de conexiones, se cerrara el servidor");
                    System.err.println(ioe.getMessage());
                }
                
            }
        }

    }

    public static void main(String [] args){
        new Server();
    }


}