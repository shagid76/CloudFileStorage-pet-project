package us.yarik.CloudFileStorage.controller;

import io.minio.errors.*;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import us.yarik.CloudFileStorage.model.File;
import us.yarik.CloudFileStorage.model.User;
import us.yarik.CloudFileStorage.service.FileService;
import us.yarik.CloudFileStorage.service.MinioService;
import us.yarik.CloudFileStorage.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//TODO delete buckets (google disc)
//TODO make folder maker(task with *)
@Controller
@AllArgsConstructor
public class DirectoryController {
    private final UserService userService;
    private final MinioService minioService;
    private final FileService fileService;


    @GetMapping("/redirect_to_directory")
    public String redirectToBalance(Authentication authentication) {
        String email = authentication.getName();
        Optional<User> user = userService.findByEmail(email);
        return user.map(value -> "redirect:/directory/" + value.getEmail().substring(0, value.getEmail().indexOf("@"))).orElse("redirect:/login");
    }


//    @GetMapping("/create/{email}")
//    public String createGet(@PathVariable("email") String email, Model model) {
//        Optional<User> user = userService.findByEmail(email);
//        if (user.isPresent()) {
//            model.addAttribute("user", user.get());
//            return "create";
//        }
//        return "redirect:/directory/" + email;
//    }

//    @PostMapping("/create/{email}")
//    public String createUserStorage(@PathVariable("email") String email, @ModelAttribute("bucketName") String bucketName,
//                                    Model model) {
//        try {
//            minioService.createBucket(bucketName, email);
//            return "redirect:/directory/" + email;
//        } catch (Exception e) {
//            Optional<User> user = userService.findByEmail(email);
//            if (user.isPresent()) {
//                model.addAttribute("user", user.get());
//                model.addAttribute("error", e.getMessage());
//                return "create";
//            }
//            return "redirect:/directory/" + email;
//        }
//    }

    @GetMapping("/directory/{owner}")
    public String bucketGet(@PathVariable("owner") String owner,
                            Model model) {
        return "directory";
    }


    @GetMapping("/add-file/{owner}")
    public String addFileGet(@PathVariable("owner") String owner, Model model) {
        return "add-file";
    }

//    @PostMapping("/add-file/{owner}")
//    public String addFilePost(@PathVariable("owner") String owner,
//                              @RequestParam("file") MultipartFile file) throws Exception {
//        try {
//            String fileName = file.getOriginalFilename();
//            String sanitizedFileName = fileName.replaceAll("[<>:\"/\\|?*]", "_");
//            InputStream inputStream = file.getInputStream();
//            String contentType = file.getContentType();
//            Path path = Paths.get("bucket" + java.io.File.separator + owner + "-" + sanitizedFileName);
//
//
//            File uploadFile = new File();
//            uploadFile.setFileName(file.getOriginalFilename());
//            uploadFile.setFileSize(file.getSize());
//            uploadFile.setFileType(file.getContentType());
//            uploadFile.setUploadDate(LocalDateTime.now());
//            uploadFile.setMinioPath(path.toString());
//            uploadFile.setOwner(owner);
//            fileService.uploadFile(uploadFile);
//            minioService.addFile(owner, fileName, inputStream, contentType);
//
//            return "redirect:/directory/" + owner;
//        } catch (Exception e) {
//
//            throw new Exception("Error occurred while adding file: " + e.getMessage());
//        }
//    }

//    @DeleteMapping("/delete-directory/{owner}")
//    public String deleteBucket(@PathVariable("owner") String owner)
//            throws Exception {
//        minioService.deleteFilesByOwner(owner);
//        fileService.deleteFilesByOwner(owner);
//        return "redirect:/directory/" + owner;
//    }

//    @PostMapping("/update/{email}/{bucketName}")
//    public String updateBucket(@PathVariable("email") String email, @PathVariable("bucketName") String oldBucketName,
//                               @ModelAttribute("newBucketName") String newBucketName) throws Exception {
//        System.out.println(email + " " + oldBucketName + " " + newBucketName);
//        minioService.createBucket(newBucketName, email);
//
//        minioService.changeBucketName(oldBucketName, newBucketName);
//        return "redirect:/directory/" + email;
//    }

    @GetMapping("/file/{email}/{fileName}")
    public String filePage(@PathVariable("email") String email,
                           @PathVariable("fileName") String fileName, Model model) {
        model.addAttribute("email", email);
        model.addAttribute("file", fileService.findByOwnerAndFileName(
                email.substring(0, email.indexOf("@")), fileName));
        return "file";
    }


    //TODO make it on frontend(pop-up window)
    @PostMapping("/update/{email}/{fileName}")
    public String renameFile(@PathVariable("email") String email,
                             @PathVariable("fileName") String fileName,
                             @RequestParam("newFileName") String newFileName) {
        fileService.updateFileName(fileService.findByOwnerAndFileName(email.substring(0, email.indexOf("@")), fileName), newFileName);
        return "redirect:/directory/" + email;
    }
}
