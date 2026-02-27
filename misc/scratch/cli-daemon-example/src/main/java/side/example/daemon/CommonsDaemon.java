package side.example.daemon;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

@Slf4j
public class CommonsDaemon implements Daemon {
    Thread backgroundThread;

    public static void main(String[] args) throws Exception {
        new CommonsDaemon().start();
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        log.info("init");
    }

    @Override
    public void start() throws Exception {
        log.info("start");

        backgroundThread = new Thread(() -> {
            int i = 0;
            while (true) {
                i++;
                log.info("we are now on: {}", i);
                try {
                    // noinspection BusyWait
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        backgroundThread.start();
    }

    @Override
    public void stop() throws Exception {
        log.info("stop");
        if (backgroundThread != null) backgroundThread.interrupt();
    }

    @Override
    public void destroy() {
        log.info("destroy");
    }
}
