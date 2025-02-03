package us.yarik.CloudFileStorage.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//TODO fix object name on minio
@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public ByteArrayResource downloadFile(String fileName) {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build();
        try (GetObjectResponse object = minioClient.getObject(getObjectArgs)) {
            return new ByteArrayResource(object.readAllBytes());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new ByteArrayResource(new byte[0]);
        }
    }

    public void addFile(String owner, String fileName, InputStream inputStream, String contentType, String uuid) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(owner + "-" + fileName + "-" + uuid)
                .contentType(contentType)
                .stream(inputStream, -1, 10485760)
                .build());
    }
    public void addFile(String owner, String fileName, InputStream inputStream, String contentType, String uuid, String folder) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(owner + "-" + fileName + "-" + uuid + "-" + folder)
                .contentType(contentType)
                .stream(inputStream, -1, 10485760)
                .build());
    }

    public List<String> allObjectsByOwner(String owner) throws Exception {
        Iterable<Result<Item>> result = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .build());
        List<String> objects = new ArrayList<>();
        for (Result<Item> itemResult : result) {
            try {
                Item item = itemResult.get();
                String fileName = item.objectName();
                if (fileName.contains(owner)) {
                    objects.add(fileName);
                }
            } catch (Exception e) {
                throw new Exception("error " + e.getMessage());
            }
        }
        return objects;
    }

    public void deleteFilesByOwner(String owner) throws Exception {
        List<String> objects = allObjectsByOwner(owner);

        Iterable<DeleteObject> objectsToDelete = objects.stream()
                .map(DeleteObject::new)
                .collect(Collectors.toList());

        Iterable<Result<DeleteError>> results =
                minioClient.removeObjects(
                        RemoveObjectsArgs.builder()
                                .bucket(bucketName)
                                .objects(objectsToDelete)
                                .build());

        for (Result<DeleteError> result : results) {
            DeleteError error = result.get();
        }
    }

    public void deleteFile(String fileName) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName).build());
    }

    public void renameFile(String fileName, String newFileName, String owner, String uuid) throws IOException {
        try {

            CopySource copySource = CopySource.builder()
                    .bucket(bucketName)
                    .object(owner + "-" + fileName + "-" + uuid)
                    .build();

            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(owner + "-" + newFileName + "-" + uuid)
                            .source(copySource)
                            .build()
            );

            deleteFile(owner + "-" + fileName + "-" + uuid);
        } catch (MinioException | IOException e) {
            throw new IOException("Error renaming file in MinIO", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public void renameFileInFolder(String fileName, String newFileName, String owner, String uuid, String folder) throws IOException {
        try {

            CopySource copySource = CopySource.builder()
                    .bucket(bucketName)
                    .object(owner + "-" + fileName + "-" + uuid + "-" + folder)
                    .build();

            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(owner + "-" + newFileName + "-" + uuid + "-" + folder)
                            .source(copySource)
                            .build()
            );

            deleteFile(owner + "-" + fileName + "-" + uuid + "-" + folder);
        } catch (MinioException | IOException e) {
            throw new IOException("Error renaming file in MinIO", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getFile(String owner, String fileName, String uuid) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException,
            InvalidResponseException, InternalException {

        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(owner + "-" + fileName + "-" + uuid)
                        .build()
        );
    }
    public InputStream getFileFromFolder(String owner, String fileName, String uuid, String folder) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException,
            InvalidResponseException, InternalException {

        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(owner + "-" + fileName + "-" + uuid + "-" + folder)
                        .build()
        );
    }

    public void uploadFileToFolder(String fullFileName, String parentId){
        try {

            CopySource copySource = CopySource.builder()
                    .bucket(bucketName)
                    .object(fullFileName)
                    .build();

            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fullFileName + "-" + parentId)
                            .source(copySource)
                            .build()
            );

            deleteFile(fullFileName);
        } catch (MinioException | IOException e) {
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFileFromFolder(String fullFileName, String folder){
        try {

            CopySource copySource = CopySource.builder()
                    .bucket(bucketName)
                    .object(fullFileName + "-" + folder)
                    .build();

            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fullFileName)
                            .source(copySource)
                            .build()
            );

            deleteFile(fullFileName);
        } catch (MinioException | IOException e) {
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

}