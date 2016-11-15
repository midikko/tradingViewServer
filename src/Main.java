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

    public static void main(String[] args){
        configureLogger();
        ServerThread server = new ServerThread();
        server.start();
    }

    public static void configureLogger(){
        logger = Logger.getLogger("MyLog");
        FileHandler fh;

        try {
            fh = new FileHandler("MyLogFile.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.setUseParentHandlers(false);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
