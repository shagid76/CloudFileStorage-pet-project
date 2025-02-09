package us.yarik.CloudFileStorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import us.yarik.CloudFileStorage.model.File;
import us.yarik.CloudFileStorage.repository.FileRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;

    public List<File> findAll() {
        return fileRepository.findAll();
    }

    public File uploadFile(File file) throws Exception {
        if (file == null) {
            throw new Exception("You can't upload null file!");
        }
        return fileRepository.save(file);
    }

    public List<File> findByOwnerAndParentIdIsNull(String owner) {
        return fileRepository.findByOwnerAndParentIdIsNull(owner);
    }


    public void deleteFile(File file) throws Exception {
        if (file == null) {
            throw new Exception("You can't delete null file!");
        }
        fileRepository.delete(file);
    }

    public File findByOwnerAndFileName(String owner, String fileName) {
        Optional<File> file = fileRepository.findByOwnerAndFileName(owner, fileName);
        if (file.isPresent()) {
            return file.get();
        }
        return file.orElse(null);
    }

    public void updateFileName(File file, String fileName) throws Exception {
        if (fileName.isEmpty() || fileName == null) {
            throw new Exception("File name can't be null!");
        }
        file.setFileName(fileName);
        fileRepository.save(file);
    }

    public void deleteFilesByOwner(String owner) throws Exception {
        if (owner.isEmpty() || owner == null) {
            throw new Exception("Owner can't be null!");
        }
        fileRepository.deleteByOwner(owner);
    }

    public File findById(String id) {
        Optional<File> file = fileRepository.findById(id);
        if (file.isPresent()) {
            return file.get();
        }
        return file.orElse(null);
    }

    public List<File> findByParentIdAndOwner(String parentId, String owner) {
        return fileRepository.findByParentIdAndOwner(parentId, owner);
    }

    public void putFileToFolder(String parentId, String fileId) throws Exception{
        File file = findById(fileId);
        if(parentId == null || parentId.isEmpty()){
            throw new Exception("ParentId can't be null!");
        }
        file.setParentId(parentId);
        fileRepository.save(file);
    }

    public List<File> findByOwnerAndIsFolderIsTrue(String owner) {
        return fileRepository.findByOwnerAndIsFolderIsTrue(owner);
    }

    public List<File> findByOwnerAndFileNameList(String owner, String fileName) {
        return fileRepository.findAllByOwnerAndParentId(owner, fileName);
    }

    public void setParentId(File folder, String parentId) throws Exception {
        if (parentId.isEmpty() || parentId == null) {
            throw new Exception("ParentId can't be null!");
        }
        folder.setParentId(parentId);
        fileRepository.save(folder);
    }

    public List<File> findByOwner(String owner) throws Exception{
        if(owner.isEmpty() || owner == null){
            throw new Exception("Owner can't be null!");
        }
        return fileRepository.findByOwner(owner);
    }

}
