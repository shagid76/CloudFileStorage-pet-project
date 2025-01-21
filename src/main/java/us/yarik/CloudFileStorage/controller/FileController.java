package us.yarik.CloudFileStorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import us.yarik.CloudFileStorage.model.File;
import us.yarik.CloudFileStorage.service.FileService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @GetMapping("/files")
    public List<File> getAllFiles() {

        return fileService.findAll();
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
