package us.yarik.CloudFileStorage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FolderDTO {
    private String folderName;
    private String parentId;
    private String owner;
}
