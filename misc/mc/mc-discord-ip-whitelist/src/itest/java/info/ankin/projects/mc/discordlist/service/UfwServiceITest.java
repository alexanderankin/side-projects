package info.ankin.projects.mc.discordlist.service;

import info.ankin.projects.mc.discordlist.McDiscordIpWhitelistApplicationITest;
import info.ankin.projects.mc.discordlist.service.UfwService.Rule;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

class UfwServiceITest extends McDiscordIpWhitelistApplicationITest {
    @Autowired
    UfwService ufwService;

    @Test
    void test_statusEnableDisable() {
        if (ufwService.enabled())
            ufwService.enable(false);

        assertThat(ufwService.enabled(), Matchers.is(false));
        ufwService.enable(true);
        assertThat(ufwService.enabled(), Matchers.is(true));
        ufwService.enable(false);
    }

    @Test
    void test() {
        ufwService.enable(true);
        // ufw allow log-all from 10.0.0.9/32 to any port 52 proto any
        List<Rule> rules = ufwService.listRules();

        ufwService.addRule(new Rule()
                .setPort(new Rule.Port().setPort(25565).setProto("tcp"))
                .setAction(Rule.Action.parse("ALLOW IN"))
                .setFrom(new Rule.IpRange().setAddress("10.0.0.0").setMaskLength(32))
                .setLog(Rule.LogMode.log)
                .setComment("managed by ufw-service"));
    }
}
