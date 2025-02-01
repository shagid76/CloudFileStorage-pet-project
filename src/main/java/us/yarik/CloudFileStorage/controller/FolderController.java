package us.yarik.CloudFileStorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import us.yarik.CloudFileStorage.dto.FolderDTO;
import us.yarik.CloudFileStorage.model.File;
import us.yarik.CloudFileStorage.service.FileService;
import us.yarik.CloudFileStorage.service.MinioService;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

//TODO folder on folder
//TODO delete folder
//TODO rename file on folder
//TODO copy file on folder
//TODO put to folder on folder
//TODO download on folder
//TODO create folder on folder
//TODO delete file on folder(maybe already created)
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

    @GetMapping("/files/folder/{parentId}")
    public List<File> allFilesFromFolder(@PathVariable("parentId") String parentId) {
        return fileService.findByParentId(parentId);
    }

//    @GetMapping("/download/{fileId}/{folder}")
//    public ResponseEntity<byte[]> downloadFileInFolder(@PathVariable String fileId, @PathVariable String folder){
//        File file = fileService.findById(fileId);
//        ByteArrayResource fileDownload = minioService.downloadFile(file.getOwner() + "-" + file.getFileName() + "-" + file.getUuid() + "-" + folder);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" +
//                        URLEncoder.encode(file.getFileName().replace(" ", "_"), StandardCharsets.UTF_8))
//                .contentType(MediaType.valueOf(file.getFileType()))
//                .body(fileDownload.getByteArray());
//    }
//
//    @DeleteMapping("/delete/{fileId}/{folder}")
//    public void deleteFileInFolder(@PathVariable("fileId") String fileId, @PathVariable("folder") String folder) throws ServerException,
//            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
//            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
//        File file = fileService.findById(fileId);
//        minioService.deleteFile(file.getOwner() + "-" + file.getFileName() + "-" + file.getUuid() + "-" + folder);
//        fileService.deleteFile(file);
//    }
//
//    @PostMapping("/copy/{fileId}")
//    public void copyFileInFolder(@PathVariable("fileId") String fileId, @PathVariable("folder") String folder) throws IOException, ServerException,
//            InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException,
//            InvalidResponseException, XmlParserException, InternalException {
//        File file = fileService.findById(fileId);
//        String uuid = UUID.randomUUID().toString();
//        String sanitizedFileName = file.getFileName().replaceAll("[<>:\"/\\|?*]", "_");
//        Path path = Paths.get("bucket" + java.io.File.separator + file.getOwner() + "-" + sanitizedFileName + "-" + uuid + "-" + folder);
//
//        File fileCopy = new File();
//        fileCopy.setFileName(file.getFileName());
//        fileCopy.setFileType(file.getFileType());
//        fileCopy.setFileSize(file.getFileSize());
//        fileCopy.setUploadDate(LocalDateTime.now());
//        fileCopy.setOwner(file.getOwner());
//        fileCopy.setMinioPath(path.toString());
//        fileCopy.setUuid(uuid);
//        fileService.uploadFile(fileCopy);
//        InputStream inputStream = minioService.getFile(file.getOwner(), file.getFileName(), file.getUuid());
//        minioService.addFile(file.getOwner(), file.getFileName(), inputStream , file.getFileType(), uuid);
//    }
//
//    @PostMapping("/rename/{fileId}")
//    public ResponseEntity<String> renameFileInFolder(@PathVariable("fileId") String fileId,
//                                                     @PathVariable("folder") String folder,
//                                                     @RequestBody Map<String, String> request) throws
//            IOException {
//        String newFileName = request.get("newFileName");
//        String oldFileName = fileService.findById(fileId).getFileName();
//        fileService.updateFileName(fileService.findById(fileId), newFileName);
//        minioService.renameFileInFolder(oldFileName, newFileName,
//                fileService.findById(fileId).getOwner(), fileService.findById(fileId).getUuid(), folder );
//        return ResponseEntity.ok("File rename seccessfully!");
//    }
//
//    @PostMapping("/api/{fileId}/{folder}")
//    public void putFileToFolder(@PathVariable("fileId") String fileId, @PathVariable("folder") String folder){
//        File file = fileService.findById(fileId);
//        String fullFileName = file.getOwner() + "-" + file.getFileName() + "-" + file.getUuid();
//        minioService.uploadFileToFolder(fullFileName, folder);
//        //fileService.setFolder(fileId, folder);
//    }
//
//    @PostMapping("/delete-file-from-folder/{fileId}/{folder}")
//    public void deleteFileFromFolder(@PathVariable("fildeId") String fileId,
//                                     @PathVariable("folder") String folder){
//        File file = fileService.findById(fileId);
//        String fullFileName = file.getOwner() + "-" + file.getFileName() + "-" + file.getUuid();
//        //fileService.deleteFileFromFolder(file);
//        minioService.deleteFileFromFolder(fullFileName, folder);
//
//    }

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

}
