import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    public static void main(String[] args) {
        final String SERVER_ADDRESS = "localhost"; // Cambia esto por la dirección IP o nombre del servidor
        final int SERVER_PORT = 2099; // Cambia esto por el puerto del servidor

        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Conectado al servidor");

            DataInputStream entradaServidor = new DataInputStream(socket.getInputStream());
            DataOutputStream salidaCliente = new DataOutputStream(socket.getOutputStream());

            // Solicitar al usuario que ingrese un alias
            System.out.print("Ingresa tu alias: ");
            Scanner scanner = new Scanner(System.in);
            String alias = scanner.nextLine();

            // Enviar el mensaje de unión al servidor
            String joinMessage = "j^" + alias + "@" + socket.getLocalAddress().getHostAddress() + "^";
            salidaCliente.writeUTF(joinMessage);

            // Hilo para recibir mensajes del servidor
            Thread recibirMensajes = new Thread(() -> {
                try {
                    while (true) {
                        String mensaje = entradaServidor.readUTF();
                        // Imprimir el mensaje en el formato (alias): (mensaje)
                        String[] partes = mensaje.split("\\^-\\^", 2);
                        if (partes.length == 2) {
                            System.out.println( partes[0] + ": " + partes[1]);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error al recibir mensajes del servidor: " + e.getMessage());
                }
            });
            recibirMensajes.start();

            // Captura de entrada del usuario y envío al servidor
            while (true) {
                System.out.print("Escribe un mensaje (o 'salir' para desconectarte): ");
                String mensajeUsuario = scanner.nextLine();
                if (mensajeUsuario.equalsIgnoreCase("salir")) {
                    socket.close();
                    System.out.println("Desconectado del servidor");
                    break;
                }
                // Formato del mensaje: m^alias@direccion_ip^-^mensaje^
                String mensaje = "m^" + alias + "@" + socket.getLocalAddress().getHostAddress() + "^-^" + mensajeUsuario + "^";
                salidaCliente.writeUTF(mensaje); // Envía el mensaje al servidor
            }

        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
        }
    }
}
