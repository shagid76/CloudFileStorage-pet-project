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

    public File uploadFile(File file) {
        return fileRepository.save(file);
    }

    public List<File> findByOwnerAndParentIdIsNull(String owner) {
        return fileRepository.findByOwnerAndParentIdIsNull(owner);
    }


    public void deleteFile(File file) {
        fileRepository.delete(file);
    }

    public File findByOwnerAndFileName(String owner, String fileName) {
        Optional<File> file = fileRepository.findByOwnerAndFileName(owner, fileName);
        if (file.isPresent()) {
            return file.get();
        }
        return file.orElse(null);
    }

    public void updateFileName(File file, String fileName) {
        file.setFileName(fileName);
        fileRepository.save(file);
    }
    public void deleteFilesByOwner(String owner){
        fileRepository.deleteByOwner(owner);
    }

    public File findById(String id){
        Optional<File> file = fileRepository.findById(id);
        if(file.isPresent()){
            return file.get();
        }
        return file.orElse(null);
     }

     public List<File> findByParentId(String parentId){
        return fileRepository.findByParentId(parentId);
     }

     public void putFileToFolder(String parentId, String fileId){
        File file = findById(fileId);
        file.setParentId(parentId);
        file.setMinioPath(file.getMinioPath() + "-" + parentId);
        fileRepository.save(file);
     }
     public List<File> findByOwnerAndIsFolderIsTrue(String owner){
        return fileRepository.findByOwnerAndIsFolderIsTrue(owner);
     }

     public File findByOwnerAndParentIdAndFileName(String owner, String parentId, String fileName){
        Optional<File> file = fileRepository.findByOwnerAndParentIdAndFileName(owner, parentId, fileName);
        if(file.isPresent()){
            return file.get();
        }
        return file.orElse(null);
     }

     public List<File> findByOwnerAndFileNameList(String owner, String fileName){
        return fileRepository.findAllByOwnerAndParentId(owner, fileName);
     }

}
