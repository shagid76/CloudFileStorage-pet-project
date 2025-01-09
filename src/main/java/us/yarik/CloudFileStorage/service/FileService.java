package us.yarik.CloudFileStorage.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
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

    public void addFile(String bucketName, String fileName, InputStream inputStream, String contentType) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .contentType(contentType)
                .stream(inputStream, -1, 10485760)
                .build());
    }

    public List<String> allObjectsOnBucket(String bucketName) throws Exception{
        Iterable<Result<Item>> result =  minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .build());
        List<String> objects = new ArrayList<>();
        for(Result<Item>  itemResult : result){
            try{
                Item item = itemResult.get();
                String fileName = item.objectName();
                objects.add(fileName);
            }catch (Exception e){
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

//    public void changeBucketName(String bucketName){
//        minioClient.
//    }

}