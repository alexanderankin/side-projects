package side.pxe;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import side.pxe.dhcp.ReferenceUdpEchoClient;

public interface FixLogging {
    static void fixLogging() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getILoggerFactory()
                .getLogger(Logger.ROOT_LOGGER_NAME))
                .setLevel(Level.WARN);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getILoggerFactory()
                .getLogger(ReferenceUdpEchoClient.class.getPackageName()))
                .setLevel(Level.DEBUG);
    }
}
