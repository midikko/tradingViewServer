import java.io.*;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.SocketHandler;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by midikko on 13.11.16.
 */
public class ServerThread extends Thread {

    static private Map<String, Integer> files = new HashMap<>();
    static String filesPath;
    int portNum;
    String serverAddress;
    ScheduledExecutorService scheduler;
    ExecutorService executorService;
    boolean suspended = false;


    static private ServerThread instance;

    private ServerThread() {
    }

    private void config() {
        synchronized (Main.class) {
            Main.logger.log(Level.INFO, "Запускаем сервер");
            try {
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
                if(!files.containsKey(file)){
                    files.put(file, 0);
                }
            });
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(new StatisticLogger(), 8, 8, TimeUnit.SECONDS);
            executorService = Executors.newCachedThreadPool();
            Main.class.notifyAll();
        }
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
            Main.logger.log(Level.INFO, "Сервер запущен со следующими параметрами");
            Main.logger.log(Level.INFO, "Адрес сервера: " + serverAddress);
            Main.logger.log(Level.INFO, "Порт сервера:" + portNum);
            Main.logger.log(Level.INFO, "Путь к директории с файлами: " + filesPath);
            serverSocket.setSoTimeout(1000);
            while (!suspended) {
                try {
                    ClientThread clientThread = new ClientThread(serverSocket.accept());
                    clientThread.getClientSocket().setSoTimeout(10000);
                    if (Main.stop) {
                        clientThread.abort();
                    } else {
                        executorService.execute(clientThread);
                        Main.logger.log(Level.INFO, "К серверу подключился клиент " + clientThread.getClientSocket().getInetAddress());
                    }
                } catch (SocketTimeoutException e) {
                    //просто идем дальше
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Integer> getFiles() {
        return Collections.unmodifiableMap(files);
    }

    public static ServerThread getInstance() {
        if (ServerThread.instance == null) {
            ServerThread.instance = new ServerThread();
        }
        return ServerThread.instance;
    }

    public File getFile(String fileName) {
        if (files.containsKey(fileName)) {
            return new File(filesPath + fileName);
        } else {
            return null;
        }
    }

    public void turnOff() throws IOException {
        try {
            System.out.println("Завершаем работу подключенных клиентов");
            executorService.shutdown();
            boolean tasksFinished = executorService.awaitTermination(1, TimeUnit.MINUTES);
            if (tasksFinished) {
                scheduler.shutdown();
                System.out.println("Все клиенты успешно завершили работу. Выключаем сервер.");
            } else {
                System.out.println("Некоторые клиенты выполняют длительную загрузку файлов. прервать передачу? д/н");
                if (Main.consoleReader.readLine().equalsIgnoreCase("д")) {
                    executorService.shutdownNow();
                } else {
                    System.out.println("Ожидаем завершения работы клиентов.");
                    System.out.println("Не реализовано. просто убиваем клиентов.");
                    executorService.shutdownNow();
                    //do smthng
                }
            }

            //прекращаем обработку приходящих и вырубаемся
            suspended = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void incrementStatForFile(String fileName) {
        if (files.containsKey(fileName)) {
            files.put(fileName, files.get(fileName) + 1);
        }
    }

    private void readOldStat(){
        File statFile = new File("statistic.txt");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(statFile.getAbsolutePath())));
                    String string = reader.readLine();
                    files = Arrays.stream(string.trim().split(" "))
//                            .forEach(c -> {
//                        String[] s = c.split(":");
//                    });

                            .collect(Collectors.toMap(c -> c.split(":")[0],c->Integer.parseInt(c.split(":")[1])));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
