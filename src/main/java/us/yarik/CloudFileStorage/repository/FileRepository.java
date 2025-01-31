package us.yarik.CloudFileStorage.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import us.yarik.CloudFileStorage.model.File;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends MongoRepository<File, ObjectId> {

    List<File> findByOwner(String owner);
    Optional<File> findByOwnerAndFileName(String owner, String fileName);
    void deleteByOwner(String owner);
    Optional<File> findById(String id);

    List<File> findByParentId(String parentId);

    List<File> findByOwnerAndParentIdIsNull(String owner);

    List<File> findByOwnerAndFolderIsTrue(String owner);
}
