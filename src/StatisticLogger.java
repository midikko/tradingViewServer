import java.io.*;
import java.util.Collections;

/**
 * Created by midikko on 13.11.16.
 */
public class StatisticLogger implements Runnable {

    @Override
    public void run() {
        File file = new File("statistic.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            PrintWriter bw = new PrintWriter(fw);
            StringBuilder statBuilder = new StringBuilder();
            ServerThread.getFiles().entrySet().stream().map(c -> {
                return " " + c.getKey() + ":" + c.getValue();
            }).forEach(c -> {
                statBuilder.append(c);
            });

            bw.println(statBuilder.toString());
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
