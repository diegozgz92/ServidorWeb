import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Created by diego on 26/02/2016.
 */
public class WebServer {

    private static final int port = 9090;
    private static final String root = "C:\\Users\\diego\\Documents\\UNIVERSIDAD\\4º\\Sistemas y Tecnologias Web\\Practicas\\Practica1\\ServidorWeb\\WEB-INF";

    public static void main(String args[]) throws UnknownHostException, IOException {
        byte[] buffer = new byte[1024]; int bytes;
        ServerSocket servidor = new ServerSocket(port);
        while(true) {
            Socket cliente = servidor.accept();
            gestionarPeticion(cliente);
        }
    }


    /**
     * Función que comprueba que la petición esta correctamente formada, es decir, es GET, que el fichero
     * existe o es '/' por lo que se devuelve el index.html y que el fichero es accesible por el usuario.
     */
    private static void gestionarPeticion(Socket cliente) throws IOException {
        System.setProperty("line.separator","\r\n");
        Scanner lee = new Scanner (cliente.getInputStream());
        System.out.println(lee.nextLine());
        String respuesta = null;
        String html = null;
        if(lee.next().equalsIgnoreCase("GET")){
            String file = lee.next();
            if(file.equals("/")){
                file = root + "\\index.html";
            } else{
                file = root + "\\" + file.substring(1);
            }
            File fichero = new File(file);
            String absolut = fichero.getAbsolutePath();
            if (fichero.exists() && absolut.equals(file) && lee.next().equalsIgnoreCase("HTTP/1.0") && lee.hasNextLine()) {
                try {
                    PrintWriter envia = new PrintWriter(cliente.getOutputStream(),true);
                    envia.println("HTTP/1.0 200 OK");
                    String cabecera = obtenerCabeceraFormato(file);
                    envia.println(cabecera);
                    cabecera = "Content-Length: " + fichero.length();
                    envia.println(cabecera);
                    envia.println();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                enviarFichero(file, cliente);
            } else if(!fichero.exists()){
                respuesta = "HTTP/1.0 404 Not Found";
                html = "<html><body><h1>404 Not Found</h1></body></html>";
            } else if(absolut.equals(file)){
                respuesta = "HTTP/1.0 401 Unauthorized";
                html = "<html><body><h1>401 Unauthorized</h1></body></html>";
            }
            else {
                respuesta = "HTTP/1.0 400 Bad Request";
                html = "<html><body><h1>400 Bad Request</h1></body></html>";
            }

        } else{
            respuesta = "HTTP/1.0 501 Not Implemented”.";
            html = "<html><body><h1>501 Not Implemented</h1></body></html>";
        }
        if(respuesta!=null){
            try {
                PrintWriter envia = new PrintWriter(cliente.getOutputStream(),true);
                envia.println(respuesta);
                envia.println();
                envia.println(html);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void enviarFichero(String file, Socket cliente) {
        byte[] buffer = new byte[1024];
        int bytes;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
            while ((bytes = fis.read(buffer)) != -1){
                cliente.getOutputStream().write(buffer, 0, bytes);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            cliente.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String obtenerCabeceraFormato(String file) {
        String cabecera = "Content-Type: ";
        if(file.endsWith(".html") || file.endsWith(".htm")){
            cabecera = cabecera + "text/html";
        } else if(file.endsWith(".gif")){
            cabecera = cabecera + "image/gif";
        } else {
            cabecera = cabecera + "application/octet-stream";
        }
        return cabecera;
    }
}
