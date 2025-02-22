package us.yarik.CloudFileStorage.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import us.yarik.CloudFileStorage.model.File;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends MongoRepository<File, ObjectId> {

    Optional<File> findByOwnerAndFileName(String owner, String fileName);
    void deleteByOwner(String owner);
    Optional<File> findById(String id);

    List<File> findByParentIdAndOwner(String parentId, String owner);

    List<File> findByOwnerAndParentIdIsNull(String owner);

    List<File> findByOwnerAndIsFolderIsTrue(String owner);

    List<File> findAllByOwnerAndParentId(String owner, String parentId);

    List<File> findByOwner(String owner);
}