package us.yarik.CloudFileStorage.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Autowired
    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }


    public void createBucket(String userInput, String email) throws NoSuchAlgorithmException, InvalidKeyException {
        if (!doesBucketExist(userInput)) {
            try {
                String userBucketName = email.substring(0, email.indexOf("@")) + "-" + userInput;

                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(userBucketName)
                        .build());

            } catch (ErrorResponseException e) {
                throw new RuntimeException(e);
            } catch (InsufficientDataException e) {
                throw new RuntimeException(e);
            } catch (InternalException e) {
                throw new RuntimeException(e);
            } catch (InvalidResponseException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ServerException e) {
                throw new RuntimeException(e);
            } catch (XmlParserException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<String> getBucketsForUser(String email) {
        try {
            List<String> bucketNames = new ArrayList<>();
            minioClient.listBuckets().forEach(bucket -> bucketNames.add(bucket.name()));

            return bucketNames.stream()
                    .filter(bucketName -> bucketName.contains(email.substring(0, email.indexOf("@"))))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving buckets: " + e.getMessage(), e);
        }
    }

    public boolean doesBucketExist(String bucketName) {
        try {
            return minioClient.listBuckets().stream()
                    .anyMatch(bucket -> bucket.name().equals(bucketName));
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }


    public ResponseEntity<InputStreamResource> downloadFile(String email, String bucketName, String fileName) throws IOException {
        InputStream fileStream = downloadFileFromMinIO(bucketName, fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        InputStreamResource resource = new InputStreamResource(fileStream);
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileStream.available())
                .body(resource);
    }

    public InputStream downloadFileFromMinIO(String bucketName, String fileName) throws IOException {
        try {

            String objectPath = bucketName + "/" + fileName;


            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
        } catch (MinioException | IOException e) {
            System.err.println("Ошибка при скачивании файла из MinIO: " + e.getMessage());
            throw new RuntimeException("Ошибка при скачивании файла из MinIO: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public void addFile(String bucketName, String fileName, InputStream inputStream, String contentType) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .contentType(contentType)
                .stream(inputStream, -1, 10485760)
                .build());
    }

    public List<String> allObjectsOnBucket(String bucketName) throws Exception {
        Iterable<Result<Item>> result = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .build());
        List<String> objects = new ArrayList<>();
        for (Result<Item> itemResult : result) {
            try {
                Item item = itemResult.get();
                String fileName = item.objectName();
                objects.add(fileName);
            } catch (Exception e) {
                throw new Exception("error " + e.getMessage());
            }
        }
        return objects;
    }

    public void deleteBucket(String bucketName) throws Exception {
        List<String> objects = allObjectsOnBucket(bucketName);

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

        minioClient.removeBucket(RemoveBucketArgs.builder()
                .bucket(bucketName)
                .build());
    }

    public void changeBucketName(String oldBucketName, String newBucketName) throws Exception {
        List<String> objects = allObjectsOnBucket(oldBucketName);
        for (String objectName : objects) {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(oldBucketName)
                    .object(objectName)
                    .build());
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(newBucketName)
                    .object(objectName)
                    .stream(inputStream, -1, 10485760)
                    .build());

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(oldBucketName)
                    .object(objectName)
                    .build());
        }
        deleteBucket(oldBucketName);
    }

    public String findFileByName(String bucketName, String fileName) throws Exception {
        List<String> objects = allObjectsOnBucket(bucketName);
        return objects.stream()
                .filter(obj -> obj.equals(fileName)).toString();
    }

    public void deleteFile(String bucketName, String fileName) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName).build());
    }

    public void renameFile(String bucketName, String fileName, String newFileName) throws IOException {
        try {

            CopySource copySource = CopySource.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build();

            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(newFileName)
                            .source(copySource)
                            .build()
            );

            deleteFile(bucketName, fileName);
        } catch (MinioException | IOException e) {
            throw new IOException("Error renaming file in MinIO", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }


}