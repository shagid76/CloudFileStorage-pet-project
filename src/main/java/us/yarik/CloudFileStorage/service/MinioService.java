package us.yarik.CloudFileStorage.service;

import io.minio.*;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    public ByteArrayResource downloadFile(String uuid) {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucketName)
                .object(uuid)
                .build();
        try (GetObjectResponse object = minioClient.getObject(getObjectArgs)) {
            return new ByteArrayResource(object.readAllBytes());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new ByteArrayResource(new byte[0]);
        }
    }

    public void addFile(InputStream inputStream, String contentType, String uuid) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(uuid)
                .contentType(contentType)
                .stream(inputStream, -1, 10485760)
                .build());
    }

    public void deleteFile(String uuid) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(uuid).build());
    }

    public InputStream getFile(String uuid) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException,
            InvalidResponseException, InternalException {

        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(uuid)
                        .build()
        );
    }
}