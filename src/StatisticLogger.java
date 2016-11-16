import java.io.*;
import java.util.logging.Level;

/**
 * Created by midikko on 13.11.16.
 */
public class StatisticLogger implements Runnable {

    @Override
    public void run() {
        File file = new File("statistic.txt");
        try {
            Main.logger.log(Level.INFO, "Сохраняем статистику");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            PrintWriter bw = new PrintWriter(fw);
            StringBuilder statBuilder = new StringBuilder();
            ServerThread.getFileMap().entrySet().stream().map(c -> {
                return c.getKey() + ":" + c.getValue();
            }).forEach(c -> {
                bw.println(c);
            });
            bw.close();
            Main.logger.log(Level.INFO, "Статистика успешно сохранена");
        } catch (IOException e) {
            Main.logger.log(Level.WARNING, e.toString());
        }
    }
}
