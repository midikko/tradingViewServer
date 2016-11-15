import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by midikko on 13.11.16.
 */
public  class ServerThread extends Thread{

    static private Map<String,Integer> files = new HashMap<>();
    static String filesPath;
    int portNum;
    String serverAddress;

    static private ServerThread instance;

    private void config(){
        Main.logger.log(Level.INFO,"Запускаем сервер");
        try{
            System.out.println("Введите адрес сервера (localhost по умолчанию): ");
            serverAddress = Main.consoleReader.readLine();

            System.out.println("Введите номер порта, на котором размещен сервер (4444 по умолчанию): ");
            String port = Main.consoleReader.readLine();
            portNum = port.isEmpty() ? 4444 : Integer.parseInt(port);

            System.out.println("Введите путь к скачиваемым файлам (\"files/\" по умолчанию): ");
            filesPath = Main.consoleReader.readLine();
            filesPath = filesPath.isEmpty() ? "files/" : filesPath;
        } catch (IOException e) {
            e.printStackTrace();
        }
        getFileNames().forEach(file -> {
            files.put(file,0);
        });
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new StatisticLogger(), 8, 8, TimeUnit.SECONDS);

    }

    public List<String> getFileNames() {
        File folder = new File(filesPath);
        File[] listOfFiles = folder.listFiles();
        return Stream.of(listOfFiles)
                .filter(f -> f.isFile())
                .map(File::getName)
                .collect(Collectors.toList());
    }

    @Override
    public void run() {
        config();
        ServerThread.instance = this;
        try (ServerSocket serverSocket = new ServerSocket(portNum)) {
            Main.logger.log(Level.INFO,"Сервер запущен со следующими параметрами");
            Main.logger.log(Level.INFO,"Адрес сервера: " + serverAddress);
            Main.logger.log(Level.INFO,"Порт сервера:" + portNum);
            Main.logger.log(Level.INFO,"Путь к директории с файлами: " + filesPath);
            while (!serverSocket.isClosed()) {
                ClientThread clientThread = new ClientThread(serverSocket.accept());
                clientThread.start();
                Main.logger.log(Level.INFO,"К серверу подключился клиент " + clientThread.getClientSocket().getInetAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Integer> getFiles() {
        return Collections.unmodifiableMap(files);
    }

    public static ServerThread getInstance(){
        return ServerThread.instance;
    }

    public File getFile(String fileName){
        if(files.containsKey(fileName)){
            return new File(filesPath+fileName);
        }else{
            return null;
        }
    }

    public void incrementStatForFile(String fileName) {
        if(files.containsKey(fileName)){
            files.put(fileName, files.get(fileName)+1);
        }
    }
}
