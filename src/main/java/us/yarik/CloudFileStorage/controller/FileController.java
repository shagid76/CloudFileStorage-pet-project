package us.yarik.CloudFileStorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import us.yarik.CloudFileStorage.model.File;
import us.yarik.CloudFileStorage.service.FileService;
import us.yarik.CloudFileStorage.service.MinioService;

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

    @PostMapping("/addFile")
    public File uploadFile(@RequestBody File file) {
        return fileService.uploadFile(file);
    }


    @GetMapping("/find-by-name")
    public ResponseEntity<List<File>> findFileByOwner(@RequestParam String owner) {
        List<File> fileList = fileService.findByOwner(owner);
        if (fileList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(fileList);
    }
}
