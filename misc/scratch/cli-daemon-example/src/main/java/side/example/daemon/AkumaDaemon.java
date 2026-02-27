package side.example.daemon;

import com.sun.akuma.Daemon;
import lombok.SneakyThrows;

import java.io.FileWriter;

public class AkumaDaemon {
    @SneakyThrows
    public static void main(String[] args) {
        Daemon d = new Daemon();
        if (d.isDaemonized()) {
            // perform initialization as a daemon
            // this involves in closing file descriptors, recording PIDs, etc.
            d.init();
        } else {
            // if you are already daemonized, no point in daemonizing yourself again,
            // so do this only when you aren't daemonizing.
            d.daemonize();
            System.exit(0);
        }
        // your normal main code follows
        // this part can be executed in two ways
        // 1) the user runs your process in the foreground
        // 2) you decided to daemonize yourself, in which case the newly forked daemon will execute this code,
        //    while the originally executed foreground Java process exits before it gets here.
        try (FileWriter fileWriter = new FileWriter("/tmp/test-" + System.currentTimeMillis())) {
            try {
                for (int i = 0; i < 10000; i++) {
                    fileWriter.write("abc-" + i + System.lineSeparator());
                    fileWriter.flush();
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                fileWriter.write("done" + System.lineSeparator());
            }
        }
    }
}
