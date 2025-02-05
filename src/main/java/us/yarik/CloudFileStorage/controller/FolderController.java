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

//TODO delete all folders (recursion)
//TODO file finder(directory + folder)

//TODO spring session

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
    public List<File> allFilesFromFolder(@PathVariable("parentId") String parentId, @PathVariable("owner") String owner) {
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
            Path path = Paths.get("bucket" + java.io.File.separator + owner + "-" + sanitizedFileName + "-" + uuid + "-" + parentId);

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
            minioService.addFile(owner, fileName, inputStream, contentType, uuid, parentId);

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
        System.out.println(isNameUnique);
        return ResponseEntity.ok().body(Map.of("isNameUnique", isNameUnique));
    }

    @PostMapping("/copy-folder/{fileId}")
    public ResponseEntity<String> copyFolder(@PathVariable("fileId") String fileId) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, XmlParserException, InvalidResponseException, InternalException {
        File folder = fileService.findById(fileId);
        List<File> filesOnFolder = fileService.findByOwnerAndFileNameList(folder.getOwner(), folder.getFileName());
        System.out.println(filesOnFolder.toString());

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH:mm:ss");
        String formattedDate = myFormatObj.format(now);

        FolderDTO folderDTO = new FolderDTO(folder.getFileName() + "-" + formattedDate, null, folder.getOwner());
        createFolder(folderDTO);

        for (File files : filesOnFolder) {
                if (!files.isFolder()) {
                    //copyFile
                    String uuid = UUID.randomUUID().toString();
                    String sanitizedFileName = files.getFileName().replaceAll("[<>:\"/\\|?*]", "_");
                    Path path = Paths.get("bucket" + java.io.File.separator + files.getOwner() + "-" + sanitizedFileName + "-" + uuid);

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
                    InputStream inputStream = minioService.getFileFromFolder(files.getOwner(), files.getFileName(), files.getUuid(), files.getParentId());
                    minioService.addFile(files.getOwner(), fileCopy.getFileName(), inputStream, files.getFileType(), uuid, fileCopy.getParentId());
                }else{
                    //copyFolder
                    //FolderDTO folderDTO1 = new FolderDTO(files.getFileName() + " " + formattedDate, folderDTO.getFolderName(), files.getOwner());
                    //createFolder(folderDTO1);
                    copyFilesOnFolder(files, formattedDate, folderDTO);
                }
            }
        return ResponseEntity.ok("Folder copied!");
    }

    public void copyFilesOnFolder(File folder, String currentTime, FolderDTO currentFolder) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException, InvalidResponseException, InternalException {
        FolderDTO folderDTO = new FolderDTO(folder.getFileName() + "-" + currentTime, currentFolder.getFolderName() , folder.getOwner());
        createFolder(folderDTO);
        List<File> files = fileService.findByParentIdAndOwner(folder.getFileName(), folder.getOwner());
        System.out.println(files.toString());
        for (File file: files){
            if(!file.isFolder()){
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
                fileCopy.setParentId(folderDTO.getFolderName());
                fileService.uploadFile(fileCopy);
                InputStream inputStream = minioService.getFileFromFolder(file.getOwner(), file.getFileName(), file.getUuid(), file.getParentId());
                minioService.addFile(file.getOwner(), fileCopy.getFileName(), inputStream, file.getFileType(), uuid, fileCopy.getParentId());
            }
            else {
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
        Path path = Paths.get("bucket" + java.io.File.separator + file.getOwner() + "-" + sanitizedFileName + "-" + uuid);

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
        InputStream inputStream = minioService.getFileFromFolder(file.getOwner(), file.getFileName(), file.getUuid(), file.getParentId());
        minioService.addFile(file.getOwner(), file.getFileName(), inputStream, file.getFileType(), uuid, fileCopy.getParentId());
    }

    @PostMapping("/rename-on-folder/{fileId}")
    public ResponseEntity<String> renameFile(@PathVariable("fileId") String fileId,
                                             @RequestBody Map<String, String> request) throws
            IOException {
        String newFileName = request.get("newFileName");
        String oldFileName = fileService.findById(fileId).getFileName();
        fileService.updateFileName(fileService.findById(fileId), newFileName);
        minioService.renameFileInFolder(oldFileName, newFileName,
                fileService.findById(fileId).getOwner(), fileService.findById(fileId).getUuid(), fileService.findById(fileId).getParentId());
        return ResponseEntity.ok("File rename successfully!");
    }

    @GetMapping("/download-from-folder/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) {
        File file = fileService.findById(fileId);
        ByteArrayResource fileDownload = minioService.downloadFile(file.getOwner() + "-" + file.getFileName() + "-" + file.getUuid() + "-" + file.getParentId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" +
                        URLEncoder.encode(file.getFileName().replace(" ", "_"), StandardCharsets.UTF_8))
                .contentType(MediaType.valueOf(file.getFileType()))
                .body(fileDownload.getByteArray());
    }

    @DeleteMapping("/delete-folder/{owner}/{parentId}")
    public ResponseEntity<String> deleteFolder(@PathVariable("owner") String owner,
                                               @PathVariable("parentId") String parentId) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        File folderName = fileService.findByOwnerAndFileName(owner, parentId);
        List<File> filesONFolder = fileService.findByParentIdAndOwner(parentId, owner);
        for (File fileDelete : filesONFolder) {
            if (!filesONFolder.isEmpty()) {
                fileService.deleteFile(fileDelete);
                minioService.deleteFile(fileDelete.getOwner() + "-" + fileDelete.getFileName() + "-" + fileDelete.getUuid() + "-" + fileDelete.getParentId());
            }
        }
        fileService.deleteFile(folderName);
        return ResponseEntity.ok("Deleting successfully!");
    }

    @DeleteMapping("/delete-folder-by-id/{owner}/{fileId}")
    public ResponseEntity<String> deleteFolderByFileId(@PathVariable("owner") String owner,
                                                       @PathVariable("fileId") String fileId) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        File folderName = fileService.findById(fileId);
        List<File> filesONFolder = fileService.findByParentIdAndOwner(folderName.getFileName(), owner);
        for (File fileDelete : filesONFolder) {
            if (!filesONFolder.isEmpty()) {
                fileService.deleteFile(fileDelete);
                minioService.deleteFile(fileDelete.getOwner() + "-" + fileDelete.getFileName() + "-" + fileDelete.getUuid() + "-" + fileDelete.getParentId());
            }
        }
        fileService.deleteFile(folderName);
        return ResponseEntity.ok("Deleting successfully!");
    }

    @GetMapping("/download-folder/{fileId}")
    public ResponseEntity<StreamingResponseBody> downloadFolder(@PathVariable String fileId) throws IOException {
        File folder = fileService.findById(fileId);
        List<File> files = fileService.findByParentIdAndOwner(folder.getFileName(), folder.getOwner());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (File file : files) {
            byte[] fileBytes = minioService.downloadFile(file.getOwner() + "-" + file.getFileName() + "-" + file.getUuid() + "-" + file.getParentId()).getByteArray();
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
    public ResponseEntity<String> putFileToFolder(@PathVariable("fileId") String fileId, @RequestBody Map<String, String> request) {
        String folderId = request.get("parentID");
        String parentId = fileService.findById(folderId).getFileName();
        File file = fileService.findById(fileId);
        fileService.putFileToFolder(parentId, fileId);
        minioService.uploadFileToFolder(file.getOwner(),file.getFileName(), file.getUuid(), file.getParentId(),
                parentId);
        return ResponseEntity.ok("File putted successfully!");
    }

}
