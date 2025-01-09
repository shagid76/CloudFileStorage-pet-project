package us.yarik.CloudFileStorage.controller;

import io.minio.errors.*;
import io.minio.messages.Item;
import okhttp3.MultipartBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import us.yarik.CloudFileStorage.model.User;
import us.yarik.CloudFileStorage.service.FileService;
import us.yarik.CloudFileStorage.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
        List<String> buckets = fileService.getBucketsForUser(email);
        Optional<User> user = userService.findByEmail(email);
        if(user.isPresent()) {
            model.addAttribute("buckets", buckets);
            model.addAttribute("user", user.get());
            return "directory";
        }
        return "redirect:/login";
    }

    @GetMapping("/create/{email}")
    public String createGet(@PathVariable("email") String email, Model model){
        Optional<User> user = userService.findByEmail(email);
        if(user.isPresent()) {
            model.addAttribute("user", user.get());
            return "create";
        }
        return "redirect:/directory/" + email;
    }

    @PostMapping("/create/{email}")
    public String createUserStorage(@PathVariable("email") String email, @ModelAttribute("bucketName") String bucketName,
                                    Model model){
        try {
            fileService.createBucket(bucketName, email);
            return "redirect:/directory/" + email;
        }catch (Exception e){
            Optional<User> user = userService.findByEmail(email);
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                model.addAttribute("error", e.getMessage());
                return "create";
            }
            return "redirect:/directory/" + email;
        }
    }

    @GetMapping("/bucket/{email}/{bucketName}")
    public String bucketGet(@PathVariable("email") String email,@PathVariable("bucketName") String bucketName,
                            Model model) throws  Exception{
        List<String> objects = fileService.allObjectsOnBucket(bucketName);
        Optional<User> user = userService.findByEmail(email);
        if (user.isPresent()) {
            model.addAttribute("objects", objects);
            model.addAttribute("user", user.get());
            model.addAttribute("bucketName", bucketName);
            return "bucket";
        }
        return "redirect:/directory/" + email;

    }


    @GetMapping("/add-file/{email}/{bucketName}")
    public String addFileGet(@PathVariable("email") String email, @PathVariable("bucketName") String bucketName,
                             Model model){
        model.addAttribute("user", userService.findByEmail(email).get());
        model.addAttribute("bucketName", bucketName);
        return "add-file";
    }
    @PostMapping("/add-file/{email}/{bucketName}")
    public String addFilePost(@PathVariable("email") String email, @PathVariable("bucketName") String bucketName,
                              @RequestParam("file") MultipartFile file , Model model) throws Exception {
       try {
           String fileName = file.getOriginalFilename();
           InputStream inputStream = file.getInputStream();
           String contentType = file.getContentType();
           fileService.addFile(bucketName, fileName, inputStream, contentType);
           return "redirect:/bucket/" + email +"/" + bucketName;
       }catch (Exception e){
           throw new Exception();
       }
    }

    @DeleteMapping("/delete/{email}/{bucketName}")
    public String deleteBucket(@PathVariable("email") String email, @PathVariable("bucketName") String bucketName)
            throws Exception {
        fileService.deleteBucket(bucketName);
        return "redirect:/directory/" + email;
    }



}
