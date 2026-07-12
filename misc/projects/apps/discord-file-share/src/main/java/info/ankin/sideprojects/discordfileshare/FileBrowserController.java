package info.ankin.sideprojects.discordfileshare;

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FileBrowserController {

    private final FileBrowserService fileBrowserService;

    public FileBrowserController(FileBrowserService fileBrowserService) {
        this.fileBrowserService = fileBrowserService;
    }

    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public String index(
            @RequestParam(name = "path", required = false) String path,
            @AuthenticationPrincipal OAuth2User user,
            Model model) throws IOException {
        model.addAttribute("listing", fileBrowserService.list(path));
        model.addAttribute("username", username(user));
        return "index";
    }

    @GetMapping("/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(@RequestParam("path") String path) throws IOException {
        Resource resource = fileBrowserService.download(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileBrowserService.filename(path) + "\"")
                .body(resource);
    }

    private static String username(OAuth2User user) {
        if (user == null) {
            return null;
        }
        String globalName = user.getAttribute("global_name");
        if (globalName != null) {
            return globalName;
        }
        String username = user.getAttribute("username");
        return username != null ? username : user.getName();
    }
}
