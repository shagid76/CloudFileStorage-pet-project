package us.yarik.cloudFileStorage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import us.yarik.cloudFileStorage.model.User;
import us.yarik.cloudFileStorage.service.FileService;
import us.yarik.cloudFileStorage.service.UserService;

import java.util.Optional;
import java.util.List;

@Controller
public class DirectoryController {
    private final UserService userService;

    private final FileService fileService;

    @Autowired
    public DirectoryController(UserService userService, FileService fileService) {
        this.fileService = fileService;
        this.userService = userService;
    }

    @GetMapping("/redirect_to_directory")
    public String redirectToBalance(Authentication authentication) {
        String email = authentication.getName();
        Optional<User> user = userService.findByEmail(email);
        return user.map(value -> "redirect:/directory/" + value.getEmail()).orElse("redirect:/login");
    }

    @GetMapping("/directory/{email}")
    public String viewUserBuckets(@PathVariable("email") String email, Model model) {
        //coo
        List<String> buckets = fileService.getBucketsForUser(email);
        Optional<User> user = userService.findByEmail(email);
        model.addAttribute("buckets", buckets);
        model.addAttribute("user", user.get());
        return "directory";
    }

    @GetMapping("/create/{email}")
    public String createGet(@PathVariable("email") String email, Model model){
        Optional<User> user = userService.findByEmail(email);
        model.addAttribute("user", user.get());
        return "create";
    }

    @PostMapping("/create/{email}")
    public String createUserStorage(@PathVariable("email") String email, @ModelAttribute("bucketName") String bucketName){
        try {
            fileService.createBucket(bucketName);
            return "directory";
        }catch (Exception e){
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

}
