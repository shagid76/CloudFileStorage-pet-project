package us.yarik.CloudFileStorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import us.yarik.CloudFileStorage.model.File;
import us.yarik.CloudFileStorage.service.FileService;
import us.yarik.CloudFileStorage.service.MinioService;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;
    private final MinioService minioService;

    @GetMapping("/files/{owner}")
    public List<File> getAllFiles(@PathVariable("owner") String owner) {
        return fileService.findByOwner(owner);
    }

    @DeleteMapping("/delete-directory/{owner}")
    public void deleteDirectory(@PathVariable("owner") String owner) throws Exception {
        minioService.deleteFilesByOwner(owner);
        fileService.deleteFilesByOwner(owner);
    }
    @PostMapping("/updated-file-name")
    public void updatedFileName(@RequestBody File file){
        fileService.updateFileName(file, file.getFileName() );
    }

    @GetMapping("/all")
    public List<File> all(){
        return fileService.findAll();
    }

    @PostMapping("/add-file/{owner}")
    public ResponseEntity<String> addFilePost(@PathVariable("owner") String owner,
                              @RequestParam("file") MultipartFile file) throws Exception {
        try {
            String fileName = file.getOriginalFilename();
            String sanitizedFileName = fileName.replaceAll("[<>:\"/\\|?*]", "_");
            InputStream inputStream = file.getInputStream();
            String contentType = file.getContentType();
            Path path = Paths.get("bucket" + java.io.File.separator + owner + "-" + sanitizedFileName);


            File uploadFile = new File();
            uploadFile.setFileName(file.getOriginalFilename());
            uploadFile.setFileSize(file.getSize());
            uploadFile.setFileType(file.getContentType());
            uploadFile.setUploadDate(LocalDateTime.now());
            uploadFile.setMinioPath(path.toString());
            uploadFile.setOwner(owner);
            fileService.uploadFile(uploadFile);
            minioService.addFile(owner, fileName, inputStream, contentType);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
