package us.yarik.CloudFileStorage.service;

import io.minio.*;
import io.minio.errors.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MinioServiceTest {
    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioService minioService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minioService, "bucketName", "test-bucket");
    }

    @Test
    void downloadFile_shouldDownload() throws IOException, ServerException, InsufficientDataException,
            ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException,
            XmlParserException, InternalException {
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(mockResponse.readAllBytes()).thenReturn(TestResources.FILE_DATA);

        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);
        ByteArrayResource result = minioService.downloadFile(TestResources.UUID);
        assertEquals(TestResources.FILE_DATA.length, result.contentLength());
    }

    @Test
    void downloadFile_shouldReturnEmptyResourceOnFailure() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new IOException("MinIO error"));

        ByteArrayResource result = minioService.downloadFile(TestResources.UUID);

        assertEquals(0, result.contentLength());
    }

    @Test
    void addFile_shouldUploadSuccessfully() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(TestResources.FILE_DATA);

        minioService.addFile(inputStream, TestResources.CONTENT_TYPE, TestResources.UUID);

        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void deleteFile_shouldRemoveFileSuccessfully() throws Exception {
        minioService.deleteFile(TestResources.UUID);

        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void getFile_shouldReturnInputStream() throws Exception {
        InputStream mockInputStream = new ByteArrayInputStream(TestResources.FILE_DATA);
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        InputStream result = minioService.getFile(TestResources.UUID);

        assertNotNull(result);
    }

    @Test
    void getFile_shouldThrowExceptionOnFailure() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new IOException("MinIO error"));

        assertThrows(IOException.class, () -> minioService.getFile(TestResources.UUID));
    }


    public class TestResources {
        static final String UUID = java.util.UUID.randomUUID().toString();
        static final String BUCKET_NAME = "test-bucket";
        static final String CONTENT_TYPE = "application/octet-stream";
        static final byte[] FILE_DATA = "Test file content".getBytes();
        static final InputStream FILE_STREAM = new ByteArrayInputStream(FILE_DATA);
        static final ByteArrayResource FILE_RESOURCE = new ByteArrayResource(FILE_DATA);
    }

}