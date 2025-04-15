package info.ankin.projects.mc.discordlist.service;

import info.ankin.projects.mc.discordlist.McDiscordIpWhitelistApplicationITest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandLineExecutorITest extends McDiscordIpWhitelistApplicationITest {
    @Test
    void test() {
        CommandLineExecutor.CommandResult whoami = commandLineExecutor.exec("whoami");
        assertEquals("root", whoami.getStandardOutput().strip());
    }
}
