package info.ankin.projects.mc.discordlist.service;

import info.ankin.projects.mc.discordlist.McDiscordIpWhitelistApplicationITest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;

class UfwServiceITest extends McDiscordIpWhitelistApplicationITest {
    @Autowired
    UfwService ufwService;

    @Test
    void test() {
        if (ufwService.enabled())
            ufwService.enable(false);

        assertThat(ufwService.enabled(), Matchers.is(false));
        ufwService.enable(true);
        assertThat(ufwService.enabled(), Matchers.is(true));
        ufwService.enable(false);
    }
}
