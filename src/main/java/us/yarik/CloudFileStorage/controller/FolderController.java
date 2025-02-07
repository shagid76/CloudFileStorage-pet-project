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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import us.yarik.CloudFileStorage.dto.FolderDTO;
import us.yarik.CloudFileStorage.model.File;
import us.yarik.CloudFileStorage.service.FileService;
import us.yarik.CloudFileStorage.service.MinioService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


//TODO rename methods
@RestController
@RequiredArgsConstructor
public class FolderController {
    private final FileService fileService;
    private final MinioService minioService;

    @PostMapping("/create-folder")
    public ResponseEntity<File> createFolder(@RequestBody FolderDTO folderDTO) {
        File folder = File.builder()
                .fileName(folderDTO.getFolderName())
                .isFolder(true)
                .fileSize(0L)
                .uploadDate(LocalDateTime.now())
                .parentId(folderDTO.getParentId())
                .owner(folderDTO.getOwner())
                .build();

        fileService.uploadFile(folder);
        return ResponseEntity.ok(folder);
    }

    @GetMapping("/files/folder/{parentId}/{owner}")
    public List<File> allFilesFromFolder(@PathVariable("parentId") String parentId,
                                         @PathVariable("owner") String owner) {
        return fileService.findByParentIdAndOwner(parentId, owner);
    }


    @PostMapping("/add-file/{owner}/{parentId}")
    public ResponseEntity<String> addFileToFolder(@PathVariable("owner") String owner,
                                                  @PathVariable("parentId") String parentId,
                                                  @RequestParam("file") MultipartFile file) {
        try {
            String uuid = UUID.randomUUID().toString();
            String fileName = file.getOriginalFilename();
            String sanitizedFileName = fileName.replaceAll("[<>:\"/\\|?*]", "_");
            InputStream inputStream = file.getInputStream();
            String contentType = file.getContentType();
            Path path = Paths.get("bucket" + java.io.File.separator + owner + "-" +
                    sanitizedFileName + "-" + uuid + "-" + parentId);

            File newFile = File.builder()
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .uploadDate(LocalDateTime.now())
                    .owner(owner)
                    .minioPath(path.toString())
                    .uuid(uuid)
                    .parentId(parentId)
                    .build();

            fileService.uploadFile(newFile);
            minioService.addFile(inputStream, contentType, uuid);

            return ResponseEntity.ok("File uploaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error " + e.getMessage());
        }
    }

    @GetMapping("/check-folder-name/{folderName}")
    public ResponseEntity<?> checkFolderName(@PathVariable("folderName") String folderName,
                                             @RequestParam String owner) {
        File file = fileService.findByOwnerAndFileName(owner, folderName);
        boolean isNameUnique = (file == null);
        return ResponseEntity.ok().body(Map.of("isNameUnique", isNameUnique));
    }

    @PostMapping("/copy-folder/{fileId}")
    public ResponseEntity<String> copyFolder(@PathVariable("fileId") String fileId) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, XmlParserException, InvalidResponseException, InternalException {
        File folder = fileService.findById(fileId);
        List<File> filesOnFolder = fileService.findByOwnerAndFileNameList(folder.getOwner(),
                folder.getFileName());

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH:mm:ss");
        String formattedDate = myFormatObj.format(now);

        FolderDTO folderDTO = new FolderDTO(folder.getFileName() + "-" + formattedDate,
                null, folder.getOwner());
        createFolder(folderDTO);

        for (File files : filesOnFolder) {
            if (!files.isFolder()) {
                String uuid = UUID.randomUUID().toString();
                String sanitizedFileName = files.getFileName().replaceAll("[<>:\"/\\|?*]", "_");
                Path path = Paths.get("bucket" + java.io.File.separator + files.getOwner() + "-" +
                        sanitizedFileName + "-" + uuid);

                File fileCopy = new File();
                fileCopy.setFileName(files.getFileName());
                fileCopy.setFileType(files.getFileType());
                fileCopy.setFileSize(files.getFileSize());
                fileCopy.setUploadDate(LocalDateTime.now());
                fileCopy.setOwner(files.getOwner());
                fileCopy.setMinioPath(path.toString());
                fileCopy.setUuid(uuid);
                fileCopy.setParentId(folderDTO.getFolderName());
                fileService.uploadFile(fileCopy);
                InputStream inputStream = minioService.getFile(files.getUuid());
                minioService.addFile(inputStream, files.getFileType(), uuid);
            } else {
                copyFilesOnFolder(files, formattedDate, folderDTO);
            }
        }
        return ResponseEntity.ok("Folder copied!");
    }

    public void copyFilesOnFolder(File folder, String currentTime, FolderDTO currentFolder)
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException, XmlParserException, InvalidResponseException,
            InternalException {
        FolderDTO folderDTO = new FolderDTO(folder.getFileName() + "-" + currentTime,
                currentFolder.getFolderName(), folder.getOwner());
        createFolder(folderDTO);
        List<File> files = fileService.findByParentIdAndOwner(folder.getFileName(), folder.getOwner());
        System.out.println(files.toString());
        for (File file : files) {
            if (!file.isFolder()) {
                String uuid = UUID.randomUUID().toString();
                String sanitizedFileName = file.getFileName().replaceAll("[<>:\"/\\|?*]", "_");
                Path path = Paths.get("bucket" + java.io.File.separator + file.getOwner() + "-" +
                        sanitizedFileName + "-" + uuid);

                File fileCopy = new File();
                fileCopy.setFileName(file.getFileName());
                fileCopy.setFileType(file.getFileType());
                fileCopy.setFileSize(file.getFileSize());
                fileCopy.setUploadDate(LocalDateTime.now());
                fileCopy.setOwner(file.getOwner());
                fileCopy.setMinioPath(path.toString());
                fileCopy.setUuid(uuid);
                fileCopy.setParentId(folderDTO.getFolderName());
                fileService.uploadFile(fileCopy);
                InputStream inputStream = minioService.getFile(file.getUuid());
                minioService.addFile(inputStream, file.getFileType(), uuid);
            } else {
                copyFilesOnFolder(file, currentTime, folderDTO);
            }
        }
    }

    @PostMapping("/copy-file-on-folder/{fileId}")
    public void copyFile(@PathVariable("fileId") String fileId) throws IOException, ServerException,
            InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {
        File file = fileService.findById(fileId);
        String uuid = UUID.randomUUID().toString();
        String sanitizedFileName = file.getFileName().replaceAll("[<>:\"/\\|?*]", "_");
        Path path = Paths.get("bucket" + java.io.File.separator + file.getOwner() + "-" +
                sanitizedFileName + "-" + uuid);

        File fileCopy = new File();
        fileCopy.setFileName(file.getFileName());
        fileCopy.setFileType(file.getFileType());
        fileCopy.setFileSize(file.getFileSize());
        fileCopy.setUploadDate(LocalDateTime.now());
        fileCopy.setOwner(file.getOwner());
        fileCopy.setMinioPath(path.toString());
        fileCopy.setUuid(uuid);
        fileCopy.setParentId(file.getParentId());
        fileService.uploadFile(fileCopy);
        InputStream inputStream = minioService.getFile(file.getUuid());
        minioService.addFile(inputStream, file.getFileType(), uuid);
    }

    @PostMapping("/rename-on-folder/{fileId}")
    public ResponseEntity<String> renameFile(@PathVariable("fileId") String fileId,
                                             @RequestBody Map<String, String> request){
        String newFileName = request.get("newFileName");
        fileService.updateFileName(fileService.findById(fileId), newFileName);
        return ResponseEntity.ok("File rename successfully!");
    }

    @GetMapping("/download-from-folder/{fileId}")
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

    @DeleteMapping("/delete-folder/{owner}/{parentId}")
    public ResponseEntity<String> deleteFolder(@PathVariable("owner") String owner,
                                               @PathVariable("parentId") String parentId) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        File folder = fileService.findByOwnerAndFileName(owner, parentId);
        List<File> filesONFolder = fileService.findByParentIdAndOwner(folder.getFileName(), owner);
        for (File fileDelete : filesONFolder) {
            if (!fileDelete.isFolder()) {
                fileService.deleteFile(fileDelete);
                minioService.deleteFile( fileDelete.getUuid());
            } else {
                deleteFilesFromFolder(fileDelete);
            }
        }
        fileService.deleteFile(folder);
        return ResponseEntity.ok("Deleting successfully!");
    }

    @DeleteMapping("/delete-folder-by-id/{owner}/{fileId}")
    public ResponseEntity<String> deleteFolderByFileId(@PathVariable("owner") String owner,
                                                       @PathVariable("fileId") String fileId) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        File folder = fileService.findById(fileId);
        List<File> filesONFolder = fileService.findByParentIdAndOwner(folder.getFileName(), owner);
        for (File fileDelete : filesONFolder) {
            if (!fileDelete.isFolder()) {
                fileService.deleteFile(fileDelete);
                minioService.deleteFile(fileDelete.getUuid());
            } else {
                deleteFilesFromFolder(fileDelete);
            }
        }
        fileService.deleteFile(folder);
        return ResponseEntity.ok("Deleting successfully!");
    }

    public void deleteFilesFromFolder(File folder) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {
        List<File> files = fileService.findByParentIdAndOwner(folder.getFileName(), folder.getOwner());
        for (File file : files) {
            if (!file.isFolder()) {
                fileService.deleteFile(file);
                minioService.deleteFile(file.getUuid());
            } else {
                deleteFilesFromFolder(file);
            }
        }
        fileService.deleteFile(folder);
    }

    @GetMapping("/download-folder/{fileId}")
    public ResponseEntity<StreamingResponseBody> downloadFolder(@PathVariable String fileId) throws IOException {
        File folder = fileService.findById(fileId);
        List<File> files = fileService.findByParentIdAndOwner(folder.getFileName(), folder.getOwner());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (File file : files) {
            byte[] fileBytes = minioService.downloadFile(file.getUuid()).getByteArray();
            ZipEntry zipEntry = new ZipEntry(file.getFileName().replace(" ", "_"));
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(fileBytes);
            zipOutputStream.closeEntry();
        }

        zipOutputStream.close();

        StreamingResponseBody responseBody = outputStream -> {
            outputStream.write(byteArrayOutputStream.toByteArray());
            outputStream.flush();
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + URLEncoder.encode(folder.getFileName() + ".zip", StandardCharsets.UTF_8))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    @PostMapping("/put-file-to-another-folder/{fileId}")
    public ResponseEntity<String> putFileToFolder(@PathVariable("fileId") String fileId,
                                                  @RequestBody Map<String, String> request) {
        String folderId = request.get("parentID");
        String parentId = fileService.findById(folderId).getFileName();
        fileService.putFileToFolder(parentId, fileId);
        return ResponseEntity.ok("File putted successfully!");
    }

}
