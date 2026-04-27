package side.ufw.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UfwServiceITest extends UfwServerApplicationITest {
    @Autowired
    UfwService ufwService;

    @Test
    void test() {
        var ufwStatusVerbose = ufwService.statusVerbose();
        System.out.println(ufwStatusVerbose);
    }
}
