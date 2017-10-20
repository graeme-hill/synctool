package ca.graemehill.synctool;

import ca.graemehill.synctool.model.FileMetadata;

public class FileDiscovered {
    private FileMetadata metadata;

    public FileDiscovered(FileMetadata metadata) {
        this.metadata = metadata;
    }

    public FileMetadata getMetadata() {
        return metadata;
    }
}
