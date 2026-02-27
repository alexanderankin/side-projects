package info.ankin.projects.ssh.ssh_server;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class TtyShellFactory implements ShellFactory {
    @SneakyThrows
    @Override
    public Command createShell(ChannelSession channel) {
        return createNewShell(channel);
    }

    public Command createNewShell(ChannelSession channel) {
        return new Command() {
            PtyProcess ptyProcess;
            ExitCallback callback;

            /**
             * @param callback The {@link ExitCallback} to call when shell is closed
             */
            @Override
            public void setExitCallback(ExitCallback callback) {
                this.callback = callback;
            }

            /**
             * @param err The {@link OutputStream} used by the shell to write its errors
             */
            @SneakyThrows
            @Override
            public void setErrorStream(OutputStream err) {
                new Thread(() -> transferAToB(getPtyProcess().getErrorStream(), err)).start();
            }

            /**
             * @param in The {@link InputStream} used by the shell to read input.
             */
            @SneakyThrows
            @Override
            public void setInputStream(InputStream in) {
                new Thread(() -> transferAToB(in, getPtyProcess().getOutputStream())).start();
            }

            /**
             * @param out The {@link OutputStream} used by the shell to write its output
             */
            @Override
            public void setOutputStream(OutputStream out) {
                new Thread(() -> {
                    transferAToB(getPtyProcess().getInputStream(), out);
                    destroy(channel);
                }).start();
            }

            /**
             * @param channel The {@link ChannelSession} through which the command has been received
             * @param env     The {@link Environment}
             */
            @Override
            public void start(ChannelSession channel, Environment env) throws IOException {
                getPtyProcess();
            }

            /**
             * @param channel The {@link ChannelSession} through which the command has been received
             */
            @SneakyThrows
            @Override
            public void destroy(ChannelSession channel) {
                if (ptyProcess != null) {
                    ptyProcess.destroy();
                    this.callback.onExit(ptyProcess.waitFor());
                    ptyProcess = null;
                }
            }

            @SneakyThrows
            @SuppressWarnings("CommentedOutCode")
            private synchronized PtyProcess getPtyProcess() {
                if (ptyProcess == null) {
                    PtyProcessBuilder ptyProcessBuilder = new PtyProcessBuilder()
                            .setCommand(new String[]{"bash", "-il"})
                            .setRedirectErrorStream(true)
                            .setDirectory(FileUtils.getUserDirectoryPath())
                            .setEnvironment(System.getenv());
                    ptyProcess = ptyProcessBuilder.start();

                /*
                    OutputStream stdin = ptyProcess.getOutputStream(); // 0
                    InputStream stdout = ptyProcess.getInputStream(); // 1
                    InputStream stderr = ptyProcess.getErrorStream(); // 2
                */
                }
                return ptyProcess;
            }

            @SneakyThrows
            private void transferAToB(InputStream a, OutputStream b) {
                // IOUtils.copy(a, b);
                int value;
                while ((value = a.read()) != -1) {
                    b.write(value);
                    try {
                        b.flush();
                    } catch (IOException e) {
                        log.debug("could not flush stream", e);
                        break;
                    }
                }
            }
        };
    }
}
