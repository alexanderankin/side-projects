package side.ufw.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/ufw")
public class UfwController {
    private final UfwService ufwService;

    public UfwController(UfwService ufwService) {
        this.ufwService = ufwService;
    }

    @GetMapping(path = "/status/verbose")
    UfwStatus.UfwStatusVerbose statusVerbose() {
        return ufwService.statusVerbose();
    }
}
