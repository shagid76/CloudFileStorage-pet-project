package us.yarik.CloudFileStorage.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
@AllArgsConstructor
public class DirectoryController {

    @GetMapping("/directory")
    public String directoryPage(Authentication authentication, Model model) {
        String email = authentication.getName();
        model.addAttribute("email", email.substring(0, email.indexOf("@")));
        return "directory";
    }

    @GetMapping("/add-file")
    public String addFilePage(Authentication authentication, Model model) {
        String email = authentication.getName();
        model.addAttribute("email", email.substring(0, email.indexOf("@")));
        return "add-file";
    }

    @GetMapping("/add-file/{parentId}")
    public String addFilePageToFolder(@PathVariable("parentId") String parentId, Authentication authentication, Model model) {
        String email = authentication.getName();
        model.addAttribute("email", email.substring(0, email.indexOf("@")));
        model.addAttribute("parentId", parentId);
        return "add-file";
    }

    @GetMapping("/folder/{parentId}")
    public String folderPage(@PathVariable("parentId") String parentId, Authentication authentication, Model model) {
        String email = authentication.getName();
        model.addAttribute("email", email.substring(0, email.indexOf("@")));
        model.addAttribute("parentId", parentId);
        return "folder";
    }
}