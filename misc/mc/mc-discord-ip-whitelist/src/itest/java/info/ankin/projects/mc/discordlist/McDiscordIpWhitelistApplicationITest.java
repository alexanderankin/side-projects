package info.ankin.projects.mc.discordlist;

import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.HostConfig;
import info.ankin.projects.mc.discordlist.service.CommandLineExecutor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@Import(McDiscordIpWhitelistApplicationITest.TestcontainersCommandLineExecutor.class)
@ContextConfiguration(initializers = McDiscordIpWhitelistApplicationITest.Init.class)
@TestPropertySource(properties = "mc.discord-list.command-line.default-executor.enabled=false")
public abstract class McDiscordIpWhitelistApplicationITest {
    @Autowired
    protected CommandLineExecutor commandLineExecutor;

    static class Init implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NonNull ConfigurableApplicationContext ctx) {
            GenericContainer<?> ufwContainer = new GenericContainer<>("ubuntu:24.04-ufw");
            ufwContainer.withCreateContainerCmdModifier(c -> c.withEntrypoint("tail", "-f", "/dev/stdout")
                    .withHostConfig(new HostConfig().withCapAdd(Capability.NET_ADMIN)));
            ufwContainer.start();

            ctx.getBeanFactory().registerSingleton("ufwContainer", ufwContainer);
        }
    }

    @TestConfiguration
    static class TestcontainersCommandLineExecutor implements CommandLineExecutor {
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        @Autowired
        GenericContainer<?> ufwContainer;

        @SneakyThrows
        private static CommandLineExecutor.CommandResult getCommandResult(GenericContainer<?> container, String[] args) {
            Container.ExecResult result = container.execInContainer(args);
            return new CommandLineExecutor.CommandResult()
                    .setExitCode(result.getExitCode())
                    .setStandardOutput(result.getStdout())
                    .setErrorOutput(result.getStderr());
        }

        @Override
        public CommandResult exec(String... args) {
            return getCommandResult(ufwContainer, args);
        }

        @SneakyThrows
        @Override
        public String fullPath(String program) {
            Container.ExecResult result = ufwContainer.execInContainer("which", program);
            assertThat(result.getExitCode(), is(0));
            return result.getStdout().strip();
        }
    }
}
