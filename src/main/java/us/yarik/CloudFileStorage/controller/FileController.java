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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Objects;
import java.util.UUID;

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
    @GetMapping("/all")
    public List<File> all(){
        return fileService.findAll();
    }

    @PostMapping("/add-file/{owner}")
    public ResponseEntity<String> addFilePost(@PathVariable("owner") String owner,
                              @RequestParam("file") MultipartFile file){
        try {
            String  uuid = UUID.randomUUID().toString();
            String fileName = file.getOriginalFilename();
            String sanitizedFileName = fileName.replaceAll("[<>:\"/\\|?*]", "_");
            InputStream inputStream = file.getInputStream();
            String contentType = file.getContentType();
            Path path = Paths.get("bucket" + java.io.File.separator + owner + "-" + sanitizedFileName + "-" + uuid);


            File uploadFile = new File();
            uploadFile.setFileName(file.getOriginalFilename());
            uploadFile.setFileSize(file.getSize());
            uploadFile.setFileType(file.getContentType());
            uploadFile.setUploadDate(LocalDateTime.now());
            uploadFile.setMinioPath(path.toString());
            uploadFile.setOwner(owner);
            uploadFile.setUuid(uuid);
            fileService.uploadFile(uploadFile);
            minioService.addFile(owner, fileName, inputStream, contentType, uuid);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId){
        File file = fileService.findById(fileId);
        ByteArrayResource fileDownload = minioService.downloadFile(file.getOwner() + "-" + file.getFileName() + "-" + file.getUuid());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" +
                        URLEncoder.encode(file.getFileName().replace(" ", "_"), StandardCharsets.UTF_8))
                .contentType(MediaType.valueOf(file.getFileType()))
                .body(fileDownload.getByteArray());
    }
    @GetMapping("/download/{fileId}/{folder}")
    public ResponseEntity<byte[]> downloadFileInFolder(@PathVariable String fileId, @PathVariable String folder){
        File file = fileService.findById(fileId);
        ByteArrayResource fileDownload = minioService.downloadFile(file.getOwner() + "-" + file.getFileName() + "-" + file.getUuid() + "-" + folder);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" +
                        URLEncoder.encode(file.getFileName().replace(" ", "_"), StandardCharsets.UTF_8))
                .contentType(MediaType.valueOf(file.getFileType()))
                .body(fileDownload.getByteArray());
    }
    @DeleteMapping("/delete/{fileId}")
    public void deleteFile(@PathVariable("fileId") String fileId) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        File file = fileService.findById(fileId);
        minioService.deleteFile(file.getOwner() + "-" + file.getFileName() + "-" + file.getUuid());
        fileService.deleteFile(file);
    }

    @DeleteMapping("/delete/{fileId}/{folder}")
    public void deleteFileInFolder(@PathVariable("fileId") String fileId, @PathVariable("folder") String folder) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        File file = fileService.findById(fileId);
        minioService.deleteFile(file.getOwner() + "-" + file.getFileName() + "-" + file.getUuid() + "-" + folder);
        fileService.deleteFile(file);
    }

    @PostMapping("/copy/{fileId}")
    public void copyFile(@PathVariable("fileId") String fileId) throws IOException, ServerException,
            InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {
        File file = fileService.findById(fileId);
        String uuid = UUID.randomUUID().toString();
        String sanitizedFileName = file.getFileName().replaceAll("[<>:\"/\\|?*]", "_");
        Path path = Paths.get("bucket" + java.io.File.separator + file.getOwner() + "-" + sanitizedFileName + "-" + uuid);

        File fileCopy = new File();
        fileCopy.setFileName(file.getFileName());
        fileCopy.setFileType(file.getFileType());
        fileCopy.setFileSize(file.getFileSize());
        fileCopy.setUploadDate(LocalDateTime.now());
        fileCopy.setOwner(file.getOwner());
        fileCopy.setMinioPath(path.toString());
        fileCopy.setUuid(uuid);
        fileService.uploadFile(fileCopy);
        InputStream inputStream = minioService.getFile(file.getOwner(), file.getFileName(), file.getUuid());
        minioService.addFile(file.getOwner(), file.getFileName(), inputStream , file.getFileType(), uuid);
    }

    @PostMapping("/copy/{fileId}")
    public void copyFileInFolder(@PathVariable("fileId") String fileId, @PathVariable("folder") String folder) throws IOException, ServerException,
            InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {
        File file = fileService.findById(fileId);
        String uuid = UUID.randomUUID().toString();
        String sanitizedFileName = file.getFileName().replaceAll("[<>:\"/\\|?*]", "_");
        Path path = Paths.get("bucket" + java.io.File.separator + file.getOwner() + "-" + sanitizedFileName + "-" + uuid + "-" + folder);

        File fileCopy = new File();
        fileCopy.setFileName(file.getFileName());
        fileCopy.setFileType(file.getFileType());
        fileCopy.setFileSize(file.getFileSize());
        fileCopy.setUploadDate(LocalDateTime.now());
        fileCopy.setOwner(file.getOwner());
        fileCopy.setMinioPath(path.toString());
        fileCopy.setUuid(uuid);
        fileCopy.setFolder(folder);
        fileService.uploadFile(fileCopy);
        InputStream inputStream = minioService.getFile(file.getOwner(), file.getFileName(), file.getUuid());
        minioService.addFile(file.getOwner(), file.getFileName(), inputStream , file.getFileType(), uuid);
    }

    @PostMapping("/rename/{fileId}")
    public ResponseEntity<String> renameFile(@PathVariable("fileId") String fileId,
                                             @RequestBody Map<String, String> request) throws
            IOException {
        String newFileName = request.get("newFileName");
        String oldFileName = fileService.findById(fileId).getFileName();
        fileService.updateFileName(fileService.findById(fileId), newFileName);
        minioService.renameFile(oldFileName, newFileName,
                fileService.findById(fileId).getOwner(), fileService.findById(fileId).getUuid() );
        return ResponseEntity.ok("File rename seccessfully!");
    }

    @PostMapping("/rename/{fileId}")
    public ResponseEntity<String> renameFileInFolder(@PathVariable("fileId") String fileId,
                                                     @PathVariable("folder") String folder,
                                             @RequestBody Map<String, String> request) throws
            IOException {
        String newFileName = request.get("newFileName");
        String oldFileName = fileService.findById(fileId).getFileName();
        fileService.updateFileName(fileService.findById(fileId), newFileName);
        minioService.renameFileInFolder(oldFileName, newFileName,
                fileService.findById(fileId).getOwner(), fileService.findById(fileId).getUuid(), folder );
        return ResponseEntity.ok("File rename seccessfully!");
    }

    @GetMapping("/{folder}")
    public List<File> getFilesByFolder(@PathVariable("folder") String folder){
        return fileService.findByFolder(folder);
    }
    @PostMapping("/api/{fileId}/{folder}")
    public void putFileToFolder(@PathVariable("fileId") String fileId, @PathVariable("folder") String folder){
        File file = fileService.findById(fileId);
        String fullFileName = file.getOwner() + "-" + file.getFileName() + "-" + file.getUuid();
        minioService.uploadFileToFolder(fullFileName, folder);
        fileService.setFolder(fileId, folder);
    }

    @PostMapping("/delete-file-from-folder/{fileId}/{folder}")
    public void deleteFileFromFolder(@PathVariable("fildeId") String fileId,
                                            @PathVariable("folder") String folder){
        File file = fileService.findById(fileId);
        String fullFileName = file.getOwner() + "-" + file.getFileName() + "-" + file.getUuid();
        fileService.deleteFileFromFolder(file);
        minioService.deleteFileFromFolder(fullFileName, folder);

    }


}
