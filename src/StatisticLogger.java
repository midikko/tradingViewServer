import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
            BufferedWriter bw = new BufferedWriter(fw);
            StringBuilder statBuilder = new StringBuilder();
            ServerThread.getFiles().entrySet().stream().map(c -> {
                return " " + c.getKey() + ":" + c.getValue();
            }).forEach(c -> {
                statBuilder.append(c);
            });

            bw.append("\n" + statBuilder.toString());
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
