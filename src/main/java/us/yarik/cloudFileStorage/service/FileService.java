package us.yarik.cloudFileStorage.service;

import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Autowired
    public FileService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    //    public void uploadFile(String fileName, InputStream fileContent) throws Exception {
//        ObjectWriteResponse objectWriteResponse = minioClient.putObject(PutObjectArgs.builder()
//                .bucket(bucketName)
//                .object(fileName)
//                .stream(new FileInputStream("/tmp/" + fileName))
//                .build());
//    }
//
//    public InputStream downloadFile(String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
//        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
//                .bucket(bucketName)
//                .object(fileName)
//                .build();
//
//        return minioClient.getObject(getObjectArgs);
//    }
//



    public void createBucket(String userInput) throws NoSuchAlgorithmException, InvalidKeyException {
        if (!doesBucketExist(userInput)) {
            try {

                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(userInput)
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
                    .filter(bucketName -> bucketName.contains(email.toLowerCase()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving buckets: " + e.getMessage(), e);
        }
    }

    public boolean doesBucketExist(String bucketName) {
        try {
            // Получаем список всех бакетов и проверяем, существует ли бакет с нужным именем
            return minioClient.listBuckets().stream()
                    .anyMatch(bucket -> bucket.name().equals(bucketName));
        } catch (Exception e) {
            // Обработка ошибок при попытке получить список бакетов
            throw new RuntimeException("Error checking if bucket exists: " + e.getMessage(), e);
        }
    }


    public String uploudFile(String email, String fileName, String contentType, InputStream fileInputStream, long size) {
        try {
            String objectName = email + "/" + fileName;
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(fileInputStream, size, -1)
                            .contentType(contentType)
                            .build()

            );
            return "File uploaded successfully " + objectName;
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public InputStream downloadFile(String email, String fileName) {
        try {
            String objectPath = email + "/" + fileName;
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}