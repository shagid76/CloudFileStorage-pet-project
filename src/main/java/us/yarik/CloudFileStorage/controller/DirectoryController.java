package us.yarik.CloudFileStorage.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import us.yarik.CloudFileStorage.model.User;
import us.yarik.CloudFileStorage.service.UserService;

import java.util.Optional;


@Controller
@AllArgsConstructor
public class DirectoryController {
    private final UserService userService;


    @GetMapping("/redirect_to_directory")
    public String redirectToBalance(Authentication authentication) {
        String email = authentication.getName();
        Optional<User> user = userService.findByEmail(email);
        return user.map(value -> "redirect:/directory/" + value.getEmail().substring(0, value.getEmail().indexOf("@"))).orElse("redirect:/login");
    }

    @GetMapping("/directory/{owner}")
    public String bucketGet(@PathVariable("owner") String owner,
                            Model model) {
        return "directory";
    }


    @GetMapping("/add-file/{owner}")
    public String addFileGet(@PathVariable("owner") String owner, Model model) {
        return "add-file";
    }


    @GetMapping("/folder")
    public String folderPage() {
        return "folder";
    }
}
