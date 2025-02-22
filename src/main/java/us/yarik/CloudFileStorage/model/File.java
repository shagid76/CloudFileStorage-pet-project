package us.yarik.CloudFileStorage.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "fileMetadata")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class File {
    @Id
    private String id;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadDate;
    private String owner;
    private String uuid;

    private String parentId;
    private boolean isFolder;

}