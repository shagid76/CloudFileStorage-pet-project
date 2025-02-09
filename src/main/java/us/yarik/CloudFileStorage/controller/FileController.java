package us.yarik.CloudFileStorage.controller;

import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import us.yarik.CloudFileStorage.model.File;
import us.yarik.CloudFileStorage.service.FileService;
import us.yarik.CloudFileStorage.service.MinioService;

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
import java.util.Map;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;
    private final MinioService minioService;


    @GetMapping("/files/{owner}")
    public List<File> getAllFiles(@PathVariable("owner") String owner) {
        return fileService.findByOwnerAndParentIdIsNull(owner);
    }


    @DeleteMapping("/delete-directory/{owner}")
    public void deleteDirectory(@PathVariable("owner") String owner) throws Exception {
        List<File> files = fileService.findByOwner(owner);
        for (File file : files) {
            if(!file.isFolder()){
            minioService.deleteFile(file.getUuid());
        }}
        fileService.deleteFilesByOwner(owner);
    }

    @GetMapping("/all")
    public List<File> all() {
        return fileService.findAll();
    }

    @PostMapping("/file/{owner}")
    public ResponseEntity<String> addFilePost(@PathVariable("owner") String owner,
                                              @RequestParam("file") MultipartFile file) {
        try {
            String uuid = UUID.randomUUID().toString();
            InputStream inputStream = file.getInputStream();
            String contentType = file.getContentType();


            File uploadFile = new File();
            uploadFile.setFileName(file.getOriginalFilename());
            uploadFile.setFileSize(file.getSize());
            uploadFile.setFileType(file.getContentType());
            uploadFile.setUploadDate(LocalDateTime.now());
            uploadFile.setOwner(owner);
            uploadFile.setUuid(uuid);
            fileService.uploadFile(uploadFile);
            minioService.addFile(inputStream, contentType, uuid);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) {
        File file = fileService.findById(fileId);
        ByteArrayResource fileDownload = minioService.downloadFile(file.getUuid());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" +
                        URLEncoder.encode(file.getFileName().replace(" ", "_"),
                                StandardCharsets.UTF_8))
                .contentType(MediaType.valueOf(file.getFileType()))
                .body(fileDownload.getByteArray());
    }

    @DeleteMapping("/delete/{fileId}")
    public void deleteFile(@PathVariable("fileId") String fileId) throws Exception{
        File file = fileService.findById(fileId);
        minioService.deleteFile(file.getUuid());
        fileService.deleteFile(file);
    }


    @PostMapping("/copy/{fileId}")
    public void copyFile(@PathVariable("fileId") String fileId) throws Exception{
        File file = fileService.findById(fileId);
        String uuid = UUID.randomUUID().toString();

        File fileCopy = new File();
        fileCopy.setFileName(file.getFileName());
        fileCopy.setFileType(file.getFileType());
        fileCopy.setFileSize(file.getFileSize());
        fileCopy.setUploadDate(LocalDateTime.now());
        fileCopy.setOwner(file.getOwner());
        fileCopy.setUuid(uuid);
        fileService.uploadFile(fileCopy);
        InputStream inputStream = minioService.getFile(file.getUuid());
        minioService.addFile(inputStream, file.getFileType(), uuid);
    }

    @PostMapping("/rename/{fileId}")
    public ResponseEntity<String> renameFile(@PathVariable("fileId") String fileId,
                                             @RequestBody Map<String, String> request) throws  Exception{
        String newFileName = request.get("newFileName");
        fileService.updateFileName(fileService.findById(fileId), newFileName);
        return ResponseEntity.ok("File rename successfully!");
    }

    @PostMapping("/rename-folder/{fileId}")
    public ResponseEntity<String> renameFolder(@PathVariable("fileId") String fileId,
                                               @RequestBody Map<String, String> request) throws Exception {
        String newFileName = request.get("newFolderName");
        File folder = fileService.findById(fileId);
        List<File> files = fileService.findByParentIdAndOwner(folder.getFileName(), folder.getOwner());
        for (File file : files) {
            if (!files.isEmpty()) {
                fileService.setParentId(file, newFileName);
            }
        }
        fileService.updateFileName(folder, newFileName);
        return ResponseEntity.ok("Folder rename successfully!");
    }

    @PostMapping("/put-file-to-folder/{fileId}")
    public ResponseEntity<String> putFileToFolder(@PathVariable("fileId") String fileId,
                                                  @RequestBody Map<String, String> request) throws  Exception{
        String folderId = request.get("parentID");
        String parentId = fileService.findById(folderId).getFileName();
        fileService.putFileToFolder(parentId, fileId);
        return ResponseEntity.ok("File putted successfully!");
    }

    @GetMapping("/folders/{owner}")
    public ResponseEntity<List<File>> allFoldersByOwner(@PathVariable("owner") String owner) {
        try {
            List<File> folders = fileService.findByOwnerAndIsFolderIsTrue(owner);
            return ResponseEntity.ok(folders);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/put-folder-to-folder/{fileId}")
    public ResponseEntity<String> putFolderToFolder(@PathVariable("fileId") String fileId,
                                                    @RequestBody Map<String, String> request) throws Exception {
        String folderId = request.get("parentID");
        String parentId = fileService.findById(folderId).getFileName();
        File folder = fileService.findById(fileId);
        fileService.setParentId(folder, parentId);
        return ResponseEntity.ok("File putted successfully!");
    }

}
