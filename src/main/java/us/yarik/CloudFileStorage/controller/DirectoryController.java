package us.yarik.CloudFileStorage.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import us.yarik.CloudFileStorage.model.User;
import us.yarik.CloudFileStorage.service.UserService;

import java.util.Optional;


@Controller
@AllArgsConstructor
public class DirectoryController {
    private final UserService userService;


    @GetMapping("/redirect_to_directory")
    public String redirectToDirectory(Authentication authentication) {
        String email = authentication.getName();
        Optional<User> user = userService.findByEmail(email);
        return user.map(value -> "redirect:/directory/" +
                value.getEmail().substring(0, value.getEmail().indexOf("@"))).orElse("redirect:/login");
    }

    @GetMapping("/directory/{owner}")
    public String directoryPage() {
        return "directory";
    }


    @GetMapping("/add-file/{owner}")
    public String addFilePage() {
        return "add-file";
    }

    @GetMapping("/add-file/{owner}/{folder}")
    public String addFilePageToFolder() {
        return "add-file";
    }


    @GetMapping("/folder/{owner}/{fileName}")
    public String folderPage() {
        return "folder";
    }
}
