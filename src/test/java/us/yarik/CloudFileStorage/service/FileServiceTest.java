package us.yarik.CloudFileStorage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import us.yarik.CloudFileStorage.model.File;
import us.yarik.CloudFileStorage.repository.FileRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {
    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileService fileService;


    @Test
    void uploadFile_shouldCallFileRepository() throws Exception {
        fileService.uploadFile(TestResources.FILE);
        verify(fileRepository, times(1)).save(TestResources.FILE);
    }

    @Test
    void uploadFile_shouldThrowException() throws Exception {
        File file = null;
        assertThatThrownBy(() -> fileService.uploadFile(file))
                .isInstanceOf(Exception.class)
                .hasMessage("You can't upload null file!");
    }

    @Test
    void findByOwnerAndParentIdIsNull_shouldCallFileRepository() {
        fileService.findByOwnerAndParentIdIsNull(TestResources.FILE.getOwner());
        verify(fileRepository, times(1)).findByOwnerAndParentIdIsNull(TestResources.FILE.getOwner());
    }

    @Test
    void findByOwnerAndParentIdIsNull_shouldReturnList() {
        List<File> exampleFiles = new ArrayList<>();
        exampleFiles.add(TestResources.FILE);
        when(fileRepository.findByOwnerAndParentIdIsNull(TestResources.FILE.getOwner()))
                .thenReturn(exampleFiles);
        List<File> files = fileService.findByOwnerAndParentIdIsNull(TestResources.FILE.getOwner());
        assertNotNull(files);
        assertEquals(1, files.size());
        assertTrue(files.contains(TestResources.FILE));
        verify(fileRepository, times(1)).findByOwnerAndParentIdIsNull(TestResources.FILE.getOwner());
    }

    @Test
    void deleteFile_shouldCallFileRepository() throws Exception {
        doNothing().when(fileRepository).delete(TestResources.FILE);
        fileService.deleteFile(TestResources.FILE);
        verify(fileRepository, times(1)).delete(TestResources.FILE);
    }

    @Test
    void deleteFile_shouldThrowException() throws Exception {
        File file = null;
        assertThatThrownBy(() -> fileService.deleteFile(file))
                .isInstanceOf(Exception.class)
                .hasMessage("You can't delete null file!");
    }

    @Test
    void findByOwnerAndFileName_shouldReturnFile() {
        when(fileRepository.findByOwnerAndFileName(TestResources.FILE.getOwner(), TestResources.FILE.getFileName()))
                .thenReturn(Optional.of(TestResources.FILE));
        File file = fileService.findByOwnerAndFileName(TestResources.FILE.getOwner(), TestResources.FILE.getFileName());
        assertNotNull(file);
        assertEquals(TestResources.FILE, file);
    }

    @Test
    void findByOwnerAndFileName_shouldReturnNull() {
        when(fileRepository.findByOwnerAndFileName(TestResources.FILE.getOwner(), TestResources.FILE.getFileName()))
                .thenReturn(Optional.empty());
        File file = fileService.findByOwnerAndFileName(TestResources.FILE.getOwner(), TestResources.FILE.getFileName());
        assertNull(file);
    }

    @Test
    void findByOwnerAndFileName_shouldCallFileRepository_whenFileIsNull() {
        when(fileRepository.findByOwnerAndFileName(TestResources.FILE.getOwner(), TestResources.FILE.getFileName()))
                .thenReturn(Optional.empty());
        File file = fileService.findByOwnerAndFileName(TestResources.FILE.getOwner(), TestResources.FILE.getFileName());
        assertNull(file);
        verify(fileRepository, times(1)).findByOwnerAndFileName(TestResources.FILE.getOwner(),
                TestResources.FILE.getFileName());
    }

    @Test
    void findByOwnerAndFileName_shouldCallRepository_whenFileExist() {
        when(fileRepository.findByOwnerAndFileName(TestResources.FILE.getOwner(), TestResources.FILE.getFileName()))
                .thenReturn(Optional.of(TestResources.FILE));
        File file = fileService.findByOwnerAndFileName(TestResources.FILE.getOwner(), TestResources.FILE.getFileName());
        assertNotNull(file);
        verify(fileRepository, times(1)).findByOwnerAndFileName(TestResources.FILE.getOwner(),
                TestResources.FILE.getFileName());
    }

    @Test
    void updateFileName_shouldCallFileRepository() throws Exception {
        String newFileName = "new name";
        fileService.updateFileName(TestResources.FILE, newFileName);
        assertThat(TestResources.FILE.getFileName()).isEqualTo(newFileName);
        verify(fileRepository, times(1)).save(TestResources.FILE);
    }

    @Test
    void updateFileName_shouldThrowException() throws Exception {
        String newFileName = "";
        assertThatThrownBy(() -> fileService.updateFileName(TestResources.FILE, newFileName))
                .isInstanceOf(Exception.class)
                .hasMessage("File name can't be null!");

    }

    @Test
    void deleteFilesByOwner_shouldCallFileRepository() throws Exception {
        fileService.deleteFilesByOwner(TestResources.FILE.getOwner());
        verify(fileRepository, times(1)).deleteByOwner(TestResources.FILE.getOwner());
    }

    @Test
    void deleteFilesByOwner_shouldThrowException() throws Exception {
        String owner = "";
        assertThatThrownBy(() -> fileService.deleteFilesByOwner(owner))
                .isInstanceOf(Exception.class)
                .hasMessage("Owner can't be null!");

    }

    @Test
    void findById_shouldReturnFile() {
        when(fileRepository.findById(TestResources.FILE.getId())
        ).thenReturn(Optional.of(TestResources.FILE));
        File file = fileService.findById(TestResources.FILE.getId());
        assertNotNull(file);
        assertEquals(TestResources.FILE, file);
    }

    @Test
    void findById_shouldReturnNull() {
        when(fileRepository.findById(TestResources.FILE.getId())
        ).thenReturn(Optional.empty());
        File file = fileService.findById(TestResources.FILE.getId());
        assertNull(file);
    }

    @Test
    void findById_shouldCallRepository_whenFIleIsNull() {
        when(fileRepository.findById(TestResources.FILE.getId())
        ).thenReturn(Optional.empty());
        File file = fileService.findById(TestResources.FILE.getId());
        assertNull(file);
        verify(fileRepository, times(1)).findById(TestResources.FILE.getId());
    }

    @Test
    void findById_shouldCallRepository_whenFIleExist() {
        when(fileRepository.findById(TestResources.FILE.getId())
        ).thenReturn(Optional.of(TestResources.FILE));
        File file = fileService.findById(TestResources.FILE.getId());
        assertNotNull(file);
        verify(fileRepository, times(1)).findById(TestResources.FILE.getId());
    }

    @Test
    void findByParentIdAndOwner_shouldReturnList() {
        List<File> exampleList = new ArrayList<>();
        exampleList.add(TestResources.FILE);

        when(fileRepository.findByParentIdAndOwner(TestResources.FILE.getParentId(),
                TestResources.FILE.getOwner()))
                .thenReturn(exampleList);
        List<File> files = fileService.findByParentIdAndOwner(TestResources.FILE.getParentId(),
                TestResources.FILE.getOwner());
        assertNotNull(files);
        assertEquals(1, files.size());
        assertTrue(files.contains(TestResources.FILE));
        verify(fileRepository, times(1)).findByParentIdAndOwner(TestResources.FILE.getParentId(),
                TestResources.FILE.getOwner());
    }

    @Test
    void findByParentIdAndOwner_shouldCallRepository() {
        fileService.findByParentIdAndOwner(TestResources.FILE.getParentId(), TestResources.FILE.getOwner());
        verify(fileRepository, times(1)).findByParentIdAndOwner(TestResources.FILE.getParentId(),
                TestResources.FILE.getOwner());
    }

    @Test
    void putFileToFolder_shouldThrowException() {
        String parentId = "";
        assertThatThrownBy(() -> fileService.putFileToFolder(parentId, TestResources.FILE.getId()))
                .isInstanceOf(Exception.class)
                .hasMessage("ParentId can't be null!");


    }
    @Test
    void putFileToFolder_shouldCallFileRepository() throws Exception {
        when(fileRepository.findById(TestResources.FILE.getId()))
                .thenReturn(Optional.of(TestResources.FILE));
        fileService.putFileToFolder(TestResources.FILE.getParentId(), TestResources.FILE.getId());
        verify(fileRepository, times(1)).save(TestResources.FILE);
    }

    @Test
    void findByOwnerAndIsFolderIsTrue_shouldReturnList() {
        List<File> exampleList = new ArrayList<>();
        exampleList.add(TestResources.FILE);

        when(fileRepository.findByOwnerAndIsFolderIsTrue(TestResources.FILE.getOwner()))
                .thenReturn(exampleList);
        List<File> files = fileService.findByOwnerAndIsFolderIsTrue(TestResources.FILE.getOwner());
        assertNotNull(files);
        assertEquals(1, files.size());
        assertTrue(files.contains(TestResources.FILE));
        verify(fileRepository, times(1))
                .findByOwnerAndIsFolderIsTrue(TestResources.FILE.getOwner());
    }

    @Test
    void findByOwnerAndIsFolderIsTrue_shouldCallRepository() {
        fileService.findByOwnerAndIsFolderIsTrue(TestResources.FILE.getOwner());
        verify(fileRepository, times(1))
                .findByOwnerAndIsFolderIsTrue(TestResources.FILE.getOwner());
    }

    @Test
    void findByOwnerAndFileNameList_shouldReturnList() {
        List<File> exampleList = new ArrayList<>();
        exampleList.add(TestResources.FILE);

        when(fileRepository.findAllByOwnerAndParentId(TestResources.FILE.getOwner(),
                TestResources.FILE.getParentId()))
                .thenReturn(exampleList);
        List<File> files = fileService.findByOwnerAndFileNameList(TestResources.FILE.getOwner(),
                TestResources.FILE.getParentId());
        assertNotNull(files);
        assertEquals(1, files.size());
        assertTrue(files.contains(TestResources.FILE));
        verify(fileRepository, times(1))
                .findAllByOwnerAndParentId(TestResources.FILE.getOwner(), TestResources.FILE.getParentId());
    }

    @Test
    void findByOwnerAndFileNameList_shouldCallRepository() {
        fileService.findByOwnerAndFileNameList(TestResources.FILE.getOwner(), TestResources.FILE.getParentId());
        verify(fileRepository, times(1))
                .findAllByOwnerAndParentId(TestResources.FILE.getOwner(), TestResources.FILE.getParentId());
    }

    @Test
    void setParentId_shouldCallFileRepository() throws Exception {
        String parentId = "new parentId";
        fileService.setParentId(TestResources.FILE, parentId);
        assertThat(TestResources.FILE.getParentId()).isEqualTo(parentId);
        verify(fileRepository, times(1)).save(TestResources.FILE);
    }

    @Test
    void setParentId_shouldThrowException() throws Exception {
        String parentId = "";

        assertThatThrownBy(() -> fileService.setParentId(TestResources.FILE, parentId))
                .isInstanceOf(Exception.class)
                .hasMessage("ParentId can't be null!");
    }

    @Test
    void findByOwner_shouldCallRepository() throws Exception {
        fileService.findByOwner(TestResources.FILE.getOwner());
        verify(fileRepository, times(1)).findByOwner(TestResources.FILE.getOwner());
    }

    @Test
    void findByOwner_shouldThrowException() throws Exception {
        String owner = "";
        assertThatThrownBy(() -> fileService.findByOwner(owner))
                .isInstanceOf(Exception.class)
                .hasMessage("Owner can't be null!");
    }

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
