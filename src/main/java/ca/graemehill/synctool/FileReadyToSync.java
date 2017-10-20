package ca.graemehill.synctool;

import ca.graemehill.synctool.model.FileMetadata;

public class FileReadyToSync {
    private FileMetadata fileMetadata;

    public FileReadyToSync(FileMetadata fileMetadata) {
        this.fileMetadata = fileMetadata;
    }

    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }
}
