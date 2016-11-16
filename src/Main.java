import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {

    public static Logger logger;
    public static boolean stop = false;
    public static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException, InterruptedException {
        configureLogger();
        ServerThread server = ServerThread.getInstance();
        server.start();
        synchronized (Main.class){
            Main.class.wait();
        }
        System.out.println("Сервер запущен.");
        System.out.println("1. Просмотреть статистику");
        System.out.println("2. Завершить работу сервера");
        while(!stop){
            switch (consoleReader.readLine()){
                case "1" :{
                    ServerThread.getFiles()
                            .entrySet()
                            .stream()
                            .forEach(entry -> {
                                System.out.println(entry.getKey() + " -- " + entry.getValue());
                            });
                }break;
                case "2" :{
                    stop = true;
                    server.turnOff();
                }
            }
        }
    }

    public static void configureLogger(){
        logger = Logger.getLogger("MyLog");
        FileHandler fh;

        try {
            fh = new FileHandler("MyLogFile.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
//            logger.setUseParentHandlers(false);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
