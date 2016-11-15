import java.io.*;
import java.net.Socket;
import java.util.logging.Level;

/**
 * Created by midikko on 13.11.16.
 */
public class ClientThread extends Thread{

    private final Socket clientSocket;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            sayHello();
            while(clientSocket.isConnected()){
                switch (Integer.parseInt(reader.readLine())){
                    case 1 : {
                        sendFiles();
                    } break;
                    case 2 : {
                        processFileRequest();
                    } break;
                    case 3 : {
                        clientSocket.close();
                    } break;
                    default: {
                        throw new UnsupportedOperationException();
                    }
                }
            }
        } catch (IOException ignored) {
        } finally {
            Main.logger.log(Level.INFO, "Клиент "+ clientSocket.getInetAddress() + " отключился от сервера");
        }
    }

    private void processFileRequest() throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String fileName = reader.readLine();
        Main.logger.log(Level.INFO, "Начинаем передавать клиенту "+ clientSocket.getInetAddress() + " файл " + fileName);
        File file = new File("files/"+fileName);
        int count;
        byte[] buffer = new byte[1024];

        OutputStream out = clientSocket.getOutputStream();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
            out.flush();
        }
        ServerThread.getInstance().incrementStatForFile(fileName);
        Main.logger.log(Level.INFO, "Клиент "+ clientSocket.getInetAddress() + "  скачал файл " + fileName);
    }

    private void sayHello() throws IOException {
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        writer.println("Вы подключились к серверу!");
        writer.flush();
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    private void sendFiles() throws IOException {
        String files = String.join(",", ServerThread.getInstance().getFileNames());
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        writer.println(files);
        writer.flush();
    }
}
