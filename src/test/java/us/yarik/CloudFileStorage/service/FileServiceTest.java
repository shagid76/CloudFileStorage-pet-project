package us.yarik.CloudFileStorage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import us.yarik.CloudFileStorage.model.File;
import us.yarik.CloudFileStorage.repository.FileRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.InstanceOfAssertFactories.FILE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {
    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileService fileService;


    @Test
    void uploadFile_shouldCallFileRepository() {
        fileService.uploadFile(TestResources.FILE);
        verify(fileRepository, times(1)).save(TestResources.FILE);
    }

    @Test
    void deleteFile_shouldCallFileRepository(){
        fileService.deleteFile(TestResources.FILE);
        verify(fileRepository, times(1)).delete(TestResources.FILE);
    }

    @Test
    void findByOwnerAndFileName_shouldReturnFile(){
        when(fileRepository.findByOwnerAndFileName(TestResources.FILE.getOwner(), TestResources.FILE.getFileName()))
                .thenReturn(Optional.of(TestResources.FILE));
        File file = fileService.findByOwnerAndFileName(TestResources.FILE.getOwner(), TestResources.FILE.getFileName());
        assertNotNull(file);
        assertEquals(TestResources.FILE, file);
    }

//    @Test
//    void updateFileName_shouldUpdateFileName(){
//        when()
//    }

    static class TestResources {
        static final String FILENAME = "NAME";
        static final Long FILESIZE = 100L;
        static final String FILETYPE = "image/png";
        static final LocalDateTime UPLOADDATE = LocalDateTime.now();
        static final String OWNER = "OWNER";
        static final String UUID = java.util.UUID.randomUUID().toString();
        static final String PARENTID = "PARENTID";
        static final boolean ISFOLDER = false;
        static final String FILE_ID = "1";
        static final File FILE = File.builder()
                .id(FILE_ID)
                .fileName(FILENAME)
                .fileSize(FILESIZE)
                .fileType(FILETYPE)
                .uploadDate(UPLOADDATE)
                .owner(OWNER)
                .uuid(UUID)
                .parentId(PARENTID)
                .isFolder(ISFOLDER)
                .build();
    }
}
