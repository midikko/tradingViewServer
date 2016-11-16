import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;

/**
 * Created by midikko on 13.11.16.
 */
public class ClientThread extends Thread {

    private final Socket clientSocket;

    PrintWriter writer;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            sayHello();
            while (!clientSocket.isClosed()) {
                if (Main.stop) {
                    this.turnOff();
                } else {
                    try {
                        switch (Integer.parseInt(reader.readLine())) {
                            case 1: {
                                sendFiles();
                            }
                            break;
                            case 2: {
                                processFileRequest();
                            }
                            break;
                            case 3: {
                                clientSocket.close();
                            }
                            break;
                            default: {
                                throw new UnsupportedOperationException();
                            }
                        }
                    } catch (SocketTimeoutException e) {
                    }
                }
            }
        } catch (IOException ignored) {
            Main.logger.log(Level.WARNING,ignored.getMessage() + ignored.getCause());
        } finally {
            Main.logger.log(Level.INFO, "Клиент " + clientSocket.getInetAddress() + " отключился от сервера");
        }
    }

    private void processFileRequest() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String fileName = reader.readLine();
        Main.logger.log(Level.INFO, "Начинаем передавать клиенту " + clientSocket.getInetAddress() + " файл " + fileName);
        File file = new File("files/" + fileName);
        int count;
        byte[] buffer = new byte[1024];
        OutputStream out = clientSocket.getOutputStream();
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
                out.flush();
            }
        }
        ServerThread.getInstance().incrementStatForFile(fileName);
        Main.logger.log(Level.INFO, "Клиент " + clientSocket.getInetAddress() + "  скачал файл " + fileName);
    }

    private void sayHello() throws IOException {
        writer.println("Вы подключились к серверу!");
        writer.flush();
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    private void sendFiles() throws IOException {
        String files = String.join(",", ServerThread.getInstance().getFileNames());
        writer.println(files);
        writer.flush();
    }

    public void turnOff() throws IOException {
        writer.println("-1");
        writer.flush();
        clientSocket.close();
    }
}
